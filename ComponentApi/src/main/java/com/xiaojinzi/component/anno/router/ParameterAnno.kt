package com.xiaojinzi.component.anno.router

/**
 * 标记一个元素是一个参数, value 表示参数的名称或者 key
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.BINARY)
annotation class ParameterAnno(

    /**
     * 参数对应的 key
     *
     * @return 参数对应的 key
     */
    val value: String = ""

)