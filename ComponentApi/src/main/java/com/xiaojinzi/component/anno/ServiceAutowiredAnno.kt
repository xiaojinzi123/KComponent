package com.xiaojinzi.component.anno

/**
 * 这是一个自动注入的注解,表示一切可注入的场景,比如：
 * 1.路由目标 Activity 的字段
 * 2.路由目标 Activity 的跨组件的服务
 */
@Target(
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.BINARY)
annotation class ServiceAutowiredAnno(

    val name: String = ""

)