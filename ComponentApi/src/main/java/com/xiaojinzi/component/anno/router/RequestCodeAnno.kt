package com.xiaojinzi.component.anno.router

/**
 * 如果标识某一个方法表示 RequestCode 是什么
 * 如果标记某一个方法中的参数,那么那个参数的值就是requestCode的值
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.BINARY)
annotation class RequestCodeAnno(

    /**
     * requestCode 的值
     * 如果标记在方法上 value 值没有表示随机生成一个
     * 如果标记在参数上,则 value 的值不起作用
     */
    val value: Int = RANDOM_REQUEST_CODE

) {

    companion object {

        const val RANDOM_REQUEST_CODE = Int.MIN_VALUE

    }

}