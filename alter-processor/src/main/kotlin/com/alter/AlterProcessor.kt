package com.alter

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class AlterProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Alter::class.qualifiedName!!)
        val unprocessed = symbols.filter { !it.validate() }.toList()

        symbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .forEach { generateAlterFunction(it) }

        return unprocessed
    }

    private fun generateAlterFunction(classDeclaration: KSClassDeclaration) {
        if (Modifier.DATA !in classDeclaration.modifiers) {
            logger.error("@Alter can only be applied to data classes", classDeclaration)
            return
        }

        val primaryConstructor = classDeclaration.primaryConstructor
        if (primaryConstructor == null) {
            logger.error("@Alter data class must have a primary constructor", classDeclaration)
            return
        }

        val parameters = primaryConstructor.parameters
        if (parameters.isEmpty()) return

        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val typeParameters = classDeclaration.typeParameters
        val typeParamResolver = typeParameters.toTypeParameterResolver()
        val typeVariables = typeParameters.map { it.toTypeVariableName(typeParamResolver) }

        val receiverType = if (typeVariables.isEmpty()) {
            classDeclaration.toClassName()
        } else {
            classDeclaration.toClassName().parameterizedBy(typeVariables)
        }

        val funBuilder = FunSpec.builder("alter")
            .receiver(receiverType)
            .returns(receiverType)

        typeVariables.forEach { funBuilder.addTypeVariable(it) }

        parameters.forEach { param ->
            val paramName = param.name!!.asString()
            val paramType = param.type.toTypeName(typeParamResolver)
            funBuilder.addParameter(
                ParameterSpec.builder(paramName, paramType)
                    .defaultValue("this.%N", paramName)
                    .build()
            )
        }

        val returnBlock = CodeBlock.builder()
            .add("return %T(\n", classDeclaration.toClassName())
            .indent()
            .apply {
                parameters.forEach { param ->
                    add("%N = %N,\n", param.name!!.asString(), param.name!!.asString())
                }
            }
            .unindent()
            .add(")")
            .build()

        funBuilder.addCode(returnBlock)

        val fileSpec = FileSpec.builder(packageName, "${className}Alter")
            .addFunction(funBuilder.build())
            .build()

        fileSpec.writeTo(codeGenerator, Dependencies(false, classDeclaration.containingFile!!))
    }
}
