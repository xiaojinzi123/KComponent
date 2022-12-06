package com.xiaojinzi.component.anno

import com.xiaojinzi.component.support.AttrAutoWireMode

/**
 * 这是一个属性自动注入的注解,表示一切可注入的场景,比如：
 * 1. 路由目标 Activity 的字段
 * 2. Fragment 的字段
 * 如果 [AttrValueAutowiredAnno.value] 是空的, 默认采用属性的名称
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class AttrValueAutowiredAnno(

    /**
     * 需要注入的 Key
     * key 可以是多个
     */
    vararg val value: String,

    /**
     * 注入的模式
     * 当属性的声明是：lateinit var xxx 的时候,
     * 这个 mode 属性值会自动忽略, 会强制使用 [AttrAutoWireMode.Override] 模式
     */
    val mode: AttrAutoWireMode = AttrAutoWireMode.Unspecified

)