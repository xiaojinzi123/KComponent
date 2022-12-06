package com.xiaojinzi.component.bean

import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.anno.support.NeedOptimizeAnno
import com.xiaojinzi.component.impl.RouterDegrade
import kotlin.reflect.KClass

@CheckClassNameAnno
data class RouterDegradeBean(

    /**
     * 优先级
     */
    val priority: Int = 0,
    /**
     * 这个目标 Activity Class,可能为空,因为可能标记在静态方法上
     */
    val targetClass: KClass<out RouterDegrade>? = null,

)
