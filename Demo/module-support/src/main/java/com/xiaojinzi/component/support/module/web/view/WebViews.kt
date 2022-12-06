package com.xiaojinzi.component.support.module.web.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.web.rememberWebViewState
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
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
            val state = rememberWebViewState(url = it)
            com.google.accompanist.web.WebView(
                modifier = Modifier
                    .fillMaxSize()
                    .nothing(),
                state = state,
                captureBackPresses = false,
            )
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
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun WebViewPreview() {
    WebView()
}