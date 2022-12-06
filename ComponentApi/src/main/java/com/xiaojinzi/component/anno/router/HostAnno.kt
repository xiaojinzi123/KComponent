package com.xiaojinzi.component.anno.router

/**
 * 表示 host
 *
 * @see SchemeAnno
 * @see UserInfoAnno
 * @see HostAnno
 * @see PathAnno
 * @see HostAndPathAnno
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class HostAnno(val value: String)