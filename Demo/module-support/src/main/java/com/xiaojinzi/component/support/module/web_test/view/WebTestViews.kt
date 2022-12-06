package com.xiaojinzi.component.support.module.web_test.view

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@SuppressLint("SetJavaScriptEnabled")
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
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

        val client: AccompanistWebViewClient = remember {
            AccompanistWebViewClient()
        }

        WebView(
            state = rememberWebViewState(url = "file:///android_asset/index.html"),
            captureBackPresses = false,
            client = client,
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
                }
            }
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
fun WebTestViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "Web 测试".toStringItemDto(),
            )
        }
    ) {
        WebTestView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun WebTestViewPreview() {
    WebTestView()
}