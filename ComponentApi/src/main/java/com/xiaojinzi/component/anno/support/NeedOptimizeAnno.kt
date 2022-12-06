package com.xiaojinzi.component.anno.support

/**
 * 表示被标记的是需要优化的
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.SOURCE)
annotation class NeedOptimizeAnno(
    /**
     * 进行描述
     */
    val value: String = ""
)