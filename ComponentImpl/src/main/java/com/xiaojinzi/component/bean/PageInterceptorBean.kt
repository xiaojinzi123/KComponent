package com.xiaojinzi.component.bean

import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.impl.RouterInterceptor
import kotlin.reflect.KClass

/**
 * [.stringInterceptor] 和 [.classInterceptor] 必须有一个是有值的
 */
@CheckClassNameAnno
class PageInterceptorBean constructor(
    /**
     * 优先级
     */
    val priority: Int,
    val interceptorName: String? = null,
    val interceptorClass: KClass<out RouterInterceptor>? = null,
)