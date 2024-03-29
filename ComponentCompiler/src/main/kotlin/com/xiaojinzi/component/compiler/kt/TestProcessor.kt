package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

class TestProcessor(
    override val environment: SymbolProcessorEnvironment,
) : BaseProcessor(
    environment = environment,
) {

    private val TAG = "TestProcessor"

    private fun ClassName.withTypeArguments(arguments: List<TypeName>): TypeName {
        return if (arguments.isEmpty()) {
            this
        } else {
            this.parameterizedBy(arguments)
        }
    }

    override fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated> {

        val testClass =
            resolver.getClassDeclarationByName("com.xiaojinzi.component.demo.TestInterface")
        logger.warn("testClass = $testClass")

        testClass?.run {

            val testFunction = this.getAllFunctions().first()
            logger.warn("testFunction = $testFunction")
            logger.warn("testFunction.returnType1 = ${(testFunction.returnType?.resolve())}")

            val testFunctionReturnType2 = testFunction.returnTypeToTypeName() as? ClassName
            logger.warn("testFunction.returnType2 = $testFunctionReturnType2")

            testFunction.returnType?.resolve()?.run {

                when (val dec = this.declaration) {
                    is KSClassDeclaration -> {
                        val testTypeName =
                            dec.toClassName().withTypeArguments(arguments.map { it.toTypeName() })
                        logger.warn("testTypeNamexxxx = $testTypeName, xxx = ${dec.classKind == ClassKind.ANNOTATION_CLASS}")
                    }

                    else -> {}
                }

            }

        }

        return emptyList()

    }

    override fun finish() {
        super.finish()
        logger.warn("$TAG $componentModuleName finish")
    }

    override fun onError() {
        super.onError()
        logger.warn("$TAG $componentModuleName onError")
    }

}

// @AutoService(SymbolProcessorProvider::class)
class TestProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestProcessor(
            environment = environment,
        )
    }

}



