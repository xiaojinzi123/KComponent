package com.xiaojinzi.component.anno.support

/**
 * 1. 如果标记的是一个普通的类, 表示此类会在主线程中被创建
 * 2. 如果标记的是一个注解, 表示被标记的注解标记的所有类都是在主线程中被创建
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.SOURCE)
annotation class UiThreadCreateAnno 