package com.xiaojinzi.component.user.module.user_center.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.base.theme.SPECIFIC_FF3A373A
import com.xiaojinzi.component.base.view.AppbarNormal
import com.xiaojinzi.component.impl.service.service
import com.xiaojinzi.support.ktx.nothing
import com.xiaojinzi.support.ktx.toStringItemDto
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flowOf

@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun UserCenterView() {
    val context = LocalContext.current
    val selfUserInfo by (UserSpi::class.service()?.userInfoObservableDto
        ?: flowOf(null)).collectAsState(null)
    val vm: UserCenterViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(
            modifier = Modifier
                .height(height = 200.dp)
                .nothing()
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .nothing(),
            text = "当前登录用户名：${selfUserInfo?.name}\n密码是：${selfUserInfo?.password}",
            style = TextStyle(
                fontSize = 16.sp,
                color = SPECIFIC_FF3A373A,
                fontWeight = FontWeight.Normal,
            ),
            textAlign = TextAlign.Center,
        )

    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun UserCenterViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "用户中心".toStringItemDto(),
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
            UserCenterView()
        }
    }
}

@InternalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun UserCenterViewPreview() {
    UserCenterView()
}