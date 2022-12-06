package com.xiaojinzi.component.anno.router

/**
 * 标记一个 Action 是跳转完成的 Action(包括成功和失败不包括取消)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
annotation class AfterRouteEventActionAnno