package com.xiaojinzi.component.user.module.login.view

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.base.theme.SPECIFIC_7AB567B5
import com.xiaojinzi.component.base.theme.SPECIFIC_FF3A373A
import com.xiaojinzi.component.base.theme.SPECIFIC_FF958F95
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
private fun LoginView() {
    val context = LocalContext.current
    val vm: LoginViewModel = viewModel()
    val userName by vm.userNameObservableDto.collectAsState(initial = "")
    val userPassword by vm.userPasswordObservableDto.collectAsState(initial = "")
    val canNext by vm.canNextObservableDto.collectAsState(initial = false)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nothing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(
            modifier = Modifier
                .height(height = 60.dp)
                .nothing()
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 48.dp, vertical = 0.dp)
                .fillMaxWidth()
                .nothing(),
            value = userName,
            onValueChange = {
                vm.userNameObservableDto.value = it.trim()
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = SPECIFIC_FF3A373A,
                fontWeight = FontWeight.Normal,
            ),
            placeholder = {
                Text(
                    text = "请输入用户名",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = SPECIFIC_FF958F95,
                        fontWeight = FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Center,
                )
            },
        )

        Spacer(
            modifier = Modifier
                .height(height = 20.dp)
                .nothing()
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 48.dp, vertical = 0.dp)
                .fillMaxWidth()
                .nothing(),
            value = userPassword,
            onValueChange = {
                vm.userPasswordObservableDto.value = it.trim()
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = SPECIFIC_FF3A373A,
                fontWeight = FontWeight.Normal,
            ),
            placeholder = {
                Text(
                    text = "请输入密码",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = SPECIFIC_FF958F95,
                        fontWeight = FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Center,
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
            )
        )

        Spacer(
            modifier = Modifier
                .height(height = 16.dp)
                .nothing()
        )

        Button(
            modifier = Modifier
                .padding(horizontal = 48.dp, vertical = 0.dp)
                .fillMaxWidth()
                .nothing(),
            enabled = canNext,
            onClick = {
                vm.login(context = context)
            },
        ) {
            Text(
                text = "登录",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                ),
                textAlign = TextAlign.Center,
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
fun LoginViewWrap() {
    Scaffold(
        topBar = {
            AppbarNormal(
                title = "登录".toStringItemDto(),
            )
        }
    ) {
        LoginView()
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun LoginViewPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .nothing(),
    ) {
        LoginView()
    }
}