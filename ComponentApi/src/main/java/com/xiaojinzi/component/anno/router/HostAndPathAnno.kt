package com.xiaojinzi.component.anno.router

/**
 * 表示一个 hostAndPath
 *
 * @see SchemeAnno
 * @see UserInfoAnno
 * @see HostAnno
 * @see PathAnno
 * @see HostAndPathAnno
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class HostAndPathAnno(

    /**
     * 表示路由的 host + path
     */
    val value: String

)