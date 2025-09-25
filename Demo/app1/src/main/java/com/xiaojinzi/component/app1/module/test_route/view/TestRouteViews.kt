package com.xiaojinzi.component.app1.module.test_route.view

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.api.RouterApi
import com.xiaojinzi.component.base.interceptor.DialogShowInterceptor
import com.xiaojinzi.component.base.interceptor.ErrorRouterInterceptor
import com.xiaojinzi.component.base.view.ActionButton
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.bean.ActivityResult
import com.xiaojinzi.component.impl.BiCallback
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.RouterErrorResult
import com.xiaojinzi.component.impl.RouterRequest
import com.xiaojinzi.component.impl.RouterResult
import com.xiaojinzi.component.support.CallbackAdapter
import com.xiaojinzi.support.ktx.AppScope
import com.xiaojinzi.support.ktx.ErrorIgnoreContext
import com.xiaojinzi.support.ktx.app
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import com.xiaojinzi.support.ktx.tryFinishActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun TestRouteView() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val vm: TestRouteViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .nothing(),
            horizontalArrangement = Arrangement.spacedBy(
                6.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(
                2.dp,
            ),
        ) {
            ActionButton(text = "手动取消") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .interceptors(DialogShowInterceptor::class)
                    .navigate(
                        callback = object : CallbackAdapter() {
                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(app, "路由被取消", Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                Toast.makeText(
                                    app,
                                    "路由错误: ${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onEvent(
                                successResult: RouterResult?,
                                errorResult: RouterErrorResult?
                            ) {
                                super.onEvent(successResult, errorResult)
                            }
                        }
                    )
                    // 直接取消
                    .cancel()
            }
            ActionButton(text = "测试自动取消") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(
                                    app,
                                    "路由被取消, originalRequest 是否为空?: ${originalRequest == null}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                context.tryFinishActivity()
            }
            ActionButton(text = "跳转获取 Activity, 一秒后取消") {
                Router
                    .withApi(apiClass = RouterApi::class)
                    .toTestActivityResultView1(
                        context = context,
                        object : BiCallback.BiCallbackAdapter<ActivityResult>() {
                            override fun onSuccess(
                                result: RouterResult,
                                targetValue: ActivityResult
                            ) {
                                super.onSuccess(result, targetValue)
                                Toast.makeText(
                                    app,
                                    "获取目标界面的 ActivityResult 成功, 返回信息为：${
                                        targetValue.data?.getStringExtra("data")
                                    }",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                Toast.makeText(
                                    app,
                                    "获取目标界面的 ActivityResult 失败, 失败信息为：${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(
                                    app,
                                    "获取目标界面的 ActivityResult 被取消",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                scope.launch {
                    delay(1000)
                    context.tryFinishActivity()
                }
            }
            ActionButton(text = "测试关闭自动取消") {
                Router.with(context = context)
                    .autoCancel(autoCancel = false)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(app, "路由被取消", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                context.tryFinishActivity()
            }
            ActionButton(text = "测试错误目标") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.APP_TEST_NO_TARGET)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                errorResult.error.printStackTrace()
                                Toast.makeText(
                                    app,
                                    "路由错误啦:${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            }
            ActionButton(text = "测试拦截器发生错误") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.APP_TEST_NO_TARGET)
                    .interceptors(ErrorRouterInterceptor::class)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                errorResult.error.printStackTrace()
                                Toast.makeText(
                                    app,
                                    "路由错误啦:${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            }
            ActionButton(text = "测试 Fragment Route") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.APP_FRAGMENT_ROUTE_TEST)
                    .forward()
            }
            ActionButton(text = "去登录界面") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward()
            }
            ActionButton(text = "打开百度") {
                Router.with(context = context)
                    .url(url = "https://www.baidu.com")
                    .forward()
            }
            ActionButton(text = "去登录界面模拟执行耗时任务") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .interceptors(DialogShowInterceptor::class)
                    .forward()
            }
            ActionButton(text = "测试获取目标界面 ActivityResult") {
                Router
                    .withApi(apiClass = RouterApi::class)
                    .toTestActivityResultView1(
                        context = context,
                        object : BiCallback.BiCallbackAdapter<ActivityResult>() {
                            override fun onSuccess(
                                result: RouterResult,
                                targetValue: ActivityResult
                            ) {
                                super.onSuccess(result, targetValue)
                                Toast.makeText(
                                    app,
                                    "获取目标界面的 ActivityResult 成功, 返回信息为：${
                                        targetValue.data?.getStringExtra("data")
                                    }",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                Toast.makeText(
                                    app,
                                    "获取目标界面的 ActivityResult 失败, 失败信息为：${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            }
            ActionButton(text = "RxJava 测试获取目标界面 ActivityResult") {
                val disposable = Router
                    .withApi(apiClass = RouterApi::class)
                    .toTestActivityResultView1111(
                        context = context,
                    ).subscribe(
                        { targetValue ->
                            Toast.makeText(
                                app,
                                "获取目标界面的 ActivityResult 成功, 返回信息为：${
                                    targetValue.data?.getStringExtra("data")
                                }",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, { error ->
                            Toast.makeText(
                                app,
                                "获取目标界面的 ActivityResult 失败, 失败信息为：${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
            }
            ActionButton(text = "suspend 完成路由") {
                scope.launch(context = ErrorIgnoreContext) {
                    try {
                        Router
                            .withApi(apiClass = RouterApi::class)
                            .toTestActivityResultView3(
                                context = context,
                            )
                        Toast.makeText(
                            app,
                            "路由成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            app,
                            "路由失败, 失败信息为：${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            ActionButton(text = "suspend 申请权限匹配 ResultCode") {
                scope.launch(context = ErrorIgnoreContext) {
                    try {
                        Router
                            .withApi(apiClass = RouterApi::class)
                            .requestPermission(
                                context = context,
                                permission = Manifest.permission.CALL_PHONE,
                                permissionDesc = "打电话的权限",
                            )
                        Toast.makeText(
                            app,
                            "申请权限成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            app,
                            "申请权限失败, 失败信息为：${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            ActionButton(text = "suspend 测试获取目标界面 ActivityResult") {
                AppScope.launch(context = ErrorIgnoreContext + Dispatchers.Main) {
                    try {
                        val targetActivityResult = Router
                            .withApi(apiClass = RouterApi::class)
                            .toTestActivityResultView111(context = context)
                        Toast.makeText(
                            app,
                            "获取目标界面的 ActivityResult 成功, 返回信息为：${
                                targetActivityResult.data?.getStringExtra("data")
                            }",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            app,
                            "获取目标界面的 ActivityResult 失败, 失败信息为：${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            ActionButton(text = "测试 afterActivityResultRouteSuccessAction 回调") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
                    .requestCodeRandom()
                    .afterActivityResultRouteSuccessAction {
                        Toast.makeText(
                            app,
                            "afterActivityResultRouteSuccessAction 被调用了",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .forwardForResult { }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun TestRouteViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "跳转测试".toStringItemDto(),
            )
        }
    ) {
        TestRouteView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun TestRouteViewPreview() {
    TestRouteView()
}