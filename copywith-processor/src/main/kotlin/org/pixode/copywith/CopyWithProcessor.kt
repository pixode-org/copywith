package org.pixode.copywith

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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
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
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class CopyWithProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(CopyWith::class.qualifiedName!!)
        val unprocessed = symbols.filter { !it.validate() }.toList()
        symbols.filterIsInstance<KSClassDeclaration>().filter { it.validate() }.forEach { processClass(it) }
        return unprocessed
    }

    private fun processClass(classDeclaration: KSClassDeclaration) {
        if (Modifier.DATA !in classDeclaration.modifiers) {
            logger.error("@CopyWith can only be applied to data classes", classDeclaration)
            return
        }
        val primaryConstructor = classDeclaration.primaryConstructor ?: return
        val parameters = primaryConstructor.parameters
        if (parameters.isEmpty()) return
        val packageName = classDeclaration.packageName.asString()
        val containingFile = classDeclaration.containingFile!!
        generateBuilderAndCopyWith(classDeclaration, parameters, packageName, containingFile)
    }

    private data class CollectionInfo(
        val mutableClass: ClassName,
        val toMutable: String,
        val toImmutable: String,
    )

    private fun nestedBuilderClass(type: KSType): ClassName? {
        val declaration = type.declaration
        val isCopyWithAnnotated = declaration.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == CopyWith::class.qualifiedName
        }
        if (!isCopyWithAnnotated) return null
        return ClassName(declaration.packageName.asString(), "${declaration.simpleName.asString()}Builder")
    }

    private fun collectionInfo(type: KSType): CollectionInfo? {
        return when (type.declaration.qualifiedName?.asString()) {
            "kotlin.collections.List" -> CollectionInfo(ClassName("kotlin.collections", "MutableList"), "toMutableList", "toList")
            "kotlin.collections.Set"  -> CollectionInfo(ClassName("kotlin.collections", "MutableSet"),  "toMutableSet",  "toSet")
            "kotlin.collections.Map"  -> CollectionInfo(ClassName("kotlin.collections", "MutableMap"),  "toMutableMap",  "toMap")
            else -> null
        }
    }

    private fun collectionInitBlock(paramName: String, info: CollectionInfo, elementBuilders: List<ClassName?>, isNullable: Boolean): CodeBlock {
        val dot = if (isNullable) "?." else "."
        if (elementBuilders.none { it != null }) return CodeBlock.of("original.%N${dot}%N()", paramName, info.toMutable)
        return when (info.mutableClass.simpleName) {
            "MutableList", "MutableSet" ->
                CodeBlock.of("original.%N${dot}map·{ %T(it) }${dot}%N()", paramName, elementBuilders[0]!!, info.toMutable)
            "MutableMap" -> {
                val keyBuilder = elementBuilders.getOrNull(0)
                val valBuilder = elementBuilders.getOrNull(1)
                when {
                    keyBuilder != null && valBuilder != null ->
                        CodeBlock.of("original.%N${dot}entries${dot}associate·{ %T(it.key) to %T(it.value) }${dot}toMutableMap()", paramName, keyBuilder, valBuilder)
                    valBuilder != null ->
                        CodeBlock.of("original.%N${dot}mapValues·{ %T(it.value) }${dot}toMutableMap()", paramName, valBuilder)
                    else ->
                        CodeBlock.of("original.%N${dot}entries${dot}associate·{ %T(it.key) to it.value }${dot}toMutableMap()", paramName, keyBuilder!!)
                }
            }
            else -> CodeBlock.of("original.%N${dot}%N()", paramName, info.toMutable)
        }
    }

    private fun collectionBuildExpr(fieldName: String, info: CollectionInfo, elementBuilders: List<ClassName?>, isNullable: Boolean): CodeBlock {
        val dot = if (isNullable) "?." else "."
        if (elementBuilders.none { it != null }) {
            return CodeBlock.of("%N.getOrThrow()${dot}%N()", fieldName, info.toImmutable)
        }
        return when (info.mutableClass.simpleName) {
            "MutableList", "MutableSet" ->
                CodeBlock.of("%N.getOrThrow()${dot}map·{ it.build() }${dot}%N()", fieldName, info.toImmutable)
            "MutableMap" -> {
                val keyBuilder = elementBuilders.getOrNull(0)
                val valBuilder = elementBuilders.getOrNull(1)
                when {
                    keyBuilder != null && valBuilder != null ->
                        CodeBlock.of("%N.getOrThrow()${dot}entries${dot}associate·{ it.key.build() to it.value.build() }${dot}toMap()", fieldName)
                    valBuilder != null ->
                        CodeBlock.of("%N.getOrThrow()${dot}mapValues·{ it.value.build() }${dot}toMap()", fieldName)
                    else ->
                        CodeBlock.of("%N.getOrThrow()${dot}entries${dot}associate·{ it.key.build() to it.value }${dot}toMap()", fieldName)
                }
            }
            else -> CodeBlock.of("%N.getOrThrow()${dot}%N()", fieldName, info.toImmutable)
        }
    }

    private data class ParamMeta(
        val name: String,
        val typeName: TypeName,
        val isNullable: Boolean,
        val collectionInfo: CollectionInfo?,
        val mutableType: TypeName?,
        val nestedBuilderClass: ClassName?,
        val elementBuilders: List<ClassName?>?,
    )

    private fun generateBuilderAndCopyWith(
        classDeclaration: KSClassDeclaration,
        parameters: List<KSValueParameter>,
        packageName: String,
        containingFile: KSFile,
    ) {
        val typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()
        val className = classDeclaration.simpleName.asString()
        val classTypeName = classDeclaration.toClassName()
        val builderClassName = ClassName(packageName, "${className}Builder")
        val optionalClass = ClassName("org.pixode.copywith", "Optional")
        val optionalSome = ClassName("org.pixode.copywith", "Optional", "Some")

        // Invariant type params: strip declaration-site variance so the builder can use T in both positions.
        val typeParams = classDeclaration.typeParameters.map { it.toTypeVariableName(typeParamResolver).let { tv -> TypeVariableName(tv.name, tv.bounds) } }
        val parameterizedClassType: TypeName = if (typeParams.isEmpty()) classTypeName else classTypeName.parameterizedBy(typeParams)
        val parameterizedBuilderType: TypeName = if (typeParams.isEmpty()) builderClassName else builderClassName.parameterizedBy(typeParams)

        val paramMetas = parameters.map { param ->
            val resolvedType = param.type.resolve()
            val isNullable = resolvedType.isMarkedNullable
            val typeName = param.type.toTypeName(typeParamResolver)
            val info = collectionInfo(resolvedType)
            val elementBuilders = info?.let {
                resolvedType.arguments.map { a -> a.type?.resolve()?.let { t -> nestedBuilderClass(t) } }
            }
            val mutableType = info?.let { i ->
                val args = resolvedType.arguments.mapIndexed { idx, a ->
                    elementBuilders?.get(idx) ?: a.type?.toTypeName(typeParamResolver) ?: STAR
                }
                val base = i.mutableClass.parameterizedBy(args)
                if (isNullable) base.copy(nullable = true) else base
            }
            val nestedBuilder = if (info == null) nestedBuilderClass(resolvedType) else null
            ParamMeta(param.name!!.asString(), typeName, isNullable, info, mutableType, nestedBuilder, elementBuilders)
        }

        val builderSpec = TypeSpec.classBuilder("${className}Builder")
            .addTypeVariables(typeParams)
            .primaryConstructor(FunSpec.constructorBuilder().addParameter("original", parameterizedClassType).build())
            .addProperty(PropertySpec.builder("original", parameterizedClassType).addModifiers(KModifier.PRIVATE).initializer("original").build())

        paramMetas.forEach { (name, typeName, isNullable, info, mutableType, nestedBuilder, elementBuilders) ->
            val fieldName = "${name}Field"

            val publicType: TypeName = when {
                info != null && mutableType != null -> mutableType
                nestedBuilder != null -> if (isNullable) nestedBuilder.copy(nullable = true) else nestedBuilder
                else -> typeName
            }

            val initializer: CodeBlock
            val getter: FunSpec
            val setter: FunSpec

            when {
                info != null && mutableType != null -> {
                    val initExpr = collectionInitBlock(name, info, elementBuilders ?: emptyList(), isNullable)
                    initializer = CodeBlock.of("%T(%L)", optionalSome, initExpr)
                    getter = FunSpec.getterBuilder()
                        .addCode("return %N.getOrThrow()\n", fieldName)
                        .build()
                    setter = FunSpec.setterBuilder()
                        .addParameter("value", publicType)
                        .addCode("%N = %T(value)\n", fieldName, optionalSome)
                        .build()
                }
                nestedBuilder != null -> {
                    val initExpr = if (isNullable)
                        CodeBlock.of("original.%N?.let·{ %T(it) }", name, nestedBuilder)
                    else
                        CodeBlock.of("%T(original.%N)", nestedBuilder, name)
                    initializer = CodeBlock.of("%T(%L)", optionalSome, initExpr)
                    getter = FunSpec.getterBuilder()
                        .addCode("return %N.getOrThrow()\n", fieldName)
                        .build()
                    setter = FunSpec.setterBuilder()
                        .addParameter("value", publicType)
                        .addCode("%N = %T(value)\n", fieldName, optionalSome)
                        .build()
                }
                else -> {
                    initializer = CodeBlock.of("%T.None", optionalClass)
                    getter = FunSpec.getterBuilder()
                        .addCode("return %N.getOrElse { original.%N }\n", fieldName, name)
                        .build()
                    setter = FunSpec.setterBuilder()
                        .addParameter("value", publicType)
                        .addCode("%N = %T.of(value)\n", fieldName, optionalClass)
                        .build()
                }
            }

            builderSpec.addProperty(
                PropertySpec.builder(fieldName, optionalClass.parameterizedBy(publicType))
                    .addModifiers(KModifier.PRIVATE)
                    .mutable(true)
                    .initializer(initializer)
                    .build()
            )

            builderSpec.addProperty(
                PropertySpec.builder(name, publicType)
                    .mutable(true)
                    .getter(getter)
                    .setter(setter)
                    .build()
            )
        }

        builderSpec.addFunction(
            FunSpec.builder("build")
                .returns(parameterizedClassType)
                .addCode(
                    CodeBlock.builder()
                        .add("return %T(\n", classTypeName)
                        .indent()
                        .apply {
                            paramMetas.forEach { (name, _, isNullable, info, _, nestedBuilder, elementBuilders) ->
                                val fieldName = "${name}Field"
                                when {
                                    info != null ->
                                        add("%N = %L,\n", name, collectionBuildExpr(fieldName, info, elementBuilders ?: emptyList(), isNullable))
                                    nestedBuilder != null -> if (isNullable)
                                        add("%N = %N.getOrThrow()?.build(),\n", name, fieldName)
                                    else
                                        add("%N = %N.getOrThrow().build(),\n", name, fieldName)
                                    else ->
                                        add("%N = %N,\n", name, name)
                                }
                            }
                        }
                        .unindent()
                        .add(")")
                        .build()
                )
                .build()
        )

        val copyWithFunc = FunSpec.builder("copyWith")
            .addTypeVariables(typeParams)
            .receiver(parameterizedClassType)
            .returns(parameterizedClassType)
            .addParameter("block", LambdaTypeName.get(receiver = parameterizedBuilderType, returnType = UNIT))
            .addCode("return %T(this).apply(block).build()", builderClassName)
            .build()

        val toBuilderFunc = FunSpec.builder("toBuilder")
            .addTypeVariables(typeParams)
            .receiver(parameterizedClassType)
            .returns(parameterizedBuilderType)
            .addCode("return %T(this)", builderClassName)
            .build()

        FileSpec.builder(packageName, "${className}CopyWith")
            .addType(builderSpec.build())
            .addFunction(copyWithFunc)
            .addFunction(toBuilderFunc)
            .build()
            .writeTo(codeGenerator, Dependencies(false, containingFile))
    }
}
