package com.xiaojinzi.component.compiler.kt

/**
 * 声明一个执行过程出现的需要抛出的异常
 * time   : 2019/02/21
 *
 * @author : xiaojinzi
 */
class ProcessException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) :
    RuntimeException(message, cause) {
}