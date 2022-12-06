package com.xiaojinzi.component.anno

import kotlin.reflect.KClass

/**
 * 表示一组条件, 当条件成立, 对应的功能才起作用
 * 目前仅支持混用的是：
 * [ConditionalAnno] 和 [GlobalInterceptorAnno]
 * [ConditionalAnno] 和 [InterceptorAnno]
 */
@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.BINARY)
annotation class ConditionalAnno(

    /**
     * 指定多个条件的类,指定的这些类必须实现 com.xiaojinzi.component.support.Condition 接口
     * 所有都返回了 true 才表示条件成立
     */
    vararg val conditions: KClass<*>

)