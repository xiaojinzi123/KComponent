package com.xiaojinzi.component.anno.router

/**
 * 表示这个路由会直接使用这个 url 进行路由
 */
@Target(
    AnnotationTarget.FUNCTION,
)
@Retention(AnnotationRetention.BINARY)
annotation class UrlAnno(

    /**
     * url 的值
     */
    val value: String

)