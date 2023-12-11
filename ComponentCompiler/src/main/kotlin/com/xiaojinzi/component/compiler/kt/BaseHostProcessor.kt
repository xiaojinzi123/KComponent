package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

abstract class BaseHostProcessor(
    open val environment: SymbolProcessorEnvironment,
    private val logger: KSPLogger = environment.logger,
    val componentModuleName: String = (environment.options["ModuleName"]
        ?: environment.options["HOST"]) ?: throw NULLHOSTEXCEPTION
) : BaseProcessor() {

    companion object {

        val NULLHOSTEXCEPTION = RuntimeException(
            """the host must not be null,you must define host in build.gradle file,such as:
            defaultConfig {
                minSdkVersion 14
                targetSdkVersion 27
                versionCode 1
                versionName "1.0"
            
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments = [HOST: "user"]
                    }
                }
            }"""
        )

    }

    private var isProcessed = false

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn(
            message = "BaseHostProcessor, isProcessed = $isProcessed, object is ${this.javaClass.simpleName}, componentModuleName is $componentModuleName",
        )
        if (!isProcessed) {
            doProcess(resolver = resolver)
            isProcessed = true
        }
        return emptyList()
    }

    abstract fun doProcess(resolver: Resolver): List<KSAnnotated>

}