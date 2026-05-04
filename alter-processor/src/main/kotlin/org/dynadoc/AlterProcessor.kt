package org.dynadoc

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.writeTo

class AlterProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val processedPackages = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Alter::class.qualifiedName!!)
        val unprocessed = symbols.filter { !it.validate() }.toList()
        symbols.filterIsInstance<KSClassDeclaration>().filter { it.validate() }.forEach { processClass(it) }
        return unprocessed
    }

    private fun processClass(classDeclaration: KSClassDeclaration) {
        if (Modifier.DATA !in classDeclaration.modifiers) {
            logger.error("@Alter can only be applied to data classes", classDeclaration)
            return
        }
        val primaryConstructor = classDeclaration.primaryConstructor ?: return
        val parameters = primaryConstructor.parameters
        if (parameters.isEmpty()) return
        val packageName = classDeclaration.packageName.asString()
        val containingFile = classDeclaration.containingFile!!
        if (packageName !in processedPackages) {
            processedPackages += packageName
            generateOptional(packageName, containingFile)
        }
        generateBuilderAndAlter(classDeclaration, parameters, packageName, containingFile)
    }

    private data class CollectionInfo(
        val mutableClass: ClassName,
        val toMutable: String,
        val toImmutable: String,
    )

    private fun nestedBuilderClass(type: KSType): ClassName? {
        val declaration = type.declaration
        val isAlterAnnotated = declaration.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == Alter::class.qualifiedName
        }
        if (!isAlterAnnotated) return null
        return ClassName(declaration.packageName.asString(), "${declaration.simpleName.asString()}Builder")
    }

    private fun collectionInfo(type: KSType): CollectionInfo? {
        if (type.isMarkedNullable) return null
        return when (type.declaration.qualifiedName?.asString()) {
        "kotlin.collections.List" -> CollectionInfo(ClassName("kotlin.collections", "MutableList"), "toMutableList", "toList")
        "kotlin.collections.Set"  -> CollectionInfo(ClassName("kotlin.collections", "MutableSet"),  "toMutableSet",  "toSet")
        "kotlin.collections.Map"  -> CollectionInfo(ClassName("kotlin.collections", "MutableMap"),  "toMutableMap",  "toMap")
        else -> null
        }
    }

    private fun generateOptional(packageName: String, containingFile: KSFile) {
        val T = TypeVariableName("T", variance = KModifier.OUT)
        val TInvariant = TypeVariableName("T")
        val optionalClass = ClassName(packageName, "Optional")

        val noneSpec = TypeSpec.objectBuilder("None")
            .superclass(optionalClass.parameterizedBy(NOTHING))
            .build()

        val someSpec = TypeSpec.classBuilder("Some")
            .addModifiers(KModifier.DATA)
            .addTypeVariable(TInvariant)
            .primaryConstructor(FunSpec.constructorBuilder().addParameter("value", TInvariant).build())
            .addProperty(PropertySpec.builder("value", TInvariant).initializer("value").build())
            .superclass(optionalClass.parameterizedBy(TInvariant))
            .build()

        val TCompanion = TypeVariableName("T")
        val companionSpec = TypeSpec.companionObjectBuilder()
            .addFunction(
                FunSpec.builder("of")
                    .addTypeVariable(TCompanion)
                    .addParameter("value", TCompanion.copy(nullable = true))
                    .returns(optionalClass.parameterizedBy(TCompanion))
                    .addCode("return if (value == null) None else Some(value)")
                    .build()
            )
            .build()

        val unsafeVariance = AnnotationSpec.builder(ClassName("kotlin", "UnsafeVariance")).build()
        val TAnnotated = TypeVariableName("T").copy(annotations = listOf(unsafeVariance))
        val getOrElseFunc = FunSpec.builder("getOrElse")
            .addParameter("default", LambdaTypeName.get(returnType = TAnnotated))
            .returns(T)
            .beginControlFlow("return when (this)")
            .addStatement("is Some -> value")
            .addStatement("None -> default()")
            .endControlFlow()
            .build()

        FileSpec.builder(packageName, "AlterOptional")
            .addType(
                TypeSpec.classBuilder("Optional")
                    .addModifiers(KModifier.INTERNAL, KModifier.SEALED)
                    .addTypeVariable(T)
                    .addType(noneSpec)
                    .addType(someSpec)
                    .addType(companionSpec)
                    .addFunction(getOrElseFunc)
                    .build()
            )
            .build()
            .writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private data class ParamMeta(
        val name: String,
        val typeName: TypeName,
        val collectionInfo: CollectionInfo?,
        val mutableType: TypeName?,
        val nestedBuilderClass: ClassName?,
        val elementBuilders: List<ClassName?>?,
    )

    // Returns CodeBlock for initializing the mutable collection field from original.{paramName}
    private fun collectionInitBlock(paramName: String, info: CollectionInfo, elementBuilders: List<ClassName?>): CodeBlock {
        if (elementBuilders.none { it != null }) return CodeBlock.of("original.%N.%N()", paramName, info.toMutable)
        return when (info.mutableClass.simpleName) {
            "MutableList", "MutableSet" ->
                CodeBlock.of("original.%N.map·{ %T(it) }.%N()", paramName, elementBuilders[0]!!, info.toMutable)
            "MutableMap" -> {
                val keyBuilder = elementBuilders.getOrNull(0)
                val valBuilder = elementBuilders.getOrNull(1)
                when {
                    keyBuilder != null && valBuilder != null ->
                        CodeBlock.of("original.%N.entries.associate·{ %T(it.key) to %T(it.value) }.toMutableMap()", paramName, keyBuilder, valBuilder)
                    valBuilder != null ->
                        CodeBlock.of("original.%N.mapValues·{ %T(it.value) }.toMutableMap()", paramName, valBuilder)
                    else ->
                        CodeBlock.of("original.%N.entries.associate·{ %T(it.key) to it.value }.toMutableMap()", paramName, keyBuilder!!)
                }
            }
            else -> CodeBlock.of("original.%N.%N()", paramName, info.toMutable)
        }
    }

    // Returns CodeBlock for the build() expression for a collection field named {name}
    private fun collectionBuildBlock(name: String, info: CollectionInfo, elementBuilders: List<ClassName?>): CodeBlock {
        if (elementBuilders.none { it != null }) return CodeBlock.of("%N.%N()", name, info.toImmutable)
        return when (info.mutableClass.simpleName) {
            "MutableList", "MutableSet" ->
                CodeBlock.of("%N.map·{ it.build() }.%N()", name, info.toImmutable)
            "MutableMap" -> {
                val keyBuilder = elementBuilders.getOrNull(0)
                val valBuilder = elementBuilders.getOrNull(1)
                when {
                    keyBuilder != null && valBuilder != null ->
                        CodeBlock.of("%N.entries.associate·{ it.key.build() to it.value.build() }.toMap()", name)
                    valBuilder != null ->
                        CodeBlock.of("%N.mapValues·{ it.value.build() }.toMap()", name)
                    else ->
                        CodeBlock.of("%N.entries.associate·{ it.key.build() to it.value }.toMap()", name)
                }
            }
            else -> CodeBlock.of("%N.%N()", name, info.toImmutable)
        }
    }

    private fun generateBuilderAndAlter(
        classDeclaration: KSClassDeclaration,
        parameters: List<KSValueParameter>,
        packageName: String,
        containingFile: KSFile,
    ) {
        val typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()
        val className = classDeclaration.simpleName.asString()
        val classTypeName = classDeclaration.toClassName()
        val builderClassName = ClassName(packageName, "${className}Builder")
        val optionalClass = ClassName(packageName, "Optional")

        val paramMetas = parameters.map { param ->
            val resolvedType = param.type.resolve()
            val typeName = param.type.toTypeName(typeParamResolver)
            val info = collectionInfo(resolvedType)
            val elementBuilders = info?.let {
                resolvedType.arguments.map { a -> a.type?.resolve()?.let { t -> nestedBuilderClass(t) } }
            }
            val mutableType = info?.let { i ->
                val args = resolvedType.arguments.mapIndexed { idx, a ->
                    elementBuilders?.get(idx) ?: a.type?.toTypeName(typeParamResolver) ?: STAR
                }
                i.mutableClass.parameterizedBy(args)
            }
            val nestedBuilder = if (info == null) nestedBuilderClass(resolvedType) else null
            ParamMeta(param.name!!.asString(), typeName, info, mutableType, nestedBuilder, elementBuilders)
        }

        val builderSpec = TypeSpec.classBuilder("${className}Builder")
            .primaryConstructor(FunSpec.constructorBuilder().addParameter("original", classTypeName).build())
            .addProperty(PropertySpec.builder("original", classTypeName).addModifiers(KModifier.PRIVATE).initializer("original").build())

        paramMetas.forEach { (name, typeName, info, mutableType, nestedBuilder, elementBuilders) ->
            val fieldName = "${name}Field"
            when {
                info != null && mutableType != null -> {
                    builderSpec.addProperty(
                        PropertySpec.builder(fieldName, mutableType)
                            .addModifiers(KModifier.PRIVATE)
                            .mutable(true)
                            .initializer(collectionInitBlock(name, info, elementBuilders ?: emptyList()))
                            .build()
                    )
                    builderSpec.addProperty(
                        PropertySpec.builder(name, mutableType)
                            .mutable(true)
                            .getter(FunSpec.getterBuilder().addCode("return %N\n", fieldName).build())
                            .setter(FunSpec.setterBuilder().addParameter("value", mutableType).addCode("%N = value\n", fieldName).build())
                            .build()
                    )
                }
                nestedBuilder != null -> {
                    builderSpec.addProperty(
                        PropertySpec.builder(fieldName, nestedBuilder)
                            .addModifiers(KModifier.PRIVATE)
                            .mutable(true)
                            .initializer("%T(original.%N)", nestedBuilder, name)
                            .build()
                    )
                    builderSpec.addProperty(
                        PropertySpec.builder(name, nestedBuilder)
                            .mutable(true)
                            .getter(FunSpec.getterBuilder().addCode("return %N\n", fieldName).build())
                            .setter(FunSpec.setterBuilder().addParameter("value", nestedBuilder).addCode("%N = value\n", fieldName).build())
                            .build()
                    )
                }
                else -> {
                    builderSpec.addProperty(
                        PropertySpec.builder(fieldName, optionalClass.parameterizedBy(typeName))
                            .addModifiers(KModifier.PRIVATE)
                            .mutable(true)
                            .initializer("%T.None", optionalClass)
                            .build()
                    )
                    builderSpec.addProperty(
                        PropertySpec.builder(name, typeName)
                            .mutable(true)
                            .getter(FunSpec.getterBuilder().addCode("return %N.getOrElse { original.%N }\n", fieldName, name).build())
                            .setter(FunSpec.setterBuilder().addParameter("value", typeName).addCode("%N = %T.of(value)\n", fieldName, optionalClass).build())
                            .build()
                    )
                }
            }
        }

        builderSpec.addFunction(
            FunSpec.builder("build")
                .returns(classTypeName)
                .addCode(
                    CodeBlock.builder()
                        .add("return %T(\n", classTypeName)
                        .indent()
                        .apply {
                            paramMetas.forEach { (name, _, info, _, nestedBuilder, elementBuilders) ->
                                when {
                                    info != null -> add("%N = %L,\n", name, collectionBuildBlock(name, info, elementBuilders ?: emptyList()))
                                    nestedBuilder != null -> add("%N = %N.build(),\n", name, name)
                                    else -> add("%N = %N,\n", name, name)
                                }
                            }
                        }
                        .unindent()
                        .add(")")
                        .build()
                )
                .build()
        )

        val alterFunc = FunSpec.builder("alter")
            .receiver(classTypeName)
            .returns(classTypeName)
            .addParameter("block", LambdaTypeName.get(receiver = builderClassName, returnType = UNIT))
            .addCode("return %T(this).apply(block).build()", builderClassName)
            .build()

        val toBuilderFunc = FunSpec.builder("toBuilder")
            .receiver(classTypeName)
            .returns(builderClassName)
            .addCode("return %T(this)", builderClassName)
            .build()

        FileSpec.builder(packageName, "${className}Alter")
            .addType(builderSpec.build())
            .addFunction(alterFunc)
            .addFunction(toBuilderFunc)
            .build()
            .writeTo(codeGenerator, Dependencies(false, containingFile))
    }
}