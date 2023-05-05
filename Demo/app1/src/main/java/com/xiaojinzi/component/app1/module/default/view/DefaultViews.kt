package com.xiaojinzi.component.app1.module.default.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.pager.ExperimentalPagerApi
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
private fun DefaultView() {
    val context = LocalContext.current
    val vm: DefaultViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(com.xiaojinzi.component.lib.resource.R.raw.lottie_anim1)
        )
        LottieAnimation(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .aspectRatio(ratio = 1f)
                .nothing(),
            composition = composition,
            iterations = LottieConstants.IterateForever,
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
fun DefaultViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "App 默认降级界面".toStringItemDto(),
            )
        }
    ) {
        DefaultView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun DefaultViewPreview() {
    DefaultView()
}