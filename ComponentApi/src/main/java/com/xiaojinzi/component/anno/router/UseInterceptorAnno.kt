package com.xiaojinzi.component.anno.router

import kotlin.reflect.KClass

/**
 * 对于接口中的路有方法来说,使用一组拦截器
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class UseInterceptorAnno(

    /**
     * 拦截器的 class
     */
    vararg val classes: KClass<*> = [],

    /**
     * 拦截器的名称
     */
    val names: Array<String> = []

)