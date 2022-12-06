package com.xiaojinzi.component.support.module.test_activity_result.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.base.view.ActionButton
import com.xiaojinzi.component.base.view.AppbarNormal
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
private fun TestActivityResultView() {
    val context = LocalContext.current
    val vm: TestActivityResultViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActionButton(
            modifier = Modifier
                .padding(horizontal = 36.dp, vertical = 0.dp)
                .fillMaxWidth()
                .nothing(),
            text = "返回",
        ) {
            context.getFragmentActivity()?.let {
                it.setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                        this.putExtra("data", "我是返回的数据")
                    }
                )
                it.finish()
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
fun TestActivityResultViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "测试返回 ActivityResult".toStringItemDto(),
            )
        }
    ) {
        TestActivityResultView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun TestActivityResultViewPreview() {
    TestActivityResultView()
}