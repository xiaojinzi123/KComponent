package com.xiaojinzi.component.anno.router

/**
 * 标记一个方法的跳转类型. 默认可省略
 * 返回值支持以下的类型:
 * 1. void
 * 2. [com.xiaojinzi.component.ComponentConstants.NAVIGATOR_CLASS_NAME]
 * 3. [com.xiaojinzi.component.ComponentConstants.RXJAVA_COMPLETABLE]
 * 4. [com.xiaojinzi.component.ComponentConstants.RXJAVA_SINGLE]
 * 此注解省略和 @NavigateAnno 是一样的
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class NavigateAnno(

    /**
     * 为了拿 ActivityResult
     */
    val forResult: Boolean = false,

    /**
     * 为了拿 Intent, 可以搭配 [resultCodeMatch] 属性使用
     */
    val forIntent: Boolean = false,

    /**
     * 为了那 resultCode
     */
    val forResultCode: Boolean = false,

    /**
     * 当你使用了 [.forIntent]的时候,
     * 你可以使用这个属性匹配 ResultCode
     * [Int.MIN_VALUE] 表示不启用
     */
    val resultCodeMatch: Int = Int.MIN_VALUE

)

val NavigateAnno.resultCodeMatchValid: Boolean
    get() = resultCodeMatch != Int.MIN_VALUE