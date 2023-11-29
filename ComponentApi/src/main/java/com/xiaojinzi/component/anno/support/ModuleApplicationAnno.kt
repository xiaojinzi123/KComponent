package com.xiaojinzi.component.anno.support

/**
 * 用于标记所有 Component 生成的 Module 的 Application 类
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.BINARY)
annotation class ModuleApplicationAnno()