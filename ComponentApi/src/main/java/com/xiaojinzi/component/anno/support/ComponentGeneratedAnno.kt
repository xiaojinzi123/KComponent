package com.xiaojinzi.component.anno.support

/**
 * 用于标记所有 Component 生成的类. 自动生成的类会带上此注解
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ComponentGeneratedAnno(val value: String = "")