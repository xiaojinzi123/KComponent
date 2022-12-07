package com.xiaojinzi.component.user.module.user_center.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
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
    hostAndPath = RouterConfig.USER_USER_CENTER,
    interceptorNames = [RouterConfig.INTERCEPTOR_PERMISSION_LOGIN],
)
@ViewLayer
class UserCenterAct : BaseActivity<UserCenterViewModel>() {

    override fun getViewModelClass(): Class<UserCenterViewModel> {
        return UserCenterViewModel::class.java
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
        }

        setContent {
            CommonTheme {
                StateBar {
                    UserCenterViewWrap()
                }
            }
        }

    }

}