package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

abstract class BaseHostProcessor(
    open val environment: SymbolProcessorEnvironment,
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
        if (isProcessed) {
        } else {
            isProcessed = true
            doProcess(resolver = resolver)
        }
        return emptyList()
    }

    abstract fun doProcess(resolver: Resolver): List<KSAnnotated>

}