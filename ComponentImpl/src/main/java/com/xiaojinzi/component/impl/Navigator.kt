package com.xiaojinzi.component.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.annotation.CheckResult
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.xiaojinzi.component.Component
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.anno.IOThread
import com.xiaojinzi.component.anno.MainThread
import com.xiaojinzi.component.anno.router.RequestCodeAnno
import com.xiaojinzi.component.anno.support.*
import com.xiaojinzi.component.bean.ActivityResult
import com.xiaojinzi.component.bean.InterceptorThreadType
import com.xiaojinzi.component.error.ignore.InterceptorNotFoundException
import com.xiaojinzi.component.error.ignore.NavigationCancelException
import com.xiaojinzi.component.error.ignore.NavigationException
import com.xiaojinzi.component.impl.interceptor.InterceptorCenter
import com.xiaojinzi.component.impl.interceptor.OpenOnceInterceptor
import com.xiaojinzi.component.support.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.KClass

/**
 * 拦截器多个连接着走的执行器,源代码来源于 OkHTTP
 * 这个原理就是, 本身是一个 执行器 (Chain),当你调用 proceed 方法的时候,会创建下一个拦截器的执行对象
 * 然后调用当前拦截器的 intercept 方法
 * @param mInterceptors 拦截器列表,所有要执行的拦截器列表
 * @param mIndex        拦截器的下标
 * @param mRequest      第一次这个对象是不需要的
 * @param mCallback     这个是拦截器的回调,这个用户不能自定义,一直都是一个对象
 */
open class InterceptorChain(
    private val mInterceptors: List<RouterInterceptor?>,
    private val mIndex: Int,
    /**
     * 每一个拦截器执行器 [RouterInterceptor.Chain]
     * 都会有上一个拦截器给的 request 对象或者初始化的一个 request,用于在下一个拦截器
     * 中获取到 request 对象,并且支持拦截器自定义修改 request 对象或者直接创建一个新的传给下一个拦截器执行器
     */
    private val mRequest: RouterRequest,
) : RouterInterceptor.Chain // 占位
{

    private val defaultContext by lazy {
        when (Component.requiredConfig().interceptorDefaultThread) {
            InterceptorThreadType.IO -> Dispatchers.IO
            InterceptorThreadType.Main -> Dispatchers.Main
        }
    }

    /**
     * 调用的次数,如果超过1次就做相应的错误处理
     */
    private var calls = 0

    /**
     * 拦截器是否是否已经走完
     */
    @Synchronized
    protected fun isCompletedProcess(): Boolean {
        return mIndex >= mInterceptors.size
    }

    override fun request(): RouterRequest {
        // 第一个拦截器的
        return mRequest
    }

    override suspend fun proceed(request: RouterRequest): RouterResult {
        return withContext(context = defaultContext) {
            try {
                ++calls
                when {
                    isCompletedProcess() -> {
                        throw NavigationException(
                            cause = IndexOutOfBoundsException(
                                "size = " + mInterceptors.size + ",index = " + mIndex
                            )
                        )
                    }

                    calls > 1 -> { // 调用了两次
                        throw NavigationException(
                            "interceptor " + mInterceptors[mIndex - 1]
                                    + " must call proceed() exactly once"
                        )
                    }

                    else -> {
                        // current Interceptor
                        val interceptor = mInterceptors[mIndex]!!
                        // 当拦截器最后一个的时候,就不是这个类了,是 DoActivityStartInterceptor 了
                        val next = InterceptorChain(
                            mInterceptors = mInterceptors,
                            mIndex = mIndex + 1,
                            mRequest = request,
                        )
                        // 提前同步 Query 到 Bundle
                        next.request().syncUriToBundle()
                        if (interceptor.javaClass.annotations.isEmpty()) {
                            // 用户自定义的部分
                            interceptor.intercept(chain = next)
                        } else {
                            if (
                                interceptor.javaClass.isAnnotationPresent(MainThread::class.java)
                            ) {
                                withContext(context = Dispatchers.Main) {
                                    // 用户自定义的部分
                                    interceptor.intercept(chain = next)
                                }
                            } else if (
                                interceptor.javaClass.isAnnotationPresent(IOThread::class.java)
                            ) {
                                withContext(context = Dispatchers.IO) {
                                    // 用户自定义的部分
                                    interceptor.intercept(chain = next)
                                }
                            } else {
                                // 用户自定义的部分
                                interceptor.intercept(chain = next)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                throw e.toNavigationException()
            }
        }
    }

}

interface INavigator<T : INavigator<T>> : IRouterRequestBuilder<T>, Call {

    fun interceptors(vararg interceptorArr: RouterInterceptor): T
    fun interceptors(vararg interceptorClassArr: KClass<out RouterInterceptor>): T
    fun interceptorNames(vararg interceptorNameArr: String): T
    fun requestCodeRandom(): T
    fun useRouteRepeatCheck(useRouteRepeatCheck: Boolean): T
    fun proxyBundle(bundle: Bundle): T

}

/**
 * 这个类一部分功能应该是 [Router] 的构建者对象的功能,但是这里面更多的为导航的功能
 * 写了很多代码,所以名字就不叫 Builder 了
 */
@Suppress("UNCHECKED_CAST")
class NavigatorImpl<T : INavigator<T>> constructor(
    context: Context? = null,
    fragment: Fragment? = null,
    private val routerRequestBuilder: RouterRequestBuilderImpl<T> = RouterRequestBuilderImpl(
        context = context,
        fragment = fragment,
    ),
) : IRouterRequestBuilder<T> by routerRequestBuilder,
    INavigator<T>, Call {

    /**
     * 一些帮助方法
     */
    private object Help {
        /**
         * 和[RouterFragment] 配套使用
         */
        private val mRequestCodeSet: MutableSet<String> = HashSet()
        private val r = Random()

        /**
         * 如果 requestCode 是 [Navigator.RANDOM_REQUEST_CODE].
         * 则随机生成一个 requestCode
         * 生成的 requestCode 会在 Activity 或者 Activity 内 的Fragment 范围内是唯一的
         *
         * @return [1, 256]
         */
        fun randomlyGenerateRequestCode(request: RouterRequest): RouterRequest {
            // 如果不是想要随机生成,就直接返回
            if (RANDOM_REQUEST_CODE != request.requestCode) {
                return request
            }
            // 转化为构建对象
            val requestBuilder = request.toBuilder()
            // 随机生成一个
            var generateRequestCode = r.nextInt(256) + 1
            // 目标界面
            val targetActivity: Activity? = Utils.getActivityFromContext(requestBuilder.context)
            // 如果生成的这个 requestCode 存在,就重新生成
            while (
                isExist(
                    targetActivity,
                    requestBuilder.fragment,
                    generateRequestCode
                )
            ) {
                generateRequestCode = r.nextInt(256) + 1
            }
            return requestBuilder
                .apply {
                    this.requestCode(requestCode = generateRequestCode)
                }
                .build()
        }

        /**
         * 检测同一个 Fragment 或者 Activity 发起的多个路由 request 中的 requestCode 是否存在了
         *
         * @param request 路由请求对象
         */
        fun isExist(request: RouterRequest): Boolean {
            if (request.requestCode == null) {
                return false
            }
            // 这个 Context 关联的 Activity,用requestCode 去拿数据的情况下
            // Context 必须是一个 Activity 或者 内部的 baseContext 是 Activity
            val act = Utils.getActivityFromContext(context = request.context)
            // 这个requestCode不会为空, 用这个方法的地方是必须填写 requestCode 的
            return isExist(
                act = act,
                fragment = request.fragment,
                requestCode = request.requestCode
            )
        }

        /**
         * 这里分别检测 [Activity]、[Fragment] 和 requestCode 的重复.
         * 即使一个路由使用了 [Activity] + 123, 另一个用 [Fragment] + 123 也没问题是因为
         * 这两个分别会被预埋一个 [RouterFragment].
         * 所以他们共享一个[RouterFragment] 接受 [ActivityResult] 的
         */
        fun isExist(
            act: Activity?,
            fragment: Fragment?,
            requestCode: Int
        ): Boolean {
            if (act != null) {
                return mRequestCodeSet.contains(act.javaClass.name + requestCode)
            } else if (fragment != null) {
                return mRequestCodeSet.contains(fragment.javaClass.name + requestCode)
            }
            return false
        }

        /**
         * 添加一个路由请求的 requestCode
         *
         * @param request 路由请求对象
         */
        @UiThread
        fun addRequestCode(request: RouterRequest?) {
            if (request?.requestCode == null) {
                return
            }
            val requestCode = request.requestCode
            // 这个 Context 关联的 Activity,用requestCode 去拿数据的情况下
            // Context 必须是一个 Activity 或者 内部的 baseContext 是 Activity
            val act = Utils.getActivityFromContext(request.context)
            if (act != null) {
                mRequestCodeSet.add(act.javaClass.name + requestCode)
            } else if (request.fragment != null) {
                mRequestCodeSet.add(request.fragment.javaClass.name + requestCode)
            }
        }

        /**
         * 移除一个路由请求的 requestCode
         *
         * @param request 路由请求对象
         */
        @UiThread
        fun removeRequestCode(request: RouterRequest?) {
            if (request?.requestCode == null) {
                return
            }
            val requestCode = request.requestCode
            // 这个 Context 关联的 Activity,用requestCode 去拿数据的情况下
            // Context 必须是一个 Activity 或者 内部的 baseContext 是 Activity
            val act = Utils.getActivityFromContext(context = request.context)
            if (act != null) {
                mRequestCodeSet.remove(act.javaClass.name + requestCode)
            } else if (request.fragment != null) {
                mRequestCodeSet.remove(request.fragment.javaClass.name + requestCode)
            }
        }

    }

    /**
     * 处理页面拦截器的. 因为页面拦截器可能会更改 [Uri]. 导致目标改变.
     * 那么新的页面拦截器也应该被加载执行.
     * 最后确认 [Uri] 的目标没被改变的时候
     * 就可以加载 [DoActivityStartInterceptor] 执行跳转了.
     */
    @UiThread
    private class PageInterceptor(
        private val mOriginalRequest: RouterRequest,
        private val mAllInterceptors: MutableList<RouterInterceptor>
    ) : RouterInterceptor // 页面拦截器
    {

        override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
            val currentUri = chain.request().uri
            // 这个地址要执行的页面拦截器,这里取的时候一定要注意了,不能拿最原始的那个 request,因为上面的拦截器都能更改 request,
            // 导致最终跳转的界面和你拿到的页面拦截器不匹配,所以这里一定是拿上一个拦截器传给你的 request 对象
            val targetPageInterceptors = RouterCenter.listPageInterceptors(currentUri)
            mAllInterceptors.add(
                PageInterceptorUriCheckInterceptor(
                    mOriginalRequest,
                    mAllInterceptors,
                    currentUri,
                    targetPageInterceptors,
                    0
                )
            )
            // 执行下一个拦截器,正好是上面代码添加的拦截器
            return chain.proceed(chain.request())
        }

    }

    /**
     * 处理页面拦截器的. 因为页面拦截器可能会更改 [Uri]. 导致目标改变.
     * 那么新的页面拦截器也应该被加载执行.
     * 最后确认 [Uri] 的目标没被改变的时候
     * 就可以加载 [DoActivityStartInterceptor] 执行跳转了.
     */
    @UiThread
    private class PageInterceptorUriCheckInterceptor(
        private val mOriginalRequest: RouterRequest,
        private val mAllInterceptors: MutableList<RouterInterceptor>,
        /**
         * 进入页面拦截器之前的 [Uri]
         */
        private val mBeforePageInterceptorUri: Uri?,
        private val mPageInterceptors: List<RouterInterceptor>?,
        private var mPageIndex: Int
    ) : RouterInterceptor // 检查页面拦截器是否改变了目标, 并且重新执行目标界面拦截器的
    {
        @Throws(Exception::class)
        override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
            if (mPageIndex < 0) {
                throw NavigationException(
                    cause = IndexOutOfBoundsException(
                        "size = " + mPageInterceptors!!.size + ",index = " + mPageIndex
                    )
                )
            }
            val currentUri = chain.request().uri
            val isSameTarget = if (mBeforePageInterceptorUri != null) {
                RouterCenter.isSameTarget(mBeforePageInterceptorUri, currentUri)
            } else {
                false
            }

            // 如果目标是相同的, 说明页面拦截器并没有改变跳转的目标
            if (isSameTarget) {
                // 没有下一个了
                if (mPageInterceptors == null || mPageIndex >= mPageInterceptors.size) {
                    // 真正的执行跳转的拦截器, 如果正常跳转了 DoActivityStartInterceptor 拦截器就直接返回了
                    // 如果没有正常跳转过去, 内部会继续走拦截器, 会执行到后面的这个
                    mAllInterceptors.add(DoActivityStartInterceptor(mOriginalRequest))
                } else {
                    mAllInterceptors.add(mPageInterceptors[mPageIndex])
                    mAllInterceptors.add(
                        PageInterceptorUriCheckInterceptor(
                            mOriginalRequest, mAllInterceptors, mBeforePageInterceptorUri,
                            mPageInterceptors, ++mPageIndex
                        )
                    )
                }
            } else {
                mAllInterceptors.add(PageInterceptor(mOriginalRequest, mAllInterceptors))
            }
            // 执行下一个拦截器,正好是上面代码添加的拦截器
            return chain.proceed(chain.request())
        }
    }

    /**
     * 这是拦截器的最后一个拦截器了
     * 实现拦截器列表中的最后一环, 内部去执行了跳转的代码
     * 1.如果跳转的时候没有发生异常, 说明可以跳转过去
     * 如果失败了进行降级处理
     */
    @UiThread
    private class DoActivityStartInterceptor(
        private val mOriginalRequest: RouterRequest
    ) : RouterInterceptor // 真正执行跳转的拦截器
    {
        /**
         * @param chain 拦截器执行连接器
         * @throws Exception
         */
        @UiThread
        @Throws(NavigationException::class)
        override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
            // 这个 request 对象已经不是最原始的了,但是可能是最原始的,就看拦截器是否更改了这个对象了
            val finalRequest = chain.request()
            // 执行真正路由跳转回出现的异常
            var routeException: Exception? = null
            val targetIntent: Intent? = try {
                withContext(context = Dispatchers.Main) {
                    // 真正执行跳转的逻辑, 失败的话, 备用计划就会启动
                    RouterCenter.openUri(routerRequest = finalRequest)
                }
            } catch (e: Exception) { // 错误的话继续下一个拦截器
                routeException = e
                null
            }
            // 如果正常跳转成功需要执行下面的代码
            if (routeException == null) {
                return RouterResult(
                    originalRequest = mOriginalRequest,
                    finalRequest = finalRequest,
                    targetIntent = targetIntent,
                )
            } else {
                try {
                    // 获取路由的降级处理类
                    val routerDegrade = getRouterDegrade(finalRequest)
                        ?: // 抛出异常走 try catch 的逻辑
                        throw NavigationException("degrade route fail, it's url is " + mOriginalRequest.uri.toString())
                    // 获取关联的 Context, 如果获取不到, 那就是界面不行了
                    if (finalRequest.autoCancel) {
                        finalRequest.rawAliveContext
                    } else {
                        finalRequest.rawContext
                    } ?: throw NavigationCancelException(
                        message = "is your fragment or Activity is Destroyed?".trimIndent()
                    )
                    // 降级跳转
                    val targetDegradeIntent: Intent? = withContext(context = Dispatchers.Main) {
                        RouterCenter.routerDegrade(
                            finalRequest,
                            routerDegrade.onDegrade(request = finalRequest)
                        )
                    }
                    // 成功的回调
                    return RouterResult(
                        originalRequest = mOriginalRequest,
                        finalRequest = finalRequest,
                        targetIntent = targetDegradeIntent,
                    )
                } catch (ignore: Exception) {
                    // 如果版本足够就添加到异常堆中, 否则忽略降级路由的错误
                    routeException.addSuppressed(ignore)
                    throw routeException
                }
            }
        }

        /**
         * 获取降级的处理类
         *
         * @param finalRequest 最终的路由请求
         */
        private fun getRouterDegrade(finalRequest: RouterRequest): RouterDegrade? {
            // 获取所有降级类
            val routerDegradeList = RouterDegradeCenter.globalRouterDegradeList
            var result: RouterDegrade? = null
            for (i in routerDegradeList.indices) {
                val routerDegrade = routerDegradeList[i]
                // 如果匹配
                val isMatch = routerDegrade.isMatch(finalRequest)
                if (isMatch) {
                    result = routerDegrade
                    break
                }
            }
            return result
        }
    }

    companion object {
        /**
         * requestCode 如果等于这个值,就表示是随机生成的
         * 从 1-256 中随机生成一个,如果生成的正好是目前正在用的,会重新生成一个
         */
        const val RANDOM_REQUEST_CODE = RequestCodeAnno.RANDOM_REQUEST_CODE

        /**
         * 返回自定义的拦截器
         */
        @UiThread
        @Suppress("UNCHECKED_CAST")
        @Throws(InterceptorNotFoundException::class)
        private fun getCustomInterceptors(
            originalRequest: RouterRequest,
            customInterceptors: List<Any>,
        ): List<RouterInterceptor> {
            return customInterceptors.mapNotNull { item ->
                when (item) {
                    is RouterInterceptor -> item
                    is KClass<*> -> {
                        RouterInterceptorCache
                            .getInterceptorByClass(tClass = (item as KClass<out RouterInterceptor>))
                            ?: throw InterceptorNotFoundException("can't find the interceptor and it's className is " + item as KClass<*> + ",target url is " + originalRequest.uri.toString())
                    }

                    is String -> {
                        InterceptorCenter
                            .getByName(interceptorName = item)
                            ?: throw InterceptorNotFoundException("can't find the interceptor and it's name is " + item + ",target url is " + originalRequest.uri.toString())
                    }

                    else -> null
                }
            }
        }
    }

    /**
     * 自定义的拦截器列表,为了保证顺序才用一个集合的
     * 1. RouterInterceptor 类型
     * 2. Class<RouterInterceptor> 类型
     * 3. String 类型
     * 其他类型会 debug 的时候报错
     */
    private var customInterceptors: MutableList<Any>? = null

    /**
     * 标记这个 builder 是否已经被使用了,使用过了就不能使用了
     */
    private var isFinish = false

    /**
     * 是否检查路由是否重复, 默认是全局配置的开关
     */
    private var useRouteRepeatCheck = Component.requiredConfig().isUseRouteRepeatCheckInterceptor

    var thisObject: T = this as T
        set(value) {
            routerRequestBuilder.thisObject = value
            field = value
        }

    private fun getRealDelegateImpl(): T {
        return thisObject
    }

    /**
     * 懒加载自定义拦截器列表
     */
    private fun lazyInitCustomInterceptors(size: Int) {
        if (customInterceptors == null) {
            customInterceptors = ArrayList(if (size > 3) size else 3)
        }
    }

    override fun interceptors(vararg interceptorArr: RouterInterceptor): T {
        Utils.debugCheckNullPointer(interceptorArr, "interceptorArr")
        lazyInitCustomInterceptors(interceptorArr.size)
        customInterceptors!!.addAll(interceptorArr.toList())
        return getRealDelegateImpl()
    }

    override fun interceptors(vararg interceptorClassArr: KClass<out RouterInterceptor>): T {
        Utils.debugCheckNullPointer(interceptorClassArr, "interceptorClassArr")
        lazyInitCustomInterceptors(interceptorClassArr.size)
        customInterceptors!!.addAll(interceptorClassArr.toList())
        return getRealDelegateImpl()
    }

    override fun interceptorNames(vararg interceptorNameArr: String): T {
        Utils.debugCheckNullPointer(interceptorNameArr, "interceptorNameArr")
        lazyInitCustomInterceptors(interceptorNameArr.size)
        customInterceptors!!.addAll(interceptorNameArr.toList())
        return getRealDelegateImpl()
    }

    /**
     * requestCode 会随机的生成
     */
    override fun requestCodeRandom(): T {
        requestCode(requestCode = RANDOM_REQUEST_CODE)
        return getRealDelegateImpl()
    }

    override fun useRouteRepeatCheck(useRouteRepeatCheck: Boolean): T {
        this.useRouteRepeatCheck = useRouteRepeatCheck
        return getRealDelegateImpl()
    }

    /**
     * 当您使用 [ProxyIntentBuilder] 构建了一个 [Intent] 之后.
     * 此 [Intent] 的跳转目标是一个代理的界面. 具体是
     * [ProxyIntentAct] 或者是用户你自己自定义的 [<]
     * 携带的参数是是真正的目标的信息. 比如：
     * [ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_URL] 表示目标的 url
     * [ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_BUNDLE] 表示跳转到真正的目标的 [Bundle] 数据
     * ......
     * 当你自定义了代理界面, 那你可以使用[Router.with] 或者  [Router.with] 或者
     * [Router.with] 得到一个 [Navigator]
     * 然后你就可以使用[Navigator.proxyBundle] 直接导入跳转到真正目标所需的各种参数, 然后
     * 直接发起跳转, 通过条用 [Navigator.forward] 等方法
     * 示例代码：
     * <pre class="prettyprint">
     * public class XXXProxyActivity extends Activity {
     *      ...
     *      protected void onCreate(Bundle savedInstanceState) {
     *           super.onCreate(savedInstanceState);
     *           Router.with(this)
     *                   .proxyBundle(getIntent().getExtras())
     *                   .forward();
     *      }
     *      ...
     * }</pre>
     *
     * @see ProxyIntentAct
     */
    override fun proxyBundle(bundle: Bundle): T {
        val reqUrl = bundle.getString(ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_URL)
        val reqBundle = bundle.getBundle(ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_BUNDLE)
        val reqOptions: Bundle? = bundle.getBundle(ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_OPTIONS)
        val reqFlags: ArrayList<Int> = bundle
            .getIntegerArrayList(ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_FLAGS) ?: ArrayList()
        val reqCategories =
            bundle.getStringArrayList(ProxyIntentAct.EXTRA_ROUTER_PROXY_INTENT_CATEGORIES)
        routerRequestBuilder.url(reqUrl!!)
        routerRequestBuilder.putAll(reqBundle!!)
        routerRequestBuilder.options(reqOptions)
        routerRequestBuilder.addIntentFlags(*reqFlags.toIntArray())
        routerRequestBuilder.addIntentCategories(*reqCategories!!.toTypedArray())
        return getRealDelegateImpl()
    }

    override fun build(): RouterRequest {
        var routerRequest = routerRequestBuilder.build()
        // 如果是随机的 requestCode, 则生成
        routerRequest = Help.randomlyGenerateRequestCode(routerRequest)
        // 现在可以检测 requestCode 是否重复
        val isExist = Help.isExist(request = routerRequest)
        if (isExist) { // 如果存在直接返回错误给 callback
            throw NavigationException(
                "request&result code is " +
                        routerRequest.requestCode + " is exist!"
            )
        }
        return routerRequest
    }

    /**
     * 使用默认的 [android.app.Application] 作为
     * [Context]. 使用默认的 [android.app.Application]
     * 会添加 [Intent.FLAG_ACTIVITY_NEW_TASK] 标记
     */
    private fun useDefaultContext() {
        // 如果 Context 和 Fragment 都是空的,使用默认的 Application
        if (context == null && fragment == null) {
            context(context = Component.getApplication())
            // 配套加上 New_Task 的标志, 当用户自己传的 Application 需要自己添加这个 flag
            // 起到更好的提示用户是使用 Application 跳的
            addIntentFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * 检查 forResult 的时候的各个参数是否合格
     */
    @Throws(Exception::class)
    private fun onCheckForResult() {
        if (context == null && fragment == null) {
            throw NavigationException(
                cause = NullPointerException(
                    "Context or Fragment is necessary if you want get ActivityResult"
                )
            )
        }
        // 如果是使用 Context 的,那么就必须是 FragmentActivity,需要操作 Fragment
        // 这里的 context != null 判断条件不能去掉,不然使用 Fragment 跳转的就过不去了
        if (context != null && Utils.getActivityFromContext(context) !is FragmentActivity) {
            throw NavigationException(
                cause = IllegalArgumentException(
                    "context must be FragmentActivity or fragment must not be null " +
                            "when you want get ActivityResult from target Activity"
                )
            )
        }
        if (requestCode == null) {
            throw NavigationException(
                cause = NullPointerException(
                    "requestCode must not be null when you want get ActivityResult from target Activity, " +
                            "if you use code, do you forget call requestCodeRandom() or requestCode(Integer). " +
                            "if you use routerApi, do you forget mark method or parameter with @RequestCodeAnno() Annotation"
                )
            )
        }
    }

    /**
     * 必须在主线程中调用,就这里可能会出现一种特殊的情况：
     * 用户收到的回调可能是 error,但是全局的监听可能是 cancel,其实这个问题也能解决,
     * 就是路由调用之前提前通过方法 [Navigator.build] 提前构建一个 [RouterRequest] 出来判断
     * 但是没有那个必要去做这件事情了,等到有必要的时候再说,基本不会出现并且出现了也不是什么问题
     */
    @CheckResult
    @NeedOptimizeAnno("回调优化下")
    @CoreLogicAnno(value = "这是跳转获取 ActivityResult, 并且转化为 BiCallback 的核心方法")
    override fun navigateForResult(
        callback: BiCallback<ActivityResult>,
    ): NavigationDisposable {

        val callable = {
            try {
                // 标记此次是需要框架帮助获取 ActivityResult 的
                isForResult(isForResult = true)
                // 为了拿数据做的检查
                onCheckForResult()
                // 声明fragment
                val fm: FragmentManager = if (context == null) {
                    fragment!!.childFragmentManager
                } else {
                    (Utils.getActivityFromContext(context) as FragmentActivity?)!!.supportFragmentManager
                }
                // 寻找是否添加过 Fragment
                var findRxFragment =
                    fm.findFragmentByTag(ComponentConstants.ACTIVITY_RESULT_FRAGMENT_TAG) as RouterFragment?
                if (findRxFragment == null) {
                    findRxFragment = RouterFragment()
                    fm.beginTransaction()
                        .add(
                            findRxFragment,
                            ComponentConstants.ACTIVITY_RESULT_FRAGMENT_TAG
                        ) // 这里必须使用 now 的形式, 否则连续的话立马就会new出来. 因为判断进来了
                        .commitNowAllowingStateLoss()
                }
                val rxFragment: RouterFragment = findRxFragment
                navigate(object : CallbackAdapter() {
                    override fun onSuccess(result: RouterResult) {
                        super.onSuccess(result)
                        val request = result.originalRequest
                        rxFragment.addActivityResultConsumer(
                            request = request,
                        ) { activityResult ->
                            RouterUtil.activityResultSuccessCallback(
                                callback = callback,
                                successResult = ActivityResultRouterResult(
                                    routerResult = result,
                                    activityResult = activityResult,
                                ),
                            )
                        }
                        // 添加这个 requestCode 到 map, 重复的事情不用考虑了, 在 build RouterRequest 的时候已经处理了
                        Help.addRequestCode(request = result.originalRequest)
                    }

                    override fun onError(errorResult: RouterErrorResult) {
                        super.onError(errorResult)
                        errorResult.originalRequest?.let { routerRequest ->
                            rxFragment.removeActivityResultConsumer(
                                request = routerRequest,
                            )
                            Help.removeRequestCode(request = routerRequest)
                        }
                        callback.onError(
                            errorResult = errorResult
                        )
                    }

                    override fun onCancel(originalRequest: RouterRequest?) {
                        super.onCancel(originalRequest)
                        originalRequest?.let { routerRequest ->
                            rxFragment.removeActivityResultConsumer(
                                request = routerRequest,
                            )
                            Help.removeRequestCode(request = routerRequest)
                        }
                        callback.onCancel(
                            originalRequest = originalRequest
                        )
                    }
                })
            } catch (e: Exception) {
                RouterUtil.errorCallback(
                    biCallback = callback,
                    errorResult = RouterErrorResult(
                        error = e,
                    )
                )
                NavigationDisposable.EmptyNavigationDisposable
            }
        }

        val proxyDisposable = NavigationDisposable.NavigationDisposableProxy()
        if (Utils.isMainThread()) {
            return callable.invoke()
        } else {
            Utils.postActionToMainThreadAnyway {
                proxyDisposable.setProxy(
                    target = callable.invoke()
                )
            }
        }
        return proxyDisposable

    }

    @AnyThread
    @NoErrorAnno
    @CheckResult
    @Synchronized // 方法加锁
    @CoreLogicAnno(
        value = "这是普通跳转转化为 Callback 并且返回可取消对象的核心方法, " +
                "也是对外提供的路由中最核心的方法. 其他的路由方法基本都是基于此方法"
    )
    override fun navigate(@UiThread callback: Callback?): NavigationDisposable {
        // 可取消对象, 默认主线程
        val scope = MainScope()
        val navigationDisposable: NavigationDisposable = NavigationDisposableImpl(
            scope = scope,
        )
        var originalRequest: RouterRequest? = null
        scope.launch(context = Dispatchers.IO) {
            try {
                // 如果用户没填写 Context 或者 Fragment 默认使用 Application
                useDefaultContext()
                // 路由前的检查
                if (isFinish) {
                    // 一个 Builder 不能被使用多次
                    throw NavigationException("Builder can't be used multiple times")
                }
                if (context == null && fragment == null) {
                    // 检查上下文和fragment
                    throw NavigationException(
                        cause = NullPointerException("the parameter 'context' or 'fragment' both are null")
                    )
                }
                // 标记这个 builder 已经不能使用了
                isFinish = true
                // 生成路由请求对象
                originalRequest = build()
                originalRequest?.let {
                    val routerResult: RouterResult = navigateWithAutoCancel(originalRequest = it)
                    // 如果是框架帮拿 ActivityResult, 那么此次路由是没有结束的, 不需要回调
                    if (originalRequest?.isForResult == false) {
                        RouterUtil.successCallback(
                            callback = callback,
                            successResult = routerResult,
                        )
                    } else {
                        callback?.onSuccess(
                            result = routerResult,
                        )
                    }
                }
            } catch (e: NavigationCancelException) {
                RouterUtil.cancelCallback(
                    request = e.originalRequest,
                    callback = callback,
                )
            } catch (e: NavigationException) {
                RouterUtil.errorCallback(
                    callback = callback,
                    errorResult = RouterErrorResult(
                        originalRequest = originalRequest,
                        error = e,
                    )
                )
            } catch (e: Exception) {
                RouterUtil.errorCallback(
                    callback = callback,
                    errorResult = RouterErrorResult(
                        originalRequest = originalRequest,
                        error = e,
                    )
                )
            }
        }.invokeOnCompletion { error ->
            when (error) {
                is CancellationException -> {
                    RouterUtil.cancelCallback(
                        request = originalRequest,
                        callback = callback,
                    )
                }
            }
        }
        return navigationDisposable
    }

    @CheckResult
    @Throws(NavigationException::class)
    @CoreLogicAnno(value = "这是自动取消功能的核心实现")
    private suspend fun navigateWithAutoCancel(originalRequest: RouterRequest): RouterResult {
        val actScope = (originalRequest.context as? FragmentActivity)?.lifecycleScope
        val fragScope = fragment?.lifecycleScope
        val scope = (actScope ?: fragScope)
        return if (scope != null && originalRequest.autoCancel) {
            suspendCancellableCoroutine { cancellableContinuation ->
                val job = scope.launch(context = Dispatchers.IO) {
                    try {
                        val result = navigateCore(
                            originalRequest = originalRequest,
                            customInterceptors = customInterceptors,
                        )
                        cancellableContinuation.resume(
                            value = result,
                        )
                    } catch (e: Exception) {
                        try {
                            cancellableContinuation.resumeWithException(
                                exception = e,
                            )
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
                job.invokeOnCompletion { error ->
                    when (error) {
                        is CancellationException -> {
                            cancellableContinuation.cancel()
                        }
                    }
                }
                cancellableContinuation.invokeOnCancellation {
                    job.cancel()
                }
            }
        } else {
            navigateCore(
                originalRequest = originalRequest,
                customInterceptors = customInterceptors,
            )
        }
    }

    /**
     * 真正的执行路由
     * 拦截器执行链也是从这里开始
     *
     * @param originalRequest           最原始的请求对象
     * @param customInterceptors        自定义的拦截器
     * @param routerInterceptorCallback 回调对象
     * @return 返回值是：[RouterResult]
     */
    @RunInWorkThread
    @Throws(NavigationException::class)
    @CoreLogicAnno(value = "这个方法是最终发起路由的地方, 开启了拦截器链的执行")
    private suspend fun navigateCore(
        originalRequest: RouterRequest,
        customInterceptors: List<Any>?,
    ): RouterResult {

        return withContext(context = Dispatchers.IO) {
            // 自定义拦截器,初始化拦截器的个数 8 个够用应该不会经常扩容
            val allInterceptors: MutableList<RouterInterceptor> = ArrayList(10)

            // 此拦截器用于执行一些整个流程开始之前的事情
            allInterceptors.add(object : RouterInterceptor {
                override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
                    withContext(context = Dispatchers.Main) {
                        // 执行跳转前的 Callback
                        chain.request().executeBeforeRouteAction()
                    }
                    // 继续下一个拦截器
                    return chain.proceed(request = chain.request())
                }
            })

            // 添加路由检查拦截器
            if (useRouteRepeatCheck) {
                allInterceptors.add(OpenOnceInterceptor)
            }
            // 添加共有拦截器
            allInterceptors.addAll(
                InterceptorCenter.globalInterceptorList
            )
            // 添加用户自定义的拦截器
            allInterceptors.addAll(
                getCustomInterceptors(
                    originalRequest = originalRequest,
                    customInterceptors = customInterceptors ?: emptyList(),
                )
            )
            // 负责加载目标 Intent 的页面拦截器的拦截器. 此拦截器后不可再添加其他拦截器
            allInterceptors.add(PageInterceptor(originalRequest, allInterceptors))

            // 创建执行器
            val chain: RouterInterceptor.Chain = InterceptorChain(
                allInterceptors, 0,
                originalRequest,
            )

            // 执行
            chain.proceed(originalRequest)
        }

    }

    init {
        routerRequestBuilder.thisObject = this as T
    }

}

@CheckClassNameAnno
class Navigator(
    context: Context? = null,
    fragment: Fragment? = null,
    private val navigator: NavigatorImpl<Navigator> = NavigatorImpl(
        context = context,
        fragment = fragment
    )
) : INavigator<Navigator> by navigator {

    init {
        navigator.thisObject = this
    }

}