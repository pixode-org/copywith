package org.pixode.copywith

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class CopyWithGenerator(private val codeGenerator: CodeGenerator) {

    private val typeResolver = TypeResolver()

    fun generate(
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

        val typeParams = classDeclaration.typeParameters.map {
            it.toTypeVariableName(typeParamResolver).let { tv -> TypeVariableName(tv.name, tv.bounds) }
        }

        val parameterizedClassType: TypeName
        val parameterizedBuilderType: TypeName
        if (typeParams.isEmpty()) {
            parameterizedClassType = classTypeName
            parameterizedBuilderType = builderClassName
        } else {
            parameterizedClassType = classTypeName.parameterizedBy(typeParams)
            parameterizedBuilderType = builderClassName.parameterizedBy(typeParams)
        }

        val parameters = parameters.map { typeResolver.resolveParameterInfo(it, typeParamResolver) }

        val builderSpec = TypeSpec.classBuilder("${className}Builder")
            .addTypeVariables(typeParams)
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter("original", parameterizedClassType)
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("original", parameterizedClassType)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("original")
                    .build()
            )

        parameters.forEach { parameter ->
            addParameterProperties(parameter, optionalSome, optionalClass, builderSpec)
        }

        builderSpec.addFunction(
            FunSpec.builder("build")
                .returns(parameterizedClassType)
                .addCode(
                    CodeBlock.builder()
                        .add("return %T(\n", classTypeName)
                        .indent()
                        .apply { parameters.forEach { addConstructorArgument(it) } }
                        .unindent()
                        .add(")")
                        .build()
                )
                .build()
        )

        val functionSpecs = getExtensionFunctions(
            typeParams = typeParams,
            parameterizedClassType = parameterizedClassType,
            parameterizedBuilderType = parameterizedBuilderType,
            builderClassName = builderClassName
        )

        FileSpec.builder(packageName, "${className}CopyWith")
            .addType(builderSpec.build())
            .apply { functionSpecs.forEach { addFunction(it) } }
            .build()
            .writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun CodeBlock.Builder.addConstructorArgument(parameter: TypeResolver.ParameterInfo) {
        val name = parameter.name
        val fieldName = "${name}Field"
        when {
            parameter.collectionInfo != null ->
                add("%N = %L,\n",
                    name,
                    getCollectionExpression(
                        fieldName = fieldName,
                        info = parameter.collectionInfo,
                        elementBuilders = parameter.elementBuilders ?: emptyList(),
                        isNullable = parameter.isNullable
                    )
                )

            parameter.nestedBuilderClass != null ->
                if (parameter.isNullable)
                    add("%N = %N.getOrThrow()?.build(),\n", name, fieldName)
                else
                    add("%N = %N.getOrThrow().build(),\n", name, fieldName)

            else ->
                add("%N = %N,\n", name, name)
        }
    }

    private fun addParameterProperties(
        parameterInfo: TypeResolver.ParameterInfo,
        optionalSome: ClassName,
        optionalClass: ClassName,
        builderSpec: TypeSpec.Builder
    ) {
        val (name, typeName, isNullable, info, mutableType, nestedBuilder, elementBuilders) = parameterInfo
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
                initializer = CodeBlock.of("%T(%L)",
                    optionalSome,
                    getCollectionInitializer(name, info, elementBuilders ?: emptyList(), isNullable)
                )
                getter = FunSpec.getterBuilder()
                    .addCode("return %N.getOrThrow()\n", fieldName)
                    .build()
                setter = FunSpec.setterBuilder()
                    .addParameter("value", publicType)
                    .addCode("%N = %T(value)\n", fieldName, optionalSome)
                    .build()
            }

            nestedBuilder != null -> {
                val initializerExpression = if (isNullable) {
                    CodeBlock.of("original.%N?.let·{ %T(it) }", name, nestedBuilder)
                } else {
                    CodeBlock.of("%T(original.%N)", nestedBuilder, name)
                }

                initializer = CodeBlock.of("%T(%L)", optionalSome, initializerExpression)
                getter = FunSpec.getterBuilder().addCode("return %N.getOrThrow()\n", fieldName).build()
                setter = FunSpec.setterBuilder().addParameter("value", publicType)
                    .addCode("%N = %T(value)\n", fieldName, optionalSome).build()
            }

            else -> {
                initializer = CodeBlock.of("%T.None", optionalClass)
                getter =
                    FunSpec.getterBuilder().addCode("return %N.getOrElse { original.%N }\n", fieldName, name).build()
                setter = FunSpec.setterBuilder().addParameter("value", publicType)
                    .addCode("%N = %T.of(value)\n", fieldName, optionalClass).build()
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

    private fun getCollectionInitializer(
        paramName: String,
        info: TypeResolver.CollectionInfo,
        elementBuilders: List<ClassName?>,
        isNullable: Boolean
    ): CodeBlock {
        val dot = if (isNullable) "?." else "."

        if (elementBuilders.none { it != null }) {
            return CodeBlock.of("original.%N${dot}%N()", paramName, info.toMutable)
        }

        return when (info.mutableClass.simpleName) {
            "MutableList", "MutableSet" -> CodeBlock.of(
                "original.%N${dot}map·{ %T(it) }${dot}%N()",
                paramName,
                elementBuilders[0]!!,
                info.toMutable
            )

            "MutableMap" -> {
                val keyBuilder = elementBuilders.getOrNull(0)
                val valBuilder = elementBuilders.getOrNull(1)
                when {
                    keyBuilder != null && valBuilder != null -> CodeBlock.of(
                        "original.%N${dot}entries${dot}associate·{ %T(it.key) to %T(it.value) }${dot}toMutableMap()",
                        paramName,
                        keyBuilder,
                        valBuilder
                    )

                    valBuilder != null -> CodeBlock.of(
                        "original.%N${dot}mapValues·{ %T(it.value) }${dot}toMutableMap()",
                        paramName,
                        valBuilder
                    )

                    else -> CodeBlock.of(
                        "original.%N${dot}entries${dot}associate·{ %T(it.key) to it.value }${dot}toMutableMap()",
                        paramName,
                        keyBuilder!!
                    )
                }
            }

            else -> CodeBlock.of("original.%N${dot}%N()", paramName, info.toMutable)
        }
    }

    private fun getCollectionExpression(
        fieldName: String,
        info: TypeResolver.CollectionInfo,
        elementBuilders: List<ClassName?>,
        isNullable: Boolean
    ): CodeBlock {
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
                    keyBuilder != null && valBuilder != null -> CodeBlock.of(
                        "%N.getOrThrow()${dot}entries${dot}associate·{ it.key.build() to it.value.build() }${dot}toMap()",
                        fieldName
                    )
                    valBuilder != null -> CodeBlock.of(
                        "%N.getOrThrow()${dot}mapValues·{ it.value.build() }${dot}toMap()",
                        fieldName
                    )
                    else -> CodeBlock.of(
                        "%N.getOrThrow()${dot}entries${dot}associate·{ it.key.build() to it.value }${dot}toMap()",
                        fieldName
                    )
                }
            }

            else -> CodeBlock.of("%N.getOrThrow()${dot}%N()", fieldName, info.toImmutable)
        }
    }

    private fun getExtensionFunctions(
        typeParams: List<TypeVariableName>,
        parameterizedClassType: TypeName,
        parameterizedBuilderType: TypeName,
        builderClassName: ClassName
    ): List<FunSpec> = buildList {
        add(
            FunSpec.builder("copyWith")
                .addTypeVariables(typeParams)
                .receiver(parameterizedClassType)
                .returns(parameterizedClassType)
                .addParameter("block", LambdaTypeName.get(receiver = parameterizedBuilderType, returnType = UNIT))
                .addCode("return %T(this).apply(block).build()", builderClassName)
                .build()
        )

        add(
            FunSpec.builder("toBuilder")
                .addTypeVariables(typeParams)
                .receiver(parameterizedClassType)
                .returns(parameterizedBuilderType)
                .addCode("return %T(this)", builderClassName)
                .build()
        )

        val mutableCollectionClass = ClassName("kotlin.collections", "MutableCollection")
        val collectionClass = ClassName("kotlin.collections", "Collection")
        val mutableListClass = ClassName("kotlin.collections", "MutableList")
        val mutableMapClass = ClassName("kotlin.collections", "MutableMap")
        val mapClass = ClassName("kotlin.collections", "Map")
        val kTypeVar = TypeVariableName("K")

        add(
            FunSpec.builder("add")
                .addTypeVariables(typeParams)
                .receiver(mutableCollectionClass.parameterizedBy(parameterizedBuilderType))
                .addParameter("element", parameterizedClassType)
                .returns(BOOLEAN)
                .addCode("return add(%T(element))", builderClassName)
                .build()
        )

        add(
            FunSpec.builder("addAll")
                .addTypeVariables(typeParams)
                .receiver(mutableCollectionClass.parameterizedBy(parameterizedBuilderType))
                .addParameter("elements", collectionClass.parameterizedBy(parameterizedClassType))
                .returns(BOOLEAN)
                .addCode("return addAll(elements.map { %T(it) })", builderClassName)
                .build()
        )

        add(
            FunSpec.builder("set")
                .addModifiers(KModifier.OPERATOR)
                .addTypeVariables(typeParams)
                .receiver(mutableListClass.parameterizedBy(parameterizedBuilderType))
                .addParameter("index", INT)
                .addParameter("element", parameterizedClassType)
                .returns(parameterizedBuilderType)
                .addCode("return set(index, %T(element))", builderClassName)
                .build()
        )

        add(
            FunSpec.builder("set")
                .addModifiers(KModifier.OPERATOR)
                .addTypeVariables(listOf(kTypeVar) + typeParams)
                .receiver(mutableMapClass.parameterizedBy(kTypeVar, parameterizedBuilderType))
                .addParameter("key", kTypeVar)
                .addParameter("value", parameterizedClassType)
                .addCode("return set(key, %T(value))", builderClassName)
                .build()
        )

        add(
            FunSpec.builder("put")
                .addTypeVariables(listOf(kTypeVar) + typeParams)
                .receiver(mutableMapClass.parameterizedBy(kTypeVar, parameterizedBuilderType))
                .addParameter("key", kTypeVar)
                .addParameter("value", parameterizedClassType)
                .returns(parameterizedBuilderType.copy(nullable = true))
                .addCode("return put(key, %T(value))", builderClassName)
                .build()
        )

        add(
            FunSpec.builder("putAll")
                .addTypeVariables(listOf(kTypeVar) + typeParams)
                .receiver(mutableMapClass.parameterizedBy(kTypeVar, parameterizedBuilderType))
                .addParameter(
                    "from",
                    mapClass.parameterizedBy(WildcardTypeName.producerOf(kTypeVar), parameterizedClassType)
                )
                .addCode("return putAll(from.mapValues { (_, v) -> %T(v) })", builderClassName)
                .build()
        )
    }
}
