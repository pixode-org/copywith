package org.pixode.copywith

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

class CopyWithProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val generator = CopyWithGenerator(codeGenerator)

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
        generator.generate(classDeclaration, parameters, packageName, containingFile)
    }
}
