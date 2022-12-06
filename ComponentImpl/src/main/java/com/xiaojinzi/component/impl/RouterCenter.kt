package com.xiaojinzi.component.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.xiaojinzi.component.Component
import com.xiaojinzi.component.Component.requiredConfig
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.anno.support.NotAppUseAnno
import com.xiaojinzi.component.bean.RouterBean
import com.xiaojinzi.component.error.ignore.InterceptorNotFoundException
import com.xiaojinzi.component.error.ignore.NavigationCancelException
import com.xiaojinzi.component.error.ignore.NavigationException
import com.xiaojinzi.component.error.ignore.TargetActivityNotFoundException
import com.xiaojinzi.component.impl.interceptor.InterceptorCenter
import com.xiaojinzi.component.support.*
import java.util.*
import java.util.regex.Pattern

data class ModuleRouterBean(
    val moduleName: String,
    val routerMap: List<RouterBean>,
)

/**
 * 请注意:
 * 请勿在项目中使用此类, 此类的 Api 不供项目使用, 仅供框架内部使用.
 * 即使你在项目中能引用到此类并且调用到 Api, 也不是你想要的效果. 所以请不要使用.
 * 尤其是方法 [.isMatchUri]
 *
 *
 * 中央路由,挂载着多个子路由表,这里有总路由表
 * 实际的跳转也是这里实现的,当有模块的注册和反注册发生的时候
 * 总路由表会有响应的变化
 *
 * @author xiaojinzi
 * @hide
 */
@NotAppUseAnno
@CheckClassNameAnno
object RouterCenter {

    /**
     * 子路由表对象
     */
    private val hostRouterMap: MutableMap<String, ModuleRouterBean> = HashMap()

    /**
     * 保存映射关系的正则匹配的 map 集合
     */
    private val routerRegExMap: MutableMap<String, RouterBean> = HashMap()

    /**
     * 保存映射关系的map集合
     */
    private val routerMap: MutableMap<String, RouterBean> = HashMap()

    @Synchronized
    fun isMatchUri(uri: Uri): Boolean {
        return getTarget(uri) != null
    }

    fun isSameTarget(uri1: Uri, uri2: Uri): Boolean {
        return getTarget(uri1) === getTarget(uri2)
    }

    @UiThread
    @Throws(TargetActivityNotFoundException::class)
    fun openUri(routerRequest: RouterRequest): Intent? {
        return doOpenUri(routerRequest)
    }

    /**
     * @param request             路由请求对象
     * @param routerDegradeIntent 一个降级的 Intent
     */
    @UiThread
    @Throws(Exception::class)
    fun routerDegrade(request: RouterRequest, routerDegradeIntent: Intent?): Intent? {
        if (!Utils.isMainThread()) {
            throw NavigationException("routerDegrade must run on main thread")
        }
        val uriString = request.uri.toString()
        if (routerDegradeIntent == null) {
            throw TargetActivityNotFoundException(uriString)
        }
        return doStartIntent(request, routerDegradeIntent)
    }

    /**
     * content 参数和 fragment 参数必须有一个有值的
     *
     * @param request 路由请求对象
     */
    @UiThread
    @Throws(TargetActivityNotFoundException::class)
    private fun doOpenUri(request: RouterRequest): Intent? {
        if (!Utils.isMainThread()) {
            throw NavigationException("doOpenUri must run on main thread")
        }
        // 参数检测完毕
        val target = getTarget(request.uri)
        // router://component1/test?data=xxxx
        val uriString = request.uri.toString()
        // 没有找到目标界面
        if (target == null) {
            throw TargetActivityNotFoundException(uriString)
        }
        if (request.context == null && request.fragment == null) {
            throw NavigationException("one of the Context and Fragment must not be null,do you forget call method: \nRouter.with(Context) or Router.with(Fragment)")
        }
        // 获取关联的 Context, 如果获取不到, 那就是界面不行了
        val rawContext = if (request.autoCancel) {
            request.rawAliveContext
        } else {
            request.rawContext
        } ?: throw NavigationCancelException(
            message = "is your fragment or Activity is Destroyed?".trimIndent()
        )
        // 如果 Context 和 Fragment 中的 Context 都是 null
        var intent: Intent? = null
        if (target.targetClass != null) {
            intent = Intent(rawContext, target.targetClass!!.java)
        } else if (target.customerIntentCall != null) {
            intent = target.customerIntentCall!!.get(request = request)
        }
        if (intent == null) {
            throw TargetActivityNotFoundException(uriString)
        }
        return doStartIntent(request, intent)
    }

    /**
     * 拿到 Intent 之后真正的跳转
     *
     * @param request 请求对象
     * @param intent  Intent
     * @return 如果是为了返回 Inten 来的
     */
    @UiThread
    @Throws(Exception::class)
    private fun doStartIntent(
        request: RouterRequest,
        intent: Intent
    ): Intent? {
        // 前置工作
        intent.putExtras(request.bundle)
        // 把用户的 flags 和 categories 都设置进来
        for (intentCategory in request.intentCategories) {
            intent.addCategory(intentCategory)
        }
        for (intentFlag in request.intentFlags) {
            intent.addFlags(intentFlag)
        }
        if (request.intentConsumer != null) {
            request.intentConsumer.accept(intent)
        }
        if (request.context is Application &&
            requiredConfig().isTipWhenUseApplication
        ) {
            LogUtil.logw(
                Router.TAG,
                "you use 'Application' to launch Activity. this is not recommended. you should not use 'Application' as far as possible"
            )
        }
        request.executeBeforeStartActivityAction()

        // ------------------------------- 启动界面核心代码 ------------------------------- START
        if (request.isForTargetIntent) {
            return intent
        }

        // 如果是普通的启动界面
        if (request.isForResult) { // 如果是 startActivity
            // 使用 context 跳转 startActivityForResult
            if (request.context != null) {
                val rxFragment = findFragment(request.context)
                var rawAct: Activity?
                when {
                    rxFragment != null -> {
                        rxFragment.startActivityForResult(
                            intent,
                            request.requestCode!!,
                            request.options
                        )
                    }
                    Utils.getActivityFromContext(request.context).also { rawAct = it } != null -> {
                        rawAct!!.startActivityForResult(
                            intent,
                            request.requestCode!!,
                            request.options
                        )
                    }
                    else -> {
                        throw NavigationException("Context is not a Activity,so can't use 'startActivityForResult' method")
                    }
                }
            } else if (request.fragment != null) { // 使用 Fragment 跳转
                val rxFragment = findFragment(request.fragment)
                if (rxFragment != null) {
                    rxFragment.startActivityForResult(
                        intent,
                        request.requestCode!!,
                        request.options
                    )
                } else {
                    request.fragment.startActivityForResult(
                        intent,
                        request.requestCode!!,
                        request.options
                    )
                }
            } else {
                throw NavigationException("the context or fragment both are null")
            }
        } else { // 不想要框架来获取 activityResult
            // 普通跳转
            if (request.requestCode == null) {
                when {
                    request.context != null -> {
                        request.context.startActivity(intent, request.options)
                    }
                    request.fragment != null -> {
                        request.fragment.startActivity(intent, request.options)
                    }
                    else -> {
                        throw NavigationException("the context or fragment both are null")
                    }
                }
            } else { // startActivityForResult
                var rawAct: Activity? = null
                when {
                    Utils.getActivityFromContext(request.context).also { rawAct = it } != null -> {
                        rawAct!!.startActivityForResult(
                            intent,
                            request.requestCode,
                            request.options
                        )
                    }
                    request.fragment != null -> {
                        request.fragment.startActivityForResult(
                            intent,
                            request.requestCode,
                            request.options
                        )
                    }
                    else -> {
                        throw NavigationException("the context or fragment both are null")
                    }
                }
            }
        }

        // ------------------------------- 启动界面核心代码 ------------------------------- END
        request.executeAfterStartActivityAction()
        return null
    }

    @Synchronized
    fun listPageInterceptors(uri: Uri): List<RouterInterceptor> {
        // 获取目标对象
        val routerBean = getTarget(uri) ?: return emptyList()

        // 如果没有拦截器直接返回 null
        if (routerBean.pageInterceptors.isEmpty()) {
            return emptyList()
        }
        val result: MutableList<RouterInterceptor> = ArrayList(routerBean.pageInterceptors.size)

        // 排个序
        val pageInterceptors = routerBean.pageInterceptors.toMutableList()
        pageInterceptors.sortWith { o1, o2 -> o2.priority - o1.priority }
        for (pageInterceptorBean in pageInterceptors) {
            val interceptorName = pageInterceptorBean.interceptorName
            val interceptorClass = pageInterceptorBean.interceptorClass
            if (!interceptorName.isNullOrEmpty()) {
                val interceptor = InterceptorCenter.getByName(interceptorName)
                    ?: throw InterceptorNotFoundException("can't find the interceptor and it's name is $interceptorName,target url is $uri")
                result.add(interceptor)
            } else if (interceptorClass != null) {
                val interceptor =
                    RouterInterceptorCache.getInterceptorByClass(tClass = interceptorClass)
                        ?: throw InterceptorNotFoundException("can't find the interceptor and it's className is $interceptorClass,target url is $uri")
                result.add(interceptor)
            } else {
                throw InterceptorNotFoundException("String interceptor and class interceptor are both null")
            }
        }
        return result
    }

    private fun getTarget(uri: Uri): RouterBean? {
        val targetKey: String = getTargetRouterKey(uri)
        for ((key, value) in routerRegExMap) {
            if (Pattern.matches(key, targetKey)) {
                return value
            }
        }
        return routerMap[targetKey]
    }

    /**
     * scheme + "://" + host + path
     */
    private fun getTargetRouterKey(uri: Uri): String {
        // "/component1/test" 不含host
        var targetPath = uri.path
        return if (!targetPath.isNullOrEmpty()) {
            if (targetPath[0] != '/') {
                targetPath = ComponentConstants.SEPARATOR + targetPath
            }
            uri.scheme + "://" + uri.host + targetPath
        } else {
            uri.scheme + "://" + uri.host
        }
    }

    /**
     * 找到那个 Activity 中隐藏的一个 Fragment,如果找的到就会用这个 Fragment 拿来跳转
     */
    private fun findFragment(context: Context): Fragment? {
        var result: Fragment? = null
        val act = Utils.getActivityFromContext(context)
        if (act is FragmentActivity) {
            val ft = act.supportFragmentManager
            result = ft.findFragmentByTag(ComponentConstants.ACTIVITY_RESULT_FRAGMENT_TAG)
        }
        return result
    }

    private fun findFragment(fragment: Fragment): Fragment? {
        return fragment.childFragmentManager.findFragmentByTag(ComponentConstants.ACTIVITY_RESULT_FRAGMENT_TAG)
    }

    fun register(
        moduleName: String,
        routerMap: List<RouterBean>,
    ) {
        if (!hostRouterMap.containsKey(key = moduleName)) {
            hostRouterMap[moduleName] = ModuleRouterBean(
                moduleName = moduleName,
                routerMap = routerMap,
            )
            routerMap.forEach { item ->
                if (item.regex?.isNotEmpty() == true) {
                    this.routerRegExMap[item.regex] = item
                } else if (item.uri?.isNotEmpty() == true) {
                    this.routerMap[item.uri] = item
                }
            }
        }
    }

    fun unregister(
        moduleName: String,
    ) {
        hostRouterMap.remove(key = moduleName)?.let {
            it.routerMap.forEach { item ->
                if (item.regex?.isNotEmpty() == true) {
                    this.routerRegExMap.remove(key = item.regex)
                } else if (item.uri?.isNotEmpty() == true) {
                    this.routerMap.remove(key = item.uri)
                }
            }
        }
    }

    /**
     * 路由表重复的检查工作
     */
    fun check() {
        val routerSet: MutableSet<String> = HashSet()
        val routerRegExSet: Set<String> = HashSet()
        for ((_, value) in hostRouterMap) {
            value.routerMap.forEach { item ->
                if (item.regex?.isNotEmpty() == true) {
                    check(!routerRegExSet.contains(element = item.regex)) { "the target regex is exist：${item.regex}" }
                    routerSet.add(element = item.regex)
                } else if (item.uri?.isNotEmpty() == true) {
                    check(!routerSet.contains(element = item.uri)) { "the target uri is exist：${item.uri}" }
                    routerSet.add(element = item.uri)
                }
            }
        }
    }
}