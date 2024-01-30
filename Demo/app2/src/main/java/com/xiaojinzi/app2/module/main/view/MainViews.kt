package com.xiaojinzi.app2.module.main.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.view.ActionButton
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.application.ModuleManager
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
private fun MainView() {
    val context = LocalContext.current
    val vm: MainViewModel = viewModel()
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
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 2.dp,
        ) {

            ActionButton(text = "加载 app2 模块") {
                ModuleManager.register(
                    moduleName = "app2",
                )
            }

            ActionButton(text = "卸载 app2 模块") {
                ModuleManager.unregister(
                    moduleName = "app2",
                )
            }

            ActionButton(text = "加载 support 模块") {
                ModuleManager.register(
                    moduleName = "support",
                )
            }

            ActionButton(text = "卸载 support 模块") {
                ModuleManager.unregister(
                    moduleName = "support",
                )
            }

            ActionButton(text = "加载 user 模块") {
                ModuleManager.register(
                    moduleName = "user",
                )
            }

            ActionButton(text = "卸载 user 模块") {
                ModuleManager.unregister(
                    moduleName = "user",
                )
            }

        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .nothing(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 2.dp,
        ) {

            ActionButton(text = "去登录界面") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                    .forward()
            }

            ActionButton(text = "去用户中心") {
                Router.with(context = context)
                    .hostAndPath(hostAndPath = RouterConfig.USER_USER_CENTER)
                    .forward()
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
fun MainViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                backIconRsd = null,
                title = "KComponent".toStringItemDto(),
            )
        }
    ) {
        MainView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun MainViewPreview() {
    MainView()
}