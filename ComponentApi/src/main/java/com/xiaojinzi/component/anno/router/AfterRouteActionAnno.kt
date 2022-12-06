package com.xiaojinzi.component.anno.router

/**
 * 标记一个 Action 是跳转后的 Action
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class AfterRouteActionAnno