package com.xiaojinzi.component.anno.support

/**
 * 表示一个资源不能在项目中使用, 是框架内部使用的
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotAppUseAnno(val value: String = "")