package com.xiaojinzi.component.anno.router

/**
 * 给 Intent 添加一些 category
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.BINARY)
annotation class CategoryAnno(

    /**
     * 表示 Intent 需要添加的 Category
     */
    vararg val value: String

)