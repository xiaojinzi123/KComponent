package com.xiaojinzi.component.error.ignore

import com.xiaojinzi.component.impl.RouterRequest

/**
 * 表示路由的过程中发生异常
 *
 * time   : 2018/11/03
 *
 * @author : xiaojinzi
 */
open class NavigationException(
    message: String? = null, cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 表示路由的过程中取消了
 *
 * time   : 2018/11/03
 *
 * @author : xiaojinzi
 */
class NavigationCancelException(
    message: String? = null, cause: Throwable? = null,
    val originalRequest: RouterRequest? = null,
) : NavigationException(
    message = message,
    cause = cause,
)

/**
 * 表示目标界面没有找到
 *
 * time   : 2018/11/11
 *
 * @author : xiaojinzi
 */
class TargetActivityNotFoundException(
    message: String? = null
) : NavigationException(
    message = message,
)