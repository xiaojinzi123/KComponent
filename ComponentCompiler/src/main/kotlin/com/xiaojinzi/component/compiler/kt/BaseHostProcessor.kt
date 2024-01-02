package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

abstract class BaseHostProcessor(
    open val environment: SymbolProcessorEnvironment,
    val logger: KSPLogger = environment.logger,
    val codeGenerator: CodeGenerator = environment.codeGenerator,
    val componentModuleName: String = (environment.options["ModuleName"]
        ?: environment.options["HOST"]) ?: throw NULL_HOST_EXCEPTION,
    val logEnable: Boolean = environment.options["LogEnable"]?.toBoolean() ?: false,
    val kspOptimize: Boolean = environment.options["KspOptimize"]?.toBoolean()
        ?: false,
) : BaseProcessor() {

    companion object {

        val NULL_HOST_EXCEPTION = RuntimeException(
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

    private var round = 0

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        round++
        if (logEnable) {
            logger.warn(
                message = "BaseHostProcessor, round = $round, object is ${this.javaClass.simpleName}, componentModuleName is $componentModuleName",
            )
        }
        if (round == 1) {
            initProcess(resolver = resolver)
            roundProcess(
                resolver = resolver,
                round = round,
            )
        }
        return emptyList()
    }

    /**
     * 初始化
     */
    open fun initProcess(resolver: Resolver) {
    }

    abstract fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated>

}