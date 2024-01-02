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
    val kspOptimizeUniqueName: String? = environment.options["KspOptimizeUniqueName"],
) : BaseProcessor() {

    companion object {

        val NULL_HOST_EXCEPTION = RuntimeException(
            """host 或者 moduleName 不能为空, 你必须在 build.gradle 中定义, 比如:
            defaultConfig {
                minSdkVersion 14
                targetSdkVersion 27
                versionCode 1
                versionName "1.0"
            
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments = [HOST: "user"]
                        // 或者
                        arguments = [ModuleName: "user"]
                    }
                }
            }""".trimIndent()
        )

        val YOU_SHOULD_RERUN_EXCEPTION = RuntimeException(
            """
               在 Android Studio 运行的时候, 有时候 ksp 并不能扫描到被标记的类.
               这个属于 ksp 还是 gradle 的问题也不是很清楚. 
               当你看到这个错误的时候, 请 clean 项目重新运行
            """.trimIndent()
        )

        val YOU_SHOULD_CONFIG_KSP_OPTIMIZE_UNIQUE_NAME_EXCEPTION = RuntimeException(
            """
               尝试添加参数 KspOptimizeUniqueName 参数指定一个唯一的名称, 建议下面的方式
               ksp {
                    arguments = [ModuleName: "user"]
                    arg("KspOptimize", "true")
                    arg("KspOptimizeUniqueName", project.rootProject.path.md5())
               }
            """.trimIndent()
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