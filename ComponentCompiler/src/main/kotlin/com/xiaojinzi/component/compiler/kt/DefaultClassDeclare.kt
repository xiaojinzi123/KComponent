package com.xiaojinzi.component.compiler.kt

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.packageName
import com.xiaojinzi.component.simpleClassName

open class DefaultClassDeclare {

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

}