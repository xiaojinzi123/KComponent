package com.xiaojinzi.component.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.annotation.AnyThread
import androidx.annotation.CheckResult
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.anno.support.CoreLogicAnno
import com.xiaojinzi.component.bean.ActivityResult
import com.xiaojinzi.component.error.ignore.ActivityResultException
import com.xiaojinzi.component.error.ignore.NavigationCancelException
import com.xiaojinzi.component.support.CallbackAdapter
import com.xiaojinzi.component.support.NavigationDisposable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 这个对象表示一个可调用的路由跳转
 */
@CheckClassNameAnno
interface Call {

    // ---------------------------- 普通跳转----------------------------

    /**
     * 普通跳转
     *
     * @param callback 当跳转完毕或者发生错误会回调
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forward(callback: Callback? = null) {
        navigate(callback = callback)
    }

    /**
     * 普通跳转
     *
     * @param callback 当跳转完毕或者发生错误会回调
     */
    @AnyThread
    fun forward(callbackForSuccess: (result: RouterResult) -> Unit) {
        forward(callback = object : CallbackAdapter() {
            override fun onSuccess(result: RouterResult) {
                callbackForSuccess.invoke(result)
            }
        })
    }

    /**
     * 执行跳转的具体逻辑
     * 返回值不可以为空, 是为了使用的时候更加的顺溜,不用判断空
     *
     * @param callback 当跳转完毕或者发生错误会回调,
     * 回调给用户的 [Callback], 回调中的各个方法, 每个方法至多有一个方法被调用, 并且只有一次
     * @return 返回的对象有可能是一个空实现对象 [NavigationDisposable.EmptyNavigationDisposable],可以取消路由或者获取原始request对象
     */
    @AnyThread
    @CheckResult
    @CoreLogicAnno
    fun navigate(callback: Callback? = null): NavigationDisposable

    @AnyThread
    @CheckResult
    fun navigate(callbackForSuccess: (result: RouterResult) -> Unit): NavigationDisposable {
        return navigate(callback = object : CallbackAdapter() {
            override fun onSuccess(result: RouterResult) {
                callbackForSuccess.invoke(result)
            }
        })
    }

    /**
     * 等待路由完成
     */
    suspend fun await() {
        return suspendCancellableCoroutine { cout ->
            val disposable = navigate(
                object : CallbackAdapter() {

                    override fun onSuccess(result: RouterResult) {
                        super.onSuccess(result)
                        cout.resume(
                            value = Unit
                        )
                    }

                    override fun onError(errorResult: RouterErrorResult) {
                        super.onError(errorResult)
                        cout.resumeWithException(
                            exception = errorResult.error,
                        )
                    }

                    override fun onCancel(originalRequest: RouterRequest?) {
                        super.onCancel(originalRequest)
                        cout.resumeWithException(
                            exception = NavigationCancelException(
                                originalRequest = originalRequest,
                            ),
                        )
                    }

                }
            )
            cout.invokeOnCancellation {
                disposable.cancel()
            }
        }
    }

    // ---------------------------- 跳转 拿 ActivityResult ----------------------------

    /**
     * 跳转拿到 [ActivityResult] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forwardForResult(callback: BiCallback<ActivityResult>) {
        navigateForResult(callback = callback)
    }

    /**
     * 跳转拿到 [ActivityResult] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     */
    @AnyThread
    fun forwardForResult(callback: (ActivityResult) -> Unit) {
        forwardForResult(callback = object : BiCallback.BiCallbackAdapter<ActivityResult>() {
            override fun onSuccess(result: RouterResult, targetValue: ActivityResult) {
                super.onSuccess(result, targetValue)
                callback.invoke(targetValue)
            }
        })
    }

    /**
     * 跳转拿到 [ActivityResult] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    @CoreLogicAnno
    fun navigateForResult(callback: BiCallback<ActivityResult>): NavigationDisposable

    /**
     * 跳转拿到 [ActivityResult] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForResult(callback: (ActivityResult) -> Unit): NavigationDisposable {
        return navigateForResult(
            callback = object : BiCallback.BiCallbackAdapter<ActivityResult>() {
                override fun onSuccess(result: RouterResult, targetValue: ActivityResult) {
                    super.onSuccess(result, targetValue)
                    callback.invoke(targetValue)
                }
            }
        )
    }

    @AnyThread
    @CheckResult
    suspend fun activityResultAwait(): ActivityResult {
        return suspendCancellableCoroutine { cout ->
            val disposable = navigateForResult(
                object : BiCallback.BiCallbackAdapter<ActivityResult>() {

                    override fun onSuccess(result: RouterResult, targetValue: ActivityResult) {
                        super.onSuccess(result, targetValue)
                        cout.resume(
                            value = targetValue,
                        )
                    }

                    override fun onError(errorResult: RouterErrorResult) {
                        super.onError(errorResult)
                        cout.resumeWithException(
                            exception = errorResult.error,
                        )
                    }

                    override fun onCancel(originalRequest: RouterRequest?) {
                        super.onCancel(originalRequest)
                        cout.resumeWithException(
                            exception = NavigationCancelException(
                                originalRequest = originalRequest,
                            ),
                        )
                    }

                }
            )
            cout.invokeOnCancellation {
                disposable.cancel()
            }
        }
    }

    // ---------------------------- 跳转 拿 Intent ----------------------------

    /**
     * 跳转拿到 [Intent] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forwardForIntent(callback: BiCallback<Intent>) {
        navigateForIntent(callback = callback)
    }

    /**
     * 跳转拿到 [Intent] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     */
    @AnyThread
    fun forwardForIntent(callback: (Intent) -> Unit) {
        forwardForIntent(callback = object : BiCallback.BiCallbackAdapter<Intent>() {
            override fun onSuccess(result: RouterResult, targetValue: Intent) {
                super.onSuccess(result, targetValue)
                callback.invoke(targetValue)
            }
        })
    }

    /**
     * 跳转拿到 [Intent] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForIntent(callback: BiCallback<Intent>): NavigationDisposable {
        return navigateForResult(object :
            BiCallback.Map<ActivityResult, Intent>(targetBiCallback = callback) {
            @Throws(Exception::class)
            override fun apply(t: ActivityResult): Intent {
                return t.intentCheckAndGet()
            }
        })
    }

    /**
     * 跳转拿到 [Intent] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForIntent(callback: (Intent) -> Unit): NavigationDisposable {
        return navigateForIntent(callback = object : BiCallback.BiCallbackAdapter<Intent>() {
            override fun onSuccess(result: RouterResult, targetValue: Intent) {
                super.onSuccess(result, targetValue)
                callback.invoke(targetValue)
            }
        })
    }

    @AnyThread
    @CheckResult
    suspend fun intentAwait(): Intent {
        return activityResultAwait().intentCheckAndGet()
    }

    // ---------------------------- 跳转 拿 ResultCode ----------------------------

    /**
     * 跳转拿到 [ActivityResult.resultCode] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forwardForResultCode(callback: BiCallback<Int>) {
        navigateForResultCode(callback = callback)
    }

    /**
     * 跳转拿到 [ActivityResult.resultCode] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     */
    @AnyThread
    fun forwardForResultCode(callback: (Int) -> Unit) {
        forwardForResultCode(callback = object : BiCallback.BiCallbackAdapter<Int>() {
            override fun onSuccess(result: RouterResult, targetValue: Int) {
                super.onSuccess(result, targetValue)
                callback.invoke(targetValue)
            }
        })
    }

    /**
     * 跳转拿到 [ActivityResult.resultCode] 数据
     *
     * @param callback 当 [ActivityResult] 拿到之后或者发生错误会回调
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForResultCode(callback: BiCallback<Int>): NavigationDisposable {
        return navigateForResult(object : BiCallback.Map<ActivityResult, Int>(callback) {
            @Throws(Exception::class)
            override fun apply(t: ActivityResult): Int {
                return t.resultCode
            }
        })
    }

    @AnyThread
    @CheckResult
    fun navigateForResultCode(callback: (Int) -> Unit): NavigationDisposable {
        return navigateForResultCode(callback = object : BiCallback.BiCallbackAdapter<Int>() {
            override fun onSuccess(result: RouterResult, targetValue: Int) {
                super.onSuccess(result, targetValue)
                callback.invoke(targetValue)
            }
        })
    }

    /**
     * 跳转拿到 [ActivityResult.resultCode] 数据
     */
    @AnyThread
    @CheckResult
    suspend fun resultCodeAwait(): Int {
        return activityResultAwait().resultCode
    }

    // ---------------------------- 跳转 匹配 ResultCode 拿 Intent ----------------------------

    /**
     * 跳转拿到 [Intent] 数据
     *
     * @param callback           当 [ActivityResult] 拿到之后或者发生错误会回调
     * @param expectedResultCode 会匹配的 resultCode
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forwardForIntentAndResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: BiCallback<Intent>
    ) {
        navigateForIntentAndResultCodeMatch(
            expectedResultCode = expectedResultCode,
            callback = callback,
        )
    }

    @AnyThread
    fun forwardForIntentAndResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: (Intent) -> Unit,
    ) {
        forwardForIntentAndResultCodeMatch(expectedResultCode = expectedResultCode,
            callback = object : BiCallback.BiCallbackAdapter<Intent>() {
                override fun onSuccess(result: RouterResult, targetValue: Intent) {
                    super.onSuccess(result, targetValue)
                    callback.invoke(targetValue)
                }
            })
    }

    /**
     * 跳转拿到 [Intent] 数据
     *
     * @param callback           当 [ActivityResult] 拿到之后或者发生错误会回调
     * @param expectedResultCode 会匹配的 resultCode
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForIntentAndResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: BiCallback<Intent>
    ): NavigationDisposable {
        return navigateForResult(object : BiCallback.Map<ActivityResult, Intent>(callback) {
            @Throws(Exception::class)
            override fun apply(t: ActivityResult): Intent {
                return t.intentWithResultCodeCheckAndGet(expectedResultCode)
            }
        })
    }

    @AnyThread
    @CheckResult
    fun navigateForIntentAndResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: (Intent) -> Unit,
    ): NavigationDisposable {
        return navigateForIntentAndResultCodeMatch(expectedResultCode = expectedResultCode,
            callback = object : BiCallback.BiCallbackAdapter<Intent>() {
                override fun onSuccess(result: RouterResult, targetValue: Intent) {
                    super.onSuccess(result, targetValue)
                    callback.invoke(targetValue)
                }
            })
    }

    /**
     * 匹配 resultCode 并且获取 Intent
     */
    @AnyThread
    @CheckResult
    suspend fun resultCodeMatchAndIntentAwait(
        expectedResultCode: Int = Activity.RESULT_OK,
    ): Intent {
        return activityResultAwait().intentWithResultCodeCheckAndGet(expectedResultCode = expectedResultCode)
    }

    // ---------------------------- 跳转 匹配 ResultCode ----------------------------

    /**
     * 跳转为了匹配 [ActivityResult] 中的 [ActivityResult.resultCode]
     *
     * @param callback           当 [ActivityResult] 拿到之后或者发生错误会回调
     * @param expectedResultCode 会匹配的 resultCode
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forwardForResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: Callback,
    ) {
        navigateForResultCodeMatch(
            expectedResultCode = expectedResultCode,
            callback = callback,
        )
    }

    @AnyThread
    fun forwardForResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: () -> Unit,
    ) {
        forwardForResultCodeMatch(
            expectedResultCode = expectedResultCode,
            callback = object : CallbackAdapter() {
                override fun onSuccess(result: RouterResult) {
                    super.onSuccess(result)
                    callback.invoke()
                }
            },
        )
    }

    /**
     * 跳转为了匹配 [ActivityResult] 中的 [ActivityResult.resultCode]
     *
     * @param callback           当 [ActivityResult] 拿到之后或者发生错误会回调
     * @param expectedResultCode 会匹配的 resultCode
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: Callback,
    ): NavigationDisposable {
        return navigateForResult(object : BiCallback<ActivityResult> {
            override fun onSuccess(result: RouterResult, targetValue: ActivityResult) {
                if (expectedResultCode == targetValue.resultCode) {
                    callback.onSuccess(result)
                } else {
                    callback.onError(
                        RouterErrorResult(
                            result.originalRequest,
                            ActivityResultException("the resultCode is not matching $expectedResultCode"),
                        )
                    )
                }
            }

            override fun onError(errorResult: RouterErrorResult) {
                callback.onError(errorResult)
            }

            override fun onCancel(originalRequest: RouterRequest?) {
                callback.onCancel(originalRequest)
            }
        })
    }

    @AnyThread
    @CheckResult
    fun navigateForResultCodeMatch(
        expectedResultCode: Int = Activity.RESULT_OK,
        callback: () -> Unit,
    ): NavigationDisposable {
        return navigateForResultCodeMatch(
            expectedResultCode = expectedResultCode,
            callback = object : CallbackAdapter() {
                override fun onSuccess(result: RouterResult) {
                    super.onSuccess(result)
                    callback.invoke()
                }
            },
        )
    }

    /**
     * 匹配 resultCode
     */
    @AnyThread
    suspend fun resultCodeMatchAwait(
        expectedResultCode: Int = Activity.RESULT_OK,
    ) {
        return activityResultAwait().resultCodeMatch(expectedResultCode = expectedResultCode)
    }

    // ---------------------------- 跳转 拿目标 Intent ----------------------------

    /**
     * 跳转, 获取目标的 Intent 的. 不会真正的发起跳转
     */
    @AnyThread
    @SuppressLint("CheckResult")
    fun forwardForTargetIntent(callback: BiCallback<Intent>) {
        navigateForTargetIntent(callback = callback)
    }

    @AnyThread
    fun forwardForTargetIntent(callback: (Intent) -> Unit) {
        forwardForTargetIntent(
            callback = object : BiCallback.BiCallbackAdapter<Intent>() {
                override fun onSuccess(result: RouterResult, targetValue: Intent) {
                    super.onSuccess(result, targetValue)
                    callback.invoke(targetValue)
                }
            },
        )
    }

    /**
     * 警告：切勿用作通知或者小部件的 Intent 使用
     * 此方法虽然在回调中返回了目标界面的真实 Intent, 但是是经过了整个路由的过程获取到的.
     * 甚至目标 Intent 都可能因为参数的不同而不同.
     * 所以通知或者小部件, 请使用如下方式, 如下方式不会发起路由. 只会返回一个代理的 Intent 供你使用.
     * 当你通过代理 Intent 发起跳转之后, 会发起真实的路由过程
     * ```
     * val targetProxyIntent = Router
     *       .newProxyIntentBuilder()
     *       .putString("name", "testName")
     *       // .....
     *       .buildProxyIntent();
     * ```
     * 跳转, 获取目标的 Intent 的. 不会真正的发起跳转
     * @return 可用于取消本次路由
     */
    @AnyThread
    @CheckResult
    fun navigateForTargetIntent(callback: BiCallback<Intent>): NavigationDisposable {
        return navigateForResult(
            callback = object : BiCallback.Map<ActivityResult, Intent>(callback) {
                @Throws(Exception::class)
                override fun apply(t: ActivityResult): Intent {
                    return t.intentCheckAndGet()
                }
            }
        )
    }

    @AnyThread
    @CheckResult
    fun navigateForTargetIntent(callback: (Intent) -> Unit): NavigationDisposable {
        return navigateForTargetIntent(
            callback = object : BiCallback.BiCallbackAdapter<Intent>() {
                override fun onSuccess(result: RouterResult, targetValue: Intent) {
                    super.onSuccess(result, targetValue)
                    callback.invoke(targetValue)
                }
            },
        )
    }

}