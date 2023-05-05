package com.xiaojinzi.component.app1.module.fragment_route_test.view

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.support.ktx.getFragmentActivity
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
private fun FragmentRouteTestView() {
    val context = LocalContext.current
    val vm: FragmentRouteTestViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidView(
            factory = { context ->
                FrameLayout(context).apply {
                    this.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
                    this.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
                    this.id = View.generateViewId()
                    Router
                        .with(fragmentFlag = RouterConfig.FRAGMENT_USER1)
                        .putString(key = "data", value = "xxxxxx")
                        .navigate()?.let { fragment ->
                            context
                                .getFragmentActivity()
                                ?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(this.id, fragment)
                                ?.commit()
                        }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .nothing(),
        )
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun FragmentRouteTestViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "Fragment 路由测试".toStringItemDto(),
            )
        }
    ) {
        FragmentRouteTestView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun FragmentRouteTestViewPreview() {
    FragmentRouteTestView()
}