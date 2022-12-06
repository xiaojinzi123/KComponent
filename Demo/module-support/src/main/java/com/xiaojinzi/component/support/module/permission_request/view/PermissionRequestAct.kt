package com.xiaojinzi.component.support.module.permission_request.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.shouldShowRationale
import com.xiaojinzi.component.anno.AttrValueAutowiredAnno
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.theme.CommonTheme
import com.xiaojinzi.component.base.view.BaseActivity
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.compose.StateBar
import com.xiaojinzi.support.ktx.initOnceUseViewModel
import com.xiaojinzi.support.ktx.translateStatusBar
import kotlinx.coroutines.InternalCoroutinesApi

@RouterAnno(
    hostAndPath = RouterConfig.SUPPORT_PERMISSION_REQUEST
)
@ViewLayer
class PermissionRequestAct : BaseActivity<PermissionRequestViewModel>() {

    @AttrValueAutowiredAnno
    lateinit var permission: String

    @AttrValueAutowiredAnno
    lateinit var permissionDesc: String

    override fun getViewModelClass(): Class<PermissionRequestViewModel> {
        return PermissionRequestViewModel::class.java
    }

    @OptIn(
        InternalCoroutinesApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalPagerApi::class,
        ExperimentalFoundationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.translateStatusBar()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        initOnceUseViewModel {
            requiredViewModel().apply {
                this.permissionInitData.value = permission
                this.permissionDescInitData.value = permissionDesc
            }
        }

        setContent {
            CommonTheme {
                StateBar {
                    PermissionRequestViewWrap()
                }
            }
        }

    }

}