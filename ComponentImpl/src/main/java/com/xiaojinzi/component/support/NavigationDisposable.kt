package com.xiaojinzi.component.support

import androidx.annotation.AnyThread
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.impl.Navigator
import com.xiaojinzi.component.impl.RouterRequest
import kotlinx.coroutines.*

/**
 * 调用 [Navigator.navigate]之后拿到的对象,可以在其中拿到请求对象 [RouterRequest]
 * 和 取消这个路由请求
 * note: 如果发起路由的时候由于参数不合格导致 [RouterRequest] 对象构建失败,
 * 这时候会返回一个空实现 [Router.emptyNavigationDisposable] 对象,
 * 这时候调用 [NavigationDisposable.originalRequest] 会得到一个 null 值
 *
 *
 * time   : 2019/01/25
 *
 * @author : xiaojinzi
 */
@CheckClassNameAnno
interface NavigationDisposable {

    /**
     * 是否已经取消
     */
    val isCanceled: Boolean

    /**
     * 取消这个路由,怎么调用都不会出问题
     */
    @AnyThread
    fun cancel()

    /**
     * 空实现,里头都是不能调用的方法
     * 这个对象只会在构建 [RouterRequest] 对象失败或者构建之前就发生错误的情况才会被返回
     * 这里为什么会有这个类是因为在调用 [com.xiaojinzi.component.impl.Call.navigate] 的时候, 需要会返回一个
     */
    object EmptyNavigationDisposable : NavigationDisposable {

        override val isCanceled: Boolean = true

        override fun cancel() {
            // ignore
        }

    }

}

class NavigationDisposableImpl(
    val scope: CoroutineScope,
) : NavigationDisposable {


    override val isCanceled: Boolean
        get() = !scope.isActive

    @AnyThread
    override fun cancel() {
        if (scope.isActive) {
            scope.cancel()
        }
    }

}
