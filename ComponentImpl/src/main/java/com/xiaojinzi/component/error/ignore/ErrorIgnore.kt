package com.xiaojinzi.component.error.ignore

/**
 * 错误的忽略
 */
object ErrorIgnore {

    /**
     * 如果使用者不想处理错误的话,这些错误都可以被默认忽略
     */
    private val DEFAULT_IGNORE_ERRORS = arrayOf<Class<*>>(
        NavigationException::class.java,
        NavigationCancelException::class.java,
        ActivityResultException::class.java,
        TargetActivityNotFoundException::class.java,
        InterceptorNotFoundException::class.java,
    )

    /**
     * 是否需要忽略
     */
    fun isIgnore(throwable: Throwable): Boolean {
        var currThrowable: Throwable? = throwable
        while (currThrowable != null) {
            for (errorClass in DEFAULT_IGNORE_ERRORS) {
                if (currThrowable.javaClass == errorClass) {
                    return true
                }
            }
            // 拿到 cause,接着判断
            currThrowable = currThrowable.cause
        }
        return false
    }

}