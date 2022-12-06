package com.xiaojinzi.component.anno

import com.xiaojinzi.component.anno.support.UiThreadCreateAnno

/**
 * 标记一个 Fragment, 方便路由到此注解标记的 Fragment
 */
@UiThreadCreateAnno
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class FragmentAnno(

    /**
     * 这个 Fragment 对应的唯一 ID
     *
     * @return 对应 Fragment 的一个标记, 不能重复
     */
    vararg val value: String

)