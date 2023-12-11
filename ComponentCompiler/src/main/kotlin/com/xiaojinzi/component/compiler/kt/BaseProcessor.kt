package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
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
        this.resolve().let {  ksType ->

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

abstract class BaseProcessor : SymbolProcessor {

    val mClassNameSparseArray: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_SPARSEARRAY.packageName(),
        ComponentConstants.ANDROID_SPARSEARRAY.simpleClassName(),
    )

    val mClassNameParcelable: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_PARCELABLE.packageName(),
        ComponentConstants.ANDROID_PARCELABLE.simpleClassName(),
    )

    val mClassNameConditionCache: ClassName = ClassName(
        packageName = ComponentConstants.CONDITIONCACHE_CLASS_NAME.packageName(),
        ComponentConstants.CONDITIONCACHE_CLASS_NAME.simpleClassName(),
    )

    val mClassNameRouter: ClassName = ClassName(
        packageName = ComponentConstants.ROUTER_CLASS_NAME.packageName(),
        ComponentConstants.ROUTER_CLASS_NAME.simpleClassName(),
    )

    val mClassNameRxRouter: ClassName = ClassName(
        packageName = ComponentConstants.ROUTER_RX_CLASS_NAME.packageName(),
        ComponentConstants.ROUTER_RX_CLASS_NAME.simpleClassName(),
    )

    val mClassNameSupportCallable: ClassName = ClassName(
        packageName = ComponentConstants.CALLABLE_CLASS_NAME.packageName(),
        ComponentConstants.CALLABLE_CLASS_NAME.simpleClassName(),
    )

    val mClassNameSupportSingletonCallable: ClassName = ClassName(
        packageName = ComponentConstants.SINGLETON_CALLABLE_CLASS_NAME.packageName(),
        ComponentConstants.SINGLETON_CALLABLE_CLASS_NAME.simpleClassName(),
    )

    val mClassNameIntent: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_INTENT.packageName(),
        ComponentConstants.ANDROID_INTENT.simpleClassName(),
    )

    val mClassNameClass: ClassName = ClassName(
        packageName = ComponentConstants.JAVA_CLASS.packageName(),
        ComponentConstants.JAVA_CLASS.simpleClassName(),
    )

    val mClassNameList: ClassName = ClassName(
        packageName = ComponentConstants.JAVA_LIST.packageName(),
        ComponentConstants.JAVA_LIST.simpleClassName(),
    )

    val mClassNameMapKt: ClassName = ClassName(
        packageName = ComponentConstants.KOTLIN_MAP.packageName(),
        ComponentConstants.KOTLIN_MAP.simpleClassName(),
    )

    val mClassNameListKt: ClassName = ClassName(
        packageName = ComponentConstants.KOTLIN_LIST.packageName(),
        ComponentConstants.KOTLIN_LIST.simpleClassName(),
    )

    val mClassNameAndroidKeepAnno: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_ANNOTATION_KEEP.packageName(),
        ComponentConstants.ANDROID_ANNOTATION_KEEP.simpleClassName(),
    )

    val mClassNameAndroidApplication: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_APPLICATION.packageName(),
        ComponentConstants.ANDROID_APPLICATION.simpleClassName(),
    )

    val mClassNameAndroidBundle: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_BUNDLE.packageName(),
        ComponentConstants.ANDROID_BUNDLE.simpleClassName(),
    )

    val mClassNameFragmentManager: ClassName = ClassName(
        packageName = ComponentConstants.FRAGMENT_MANAGER_CALL_CLASS_NAME.packageName(),
        ComponentConstants.FRAGMENT_MANAGER_CALL_CLASS_NAME.simpleClassName(),
    )

    val mClassNameFunction1: ClassName = ClassName(
        packageName = ComponentConstants.FUNCTION1_CLASS_NAME.packageName(),
        ComponentConstants.FUNCTION1_CLASS_NAME.simpleClassName(),
    )

    val mClassNameKeep: ClassName = ClassName(
        packageName = ComponentConstants.ANDROID_ANNOTATION_KEEP.packageName(),
        ComponentConstants.ANDROID_ANNOTATION_KEEP.simpleClassName(),
    )

    val mClassNameComponentGeneratedAnno: ClassName = ClassName(
        packageName = ComponentConstants.COMPONENT_GENERATED_ANNO_CLASS_NAME.packageName(),
        ComponentConstants.COMPONENT_GENERATED_ANNO_CLASS_NAME.simpleClassName(),
    )

    val mClassNameParameterSupport: ClassName = ClassName(
        packageName = ComponentConstants.PARAMETERSUPPORT_CLASS_NAME.packageName(),
        ComponentConstants.PARAMETERSUPPORT_CLASS_NAME.simpleClassName(),
    )

    val mClassNameServiceManager: ClassName = ClassName(
        packageName = ComponentConstants.SERVICE_MANAGER_CLASS_NAME.packageName(),
        ComponentConstants.SERVICE_MANAGER_CLASS_NAME.simpleClassName(),
    )

    val mClassNameComponent: ClassName = ClassName(
        packageName = ComponentConstants.COMPONENT_CLASS_NAME.packageName(),
        ComponentConstants.COMPONENT_CLASS_NAME.simpleClassName(),
    )

    // Kotlin 原生 和 基础类型 --------------------start-------------------
    val mClassNameBoolean: ClassName = Boolean::class.asClassName()
    val mClassNameDouble: ClassName = Double::class.asClassName()
    val mClassNameFloat: ClassName = Float::class.asClassName()
    val mClassNameLong: ClassName = Long::class.asClassName()
    val mClassNameInt: ClassName = Int::class.asClassName()
    val mClassNameShort: ClassName = Short::class.asClassName()
    val mClassNameByte: ClassName = Byte::class.asClassName()
    val mClassNameChar: ClassName = Char::class.asClassName()
    val mClassNameString: ClassName = String::class.asClassName()
    val mClassNameCharSequence: ClassName = CharSequence::class.asClassName()
    val mClassNameIntArray: ClassName = IntArray::class.asClassName()
    val mClassNameLongArray: ClassName = LongArray::class.asClassName()
    val mClassNameByteArray: ClassName = ByteArray::class.asClassName()
    val mClassNameCharArray: ClassName = CharArray::class.asClassName()
    val mClassNameShortArray: ClassName = ShortArray::class.asClassName()
    val mClassNameFloatArray: ClassName = FloatArray::class.asClassName()
    val mClassNameDoubleArray: ClassName = DoubleArray::class.asClassName()
    val mClassNameBooleanArray: ClassName = BooleanArray::class.asClassName()
    val mClassNameArray: ClassName = Array::class.asClassName()
    val mClassNameArrayKt: ClassName? = "kotlin.Array".toNullableClassName()
    val mClassNameArrayList: ClassName = ArrayList::class.asClassName()
    val mClassNameArrayListKt: ClassName? = "kotlin.collections.ArrayList".toNullableClassName()
    // Kotlin 原生 和 基础类型 --------------------end-------------------

    fun getMethodNameFromKsType(
        resolver: Resolver,
        // 属性的类型, 可能是泛型那种, 也可能可null
        ksType: KSType,
        prefix: String,
    ): String {

        // 外层的对象
        val propertyClassName = ksType.declaration.qualifiedName?.asString()?.toClassName()!!
        val serializableKsType =
            resolver.getClassDeclarationByName(name = ComponentConstants.JAVA_SERIALIZABLE)!!
                .asStarProjectedType()
        val parcelableKsType =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_PARCELABLE)!!
                .asStarProjectedType()

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