package com.xiaojinzi.component.anno.router

/**
 * 给 Intent 添加一些 flag
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.BINARY)
annotation class FlagAnno(

    /**
     * 表示 Intent 需要添加的 Flag
     */
    vararg val value: Int

)