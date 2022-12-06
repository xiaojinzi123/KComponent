package com.xiaojinzi.component.anno

/**
 * 全局拦截器的注解,用这个注解的拦截器在 App 启动的时候就会被加载到拦截器列表中
 * 可以拦截到所有的路由请求
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.BINARY)
annotation class GlobalInterceptorAnno(

    /**
     * 定义优先级, 值越大优先级越高
     */
    val priority: Int = 0

)