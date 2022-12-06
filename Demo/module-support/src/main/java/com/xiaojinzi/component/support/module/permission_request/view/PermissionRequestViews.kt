package com.xiaojinzi.component.support.module.permission_request.view

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.theme.BLACK_ALPHA_50
import com.xiaojinzi.component.base.theme.SPECIFIC_FF3A373A
import com.xiaojinzi.component.base.theme.SPECIFIC_FFFF56B0
import com.xiaojinzi.component.base.view.ActionButton
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.support.ktx.getActivity
import com.xiaojinzi.support.ktx.nothing
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
private fun PermissionRequestView() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val vm: PermissionRequestViewModel = viewModel()
    val permission by vm.permissionInitData.valueStateFlow.collectAsState(initial = null)
    val permissionDesc by vm.permissionDescInitData.valueStateFlow.collectAsState(initial = null)
    BackHandler {
    }
    if (permission.isNullOrEmpty().not()) {
        val permissionState = rememberPermissionState(permission = permission!!)
        when (permissionState.status) {
            PermissionStatus.Granted -> {
                context.getActivity()?.let { act ->
                    scope.launch {
                        delay(2000)
                        act.setResult(Activity.RESULT_OK)
                        act.finish()
                    }
                }
            }
            else -> {}
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BLACK_ALPHA_50)
                .nothing(),
            contentAlignment = Alignment.Center,
        ) {

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 24.dp, vertical = 0.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = Color.White)
                    .nothing(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier
                        .height(height = 24.dp)
                        .nothing()
                )

                when (val state = permissionState.status) {
                    is PermissionStatus.Denied -> {
                        Text(
                            modifier = Modifier
                                .wrapContentSize()
                                .nothing(),
                            text = "权限申请未成功",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = SPECIFIC_FFFF56B0,
                                fontWeight = FontWeight.Normal,
                            ),
                            textAlign = TextAlign.Start,
                        )
                    }
                    PermissionStatus.Granted -> {
                        Text(
                            modifier = Modifier
                                .wrapContentSize()
                                .nothing(),
                            text = "权限申请成功, 2 秒后返回",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = SPECIFIC_FFFF56B0,
                                fontWeight = FontWeight.Normal,
                            ),
                            textAlign = TextAlign.Start,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(height = 12.dp)
                        .nothing()
                )

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .nothing(),
                    text = permissionDesc ?: "",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = SPECIFIC_FF3A373A,
                        fontWeight = FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Start,
                )

                Spacer(
                    modifier = Modifier
                        .height(height = 24.dp)
                        .nothing()
                )

                ActionButton(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 0.dp)
                        .fillMaxWidth()
                        .nothing(),
                    text = "申请",
                ) {
                    when (val state = permissionState.status) {
                        is PermissionStatus.Denied -> {
                            if (state.shouldShowRationale) {

                            } else {

                            }
                            permissionState.launchPermissionRequest()
                        }
                        else -> {}
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(height = 14.dp)
                        .nothing()
                )

                ActionButton(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 0.dp)
                        .fillMaxWidth()
                        .nothing(),
                    text = "去设置",
                ) {
                    Router.with(context = context)
                        .hostAndPath(hostAndPath = RouterConfig.SYSTEM_APP_DETAIL)
                        .forward()
                }

                Spacer(
                    modifier = Modifier
                        .height(height = 14.dp)
                        .nothing()
                )

            }

        }
    }

}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun PermissionRequestViewWrap() {
    PermissionRequestView()
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun PermissionRequestViewPreview() {
    PermissionRequestView()
}