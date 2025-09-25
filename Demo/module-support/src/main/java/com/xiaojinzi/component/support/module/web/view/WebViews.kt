package com.xiaojinzi.component.support.module.web.view

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun WebView() {
    val context = LocalContext.current
    val vm: WebViewModel = viewModel()
    val targetUrl by vm.urlInitData.valueStateFlow.collectAsState(initial = null)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        targetUrl?.let {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .nothing(),
                factory = { context ->
                    WebView(context)
                },
                update = { webView ->
                    webView.loadUrl(it)
                },
                onRelease = { webView ->
                },
            )
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun WebViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "浏览器".toStringItemDto(),
            )
        }
    ) {
        WebView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun WebViewPreview() {
    WebView()
}