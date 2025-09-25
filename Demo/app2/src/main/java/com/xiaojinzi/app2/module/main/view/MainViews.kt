package com.xiaojinzi.app2.module.main.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.view.ActionButton
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.application.ModuleManager
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalAnimationApi
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
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(
                space = 2.dp,
            ),
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
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(
                space = 2.dp,
            ),
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
@ExperimentalAnimationApi
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                )
                .nothing(),
        ) {
            MainView()
        }
    }
}

@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun MainViewPreview() {
    MainView()
}