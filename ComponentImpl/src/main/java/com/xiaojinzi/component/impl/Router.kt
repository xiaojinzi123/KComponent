package com.xiaojinzi.component.impl

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.fragment.app.Fragment
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.cache.ClassCache
import com.xiaojinzi.component.support.ProxyIntentAct
import com.xiaojinzi.component.support.Utils
import java.util.*
import kotlin.reflect.KClass

fun Fragment.withHost(host: String): Navigator {
    return Router.with(this).host(host = host)
}

fun Fragment.withHostAndPath(hostAndPath: String): Navigator {
    return Router.with(this).hostAndPath(hostAndPath = hostAndPath)
}

fun Context.withHost(host: String): Navigator {
    return Router.with(this).host(host = host)
}

fun Context.withHostAndPath(hostAndPath: String): Navigator {
    return Router.with(this).hostAndPath(hostAndPath = hostAndPath)
}

/**
 * 整个路由框架,整体都是在主线程中执行的,在拦截器中提供了 callback 机制
 * 所以有耗时的操作可以在拦截器中去开子线程执行然后在回调中继续下一个拦截器
 * 这个类必须放在 [ComponentUtil.IMPL_OUTPUT_PKG] 包下面
 * 这个类作为框架对外的一个使用的类,里面会很多易用的方法
 *
 * time   : 2018/07/26
 * @author : xiaojinzi
 */
@CheckClassNameAnno
object Router {

    /**
     * 类的标志
     */
    const val TAG = "-------- Router --------"

    /**
     * 路由的监听器
     */
    val routerListeners: MutableCollection<RouterListener> =
        Collections.synchronizedCollection(ArrayList(0))

    fun clearRouterListeners() {
        routerListeners.clear()
    }

    fun addRouterListener(listener: RouterListener) {
        if (routerListeners.contains(listener)) {
            return
        }
        routerListeners.add(listener)
    }

    fun removeRouterListener(listener: RouterListener) {
        routerListeners.remove(listener)
    }

    @AnyThread
    fun newProxyIntentBuilder(): ProxyIntentBuilder {
        return ProxyIntentBuilder()
    }

    @AnyThread
    fun with(fragmentFlag: String): FragmentNavigator {
        return FragmentNavigator(fragmentFlag)
    }

    /**
     * 空参数的默认会使用 [com.xiaojinzi.component.Component.getApplication()] 来跳转,
     * 所以空参数的这种不能够用来获取 [com.xiaojinzi.component.bean.ActivityResult]
     * 同时用户在自定义拦截器的时候, 也要注意 [Context] 未必是一个 [Activity]
     * 所以使用者请注意了, 此方法在明确有 [Activity] 可以拿到的时候请务必使用
     * [Context] 方法或者 [Router.with)]
     * 此方法虽然你可以在任何时候用, 但是作者建议一定要在拿不到 [Activity] 和 [Fragment]
     * 的时候去用, 而不是随便用
     *
     * @return 返回一个路由的 Builder
     */
    @AnyThread
    fun with(): Navigator {
        return Navigator()
    }

    @AnyThread
    fun with(context: Context?): Navigator {
        return Navigator(context = context)
    }

    @AnyThread
    fun with(fragment: Fragment?): Navigator {
        return Navigator(fragment = fragment)
    }

    /**
     * 拿到一个接口的实现类
     *
     * @param apiClass 路由接口 Api class
     * @param <T>      路由接口 Api class 的实例对象
     * @return 路由接口 Api class 的实例对象
     */
    @AnyThread
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> withApi(apiClass: KClass<T>): T {
        var t: T? = ClassCache.get(clazz = apiClass) as? T
        if (t == null) {
            val className: String = ComponentUtil.genRouterApiImplClassName(apiClass.java)
            try {
                t = Class.forName(className).newInstance() as T
                ClassCache.put(apiClass, t)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return t
    }

    /**
     * 是否有代理的 [android.content.Intent]
     */
    fun isProxyIntentExist(bundle: Bundle?): Boolean {
        if (bundle == null) {
            return false
        }
        return bundle.getBoolean(ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT)
    }

}

fun <T : Any> KClass<T>.routeApi(): T {
    return Router.withApi(apiClass = this)
}
