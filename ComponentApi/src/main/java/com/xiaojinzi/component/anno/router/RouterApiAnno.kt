package com.xiaojinzi.component.anno.router

/**
 * 这是一个标识某一个接口是一个路由接口的注解
 * 注解生成器会解析接口上的所有方法, 生成一个实现类
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class RouterApiAnno