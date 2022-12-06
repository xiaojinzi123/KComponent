package com.xiaojinzi.component.impl.interceptor

import com.xiaojinzi.component.impl.RouterInterceptor
import kotlin.reflect.KClass

/**
 * time   : 2018/12/26
 *
 * @author : xiaojinzi
 */
data class InterceptorBean(
    /**
     * 拦截器
     */
    val interceptor: KClass<out RouterInterceptor>,
    /**
     * 优先级
     */
    val priority: Int
)