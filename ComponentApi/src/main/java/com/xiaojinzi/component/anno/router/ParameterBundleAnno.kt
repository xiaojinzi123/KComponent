package com.xiaojinzi.component.anno.router

/**
 * 标记一个元素是一个 Bundle 参数
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.BINARY)
annotation class ParameterBundleAnno