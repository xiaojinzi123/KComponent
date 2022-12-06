package com.xiaojinzi.component.anno.router

/**
 * 标记一个 Action 是跳转失败的 Action
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
annotation class AfterRouteErrorActionAnno