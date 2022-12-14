package com.xiaojinzi.component.demo.module.test_route.view

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.api.RouterApi
import com.xiaojinzi.component.base.interceptor.DialogShowInterceptor
import com.xiaojinzi.component.base.interceptor.ErrorRouterInterceptor
import com.xiaojinzi.component.base.view.ActionButton
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.bean.ActivityResult
import com.xiaojinzi.component.impl.*
import com.xiaojinzi.component.support.CallbackAdapter
import com.xiaojinzi.support.ktx.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
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
            mainAxisSpacing = 6.dp,
            crossAxisSpacing = 2.dp,
        ) {
            ActionButton(text = "????????????") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .interceptors(DialogShowInterceptor::class)
                    .navigate(
                        callback = object : CallbackAdapter() {
                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(app, "???????????????", Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                Toast.makeText(app, "????????????", Toast.LENGTH_SHORT).show()
                            }

                            override fun onEvent(
                                successResult: RouterResult?,
                                errorResult: RouterErrorResult?
                            ) {
                                super.onEvent(successResult, errorResult)
                            }
                        }
                    )
                    // ????????????
                    .cancel()
            }
            ActionButton(text = "??????????????????") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(app, "???????????????", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                context.tryFinishActivity()
            }
            ActionButton(text = "????????????????????????") {
                Router.with(context = context)
                    .autoCancel(autoCancel = false)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onCancel(originalRequest: RouterRequest?) {
                                super.onCancel(originalRequest)
                                Toast.makeText(app, "???????????????", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                context.tryFinishActivity()
            }
            ActionButton(text = "??????????????????") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.APP_TEST_NO_TARGET)
                    .forward(
                        callback = object : CallbackAdapter() {
                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                errorResult.error.printStackTrace()
                                Toast.makeText(
                                    app,
                                    "???????????????:${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            }
            ActionButton(text = "???????????????????????????") {
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
                                    "???????????????:${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            }
            ActionButton(text = "?????? Fragment Route") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.APP_FRAGMENT_ROUTE_TEST)
                    .forward()
            }
            ActionButton(text = "???????????????") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward()
            }
            ActionButton(text = "????????????") {
                Router.with(context = context)
                    .url(url = "https://www.baidu.com")
                    .forward()
            }
            ActionButton(text = "???????????????????????????????????????") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .interceptors(DialogShowInterceptor::class)
                    .forward()
            }
            ActionButton(text = "???????????????????????? ActivityResult") {
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
                                    "????????????????????? ActivityResult ??????, ??????????????????${
                                        targetValue.data?.getStringExtra("data")
                                    }",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onError(errorResult: RouterErrorResult) {
                                super.onError(errorResult)
                                Toast.makeText(
                                    app,
                                    "????????????????????? ActivityResult ??????, ??????????????????${errorResult.error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            }
            ActionButton(text = "RxJava ???????????????????????? ActivityResult") {
                val disposable = Router
                    .withApi(apiClass = RouterApi::class)
                    .toTestActivityResultView1111(
                        context = context,
                    ).subscribe(
                        { targetValue ->
                            Toast.makeText(
                                app,
                                "????????????????????? ActivityResult ??????, ??????????????????${
                                    targetValue.data?.getStringExtra("data")
                                }",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, { error ->
                            Toast.makeText(
                                app,
                                "????????????????????? ActivityResult ??????, ??????????????????${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
            }
            ActionButton(text = "suspend ????????????") {
                scope.launch(context = ErrorIgnoreContext) {
                    try {
                        Router
                            .withApi(apiClass = RouterApi::class)
                            .toTestActivityResultView3(
                                context = context,
                            )
                        Toast.makeText(
                            app,
                            "????????????",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            app,
                            "????????????, ??????????????????${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            ActionButton(text = "suspend ?????????????????? ResultCode") {
                scope.launch(context = ErrorIgnoreContext) {
                    try {
                        Router
                            .withApi(apiClass = RouterApi::class)
                            .requestPermission(
                                context = context,
                                permission = Manifest.permission.CALL_PHONE,
                                permissionDesc = "??????????????????",
                            )
                        Toast.makeText(
                            app,
                            "??????????????????",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            app,
                            "??????????????????, ??????????????????${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            ActionButton(text = "suspend ???????????????????????? ActivityResult") {
                scope.launch(context = ErrorIgnoreContext) {
                    try {
                        val targetActivityResult = Router
                            .withApi(apiClass = RouterApi::class)
                            .toTestActivityResultView111(context = context)
                        Toast.makeText(
                            app,
                            "????????????????????? ActivityResult ??????, ??????????????????${
                                targetActivityResult.data?.getStringExtra("data")
                            }",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            app,
                            "????????????????????? ActivityResult ??????, ??????????????????${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            ActionButton(text = "?????? afterActivityResultRouteSuccessAction ??????") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
                    .requestCodeRandom()
                    .afterActivityResultRouteSuccessAction {
                        Toast.makeText(
                            app,
                            "afterActivityResultRouteSuccessAction ????????????",
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
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun TestRouteViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "????????????".toStringItemDto(),
            )
        }
    ) {
        TestRouteView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun TestRouteViewPreview() {
    TestRouteView()
}