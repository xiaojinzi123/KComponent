package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.packageName
import com.xiaojinzi.component.simpleClassName

fun notSupport(
    message: String? = null,
): Nothing = throw RuntimeException(message ?: "not support")

fun KSAnnotated.getDescName(): String {
    return when (this) {
        is KSClassDeclaration -> {
            qualifiedName?.asString()
        }

        is KSFunctionDeclaration -> {
            qualifiedName?.asString()
        }

        else -> {
            null
        }
    } ?: "not support"
}

fun String.toNullableClassName(): ClassName? {
    return try {
        ClassName(
            packageName = this.packageName(),
            this.simpleClassName(),
        )
        // return ClassName.bestGuess(this)
    } catch (e: Exception) {
        null
    }
}

fun String.toClassName(): ClassName {
    return this.toNullableClassName()!!
}

fun KSClassDeclaration.toClassName(): ClassName {
    return this.qualifiedName!!.asString().toClassName()
}

fun KSFunctionDeclaration.returnTypeToTypeName(): TypeName? {
    return this.returnType?.run {
        this.resolve().let { ksType ->

            ksType.toTypeName()
        }
    }
}

/**
 * 参数的类型不可能为 null
 */
fun KSValueParameter.typeToClassName(): TypeName {
    return this.type.toTypeName()
    // return this.type.resolve().toClassTypeName()!!
}

abstract class BaseProcessor(
    open val environment: SymbolProcessorEnvironment,
    val logger: KSPLogger = environment.logger,
    val codeGenerator: CodeGenerator = environment.codeGenerator,
    val componentModuleName: String = (environment.options["ModuleName"]
        ?: environment.options["HOST"]) ?: throw NULL_HOST_EXCEPTION,
    val logEnable: Boolean = environment.options["LogEnable"]?.toBoolean() ?: false,
) : SymbolProcessor, DefaultClassDeclare() {

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
                    arguments = [ModuleName: "xxx"]
                    arg("KspOptimize", "true")
                    arg("KspOptimizeUniqueName", project.path.md5())
               }
            """.trimIndent()
        )

    }

    lateinit var serializableKsType: KSType
    lateinit var parcelableKsType: KSType
    lateinit var activityKsClassDeclaration: KSClassDeclaration
    lateinit var fragmentKsClassDeclaration: KSClassDeclaration

    private var round = 0

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        round++
        if (round == 1) {
            initProcess(resolver = resolver)
        }
        if (logEnable) {
            logger.warn(
                message = "${this.javaClass.simpleName}, componentModuleName is $componentModuleName, round = $round",
            )
        }
        return roundProcess(
            resolver = resolver,
            round = round,
        ).apply {
            if (logEnable) {
                if (this.isNotEmpty()) {
                    logger.warn(
                        message = "${this.javaClass.simpleName}, componentModuleName is $componentModuleName, round = $round, 有不能处理的注解个数：${this.size}",
                    )
                }
            }
        }
    }

    /**
     * 初始化
     */
    open fun initProcess(resolver: Resolver) {
        serializableKsType =
            resolver.getClassDeclarationByName(name = ComponentConstants.JAVA_SERIALIZABLE)!!
                .asStarProjectedType()
        parcelableKsType =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_PARCELABLE)!!
                .asStarProjectedType()
        activityKsClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_ACTIVITY)!!
        fragmentKsClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_FRAGMENT)!!
    }

    abstract fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated>

    fun getMethodNameFromKsType(
        // 属性的类型, 可能是泛型那种, 也可能可null
        ksType: KSType,
        prefix: String,
    ): String {
        // 外层的对象
        val propertyClassName = ksType.declaration.qualifiedName?.asString()?.toClassName()!!
        return prefix + when (propertyClassName) {
            mClassNameString -> {
                "String"
            }

            mClassNameCharSequence -> {
                "CharSequence"
            }

            mClassNameChar -> {
                "Char"
            }

            mClassNameByte -> {
                "Byte"
            }

            mClassNameShort -> {
                "Short"
            }

            mClassNameInt -> {
                "Int"
            }

            mClassNameLong -> {
                "Long"
            }

            mClassNameFloat -> {
                "Float"
            }

            mClassNameDouble -> {
                "Double"
            }

            mClassNameBoolean -> {
                "Boolean"
            }

            mClassNameByteArray -> {
                "ByteArray"
            }

            mClassNameCharArray -> {
                "CharArray"
            }

            mClassNameShortArray -> {
                "ShortArray"
            }

            mClassNameIntArray -> {
                "IntArray"
            }

            mClassNameLongArray -> {
                "LongArray"
            }

            mClassNameFloatArray -> {
                "FloatArray"
            }

            mClassNameDoubleArray -> {
                "DoubleArray"
            }

            mClassNameBooleanArray -> {
                "BooleanArray"
            }

            mClassNameArrayList, mClassNameArrayListKt -> {
                // 第一个泛型参数
                val ksTypeForGeneric: KSType? =
                    ksType.arguments.firstOrNull()?.type?.resolve()
                val typeClassName =
                    ksTypeForGeneric?.declaration?.qualifiedName?.asString()?.toClassName()
                when {
                    typeClassName == mClassNameString -> {
                        "StringArrayList"
                    }

                    typeClassName == mClassNameCharSequence -> {
                        "CharSequenceArrayList"
                    }

                    typeClassName == mClassNameInt -> {
                        "IntegerArrayList"
                    }

                    ksTypeForGeneric?.let {
                        serializableKsType.isAssignableFrom(
                            that = it
                        )
                    } == true -> {
                        "Serializable"
                    }

                    ksTypeForGeneric?.let {
                        parcelableKsType.isAssignableFrom(
                            that = it
                        )
                    } == true -> {
                        "ParcelableArrayList"
                    }

                    else -> {
                        notSupport(
                            message = "不支持的 ArrayList<$ksTypeForGeneric> 类型",
                        )
                    }
                }
            }

            mClassNameArray, mClassNameArrayKt -> {
                // 第一个泛型参数
                val ksTypeForGeneric: KSType? =
                    ksType.arguments.firstOrNull()?.type?.resolve()
                val typeClassName =
                    ksTypeForGeneric?.declaration?.qualifiedName?.asString()?.toClassName()
                when {
                    typeClassName == mClassNameString -> {
                        "StringArray"
                    }

                    typeClassName == mClassNameCharSequence -> {
                        "CharSequenceArray"
                    }

                    typeClassName == mClassNameByte -> {
                        "ByteArray"
                    }

                    typeClassName == mClassNameChar -> {
                        "CharArray"
                    }

                    typeClassName == mClassNameShort -> {
                        "ShortArray"
                    }

                    typeClassName == mClassNameInt -> {
                        "IntArray"
                    }

                    typeClassName == mClassNameLong -> {
                        "LongArray"
                    }

                    typeClassName == mClassNameFloat -> {
                        "FloatArray"
                    }

                    typeClassName == mClassNameDouble -> {
                        "DoubleArray"
                    }

                    typeClassName == mClassNameBoolean -> {
                        "BooleanArray"
                    }

                    ksTypeForGeneric?.let {
                        parcelableKsType.isAssignableFrom(
                            that = it
                        )
                    } == true -> {
                        "ParcelableArray"
                    }

                    else -> {
                        notSupport(
                            message = "不支持的 Array<$ksTypeForGeneric> 类型",
                        )
                    }
                }
            }

            mClassNameSparseArray -> {
                // 第一个泛型参数
                val ksTypeForGeneric: KSType? =
                    ksType.arguments.firstOrNull()?.type?.resolve()
                when {
                    ksTypeForGeneric?.let {
                        parcelableKsType.isAssignableFrom(
                            that = it
                        )
                    } == true -> {
                        "SparseParcelableArray"
                    }

                    else -> {
                        notSupport(
                            message = "不支持的 SparseArray<$ksTypeForGeneric> 类型",
                        )
                    }
                }
            }

            mClassNameAndroidBundle -> {
                "Bundle"
            }

            else -> {
                when {
                    parcelableKsType.isAssignableFrom(that = ksType.makeNotNullable()) -> {
                        "Parcelable"
                    }

                    serializableKsType.isAssignableFrom(that = ksType.makeNotNullable()) -> {
                        "Serializable"
                    }

                    else -> {
                        notSupport(
                            message = "Unsupported type: $ksType, ${
                                parcelableKsType.isAssignableFrom(
                                    that = ksType.makeNotNullable()
                                )
                            }",
                        )
                    }
                }
            }
        }
    }

}