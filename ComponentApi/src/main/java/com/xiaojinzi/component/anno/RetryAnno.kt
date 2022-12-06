package com.xiaojinzi.component.anno

import com.xiaojinzi.component.anno.support.UiThreadCreateAnno

/**
 * 重试注解. 目前没有生效的地方
 */
@UiThreadCreateAnno
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RetryAnno(
    /**
     * 这个服务对应的接口
     */
    val value: Int = 1
)