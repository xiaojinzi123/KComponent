package com.xiaojinzi.component.support.module.web_test.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@SuppressLint("SetJavaScriptEnabled")
@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun WebTestView() {
    val context = LocalContext.current
    val vm: WebTestViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidView(
            factory = { context ->
                class JavascriptInterface {

                    /**
                     * 打开网页
                     */
                    @android.webkit.JavascriptInterface
                    fun openUrl(url: String) {
                        Router
                            .with(context)
                            .url(url = url)
                            .forward()
                    }

                }
                android.webkit.WebView(context).apply {
                    this.settings.javaScriptEnabled = true
                    this.addJavascriptInterface(
                        JavascriptInterface(),
                        "testWebRouter",
                    )
                    this.loadUrl("file:///android_asset/index.html")
                }
            },
        )
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun WebTestViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "Web 测试".toStringItemDto(),
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
            WebTestView()
        }
    }
}

@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun WebTestViewPreview() {
    WebTestView()
}