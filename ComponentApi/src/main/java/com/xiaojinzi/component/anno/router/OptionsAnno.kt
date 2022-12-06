package com.xiaojinzi.component.anno.router

/**
 * 路由跳转的时候,给 Android 跳转添加一个 options
 * 标记的目标类型是 Bundle
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class OptionsAnno 