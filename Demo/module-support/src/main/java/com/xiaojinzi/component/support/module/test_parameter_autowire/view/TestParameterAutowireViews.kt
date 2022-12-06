package com.xiaojinzi.component.support.module.test_parameter_autowire.view

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.base.theme.SPECIFIC_FF3A373A
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
private fun TestParameterAutowireView() {
    val context = LocalContext.current
    val vm: TestParameterAutowireViewModel = viewModel()
    val data1 by vm.data1InitData.valueStateFlow.collectAsState(initial = null)
    val data2 by vm.data2InitData.valueStateFlow.collectAsState(initial = null)
    val name by vm.nameInitData.valueStateFlow.collectAsState(initial = null)
    val pass by vm.passInitData.valueStateFlow.collectAsState(initial = null)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .nothing(),
            text = "data1:$data1\ndata2:$data2\nname: $name\npass: $pass",
            style = TextStyle(
                fontSize = 24.sp,
                color = SPECIFIC_FF3A373A,
                fontWeight = FontWeight.Normal,
            ),
            textAlign = TextAlign.Start,
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
fun TestParameterAutowireViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "测试参数注入".toStringItemDto(),
            )
        }
    ) {
        TestParameterAutowireView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun TestParameterAutowireViewPreview() {
    TestParameterAutowireView()
}