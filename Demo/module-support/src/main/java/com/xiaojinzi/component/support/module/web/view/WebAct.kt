package com.xiaojinzi.component.support.module.web.view

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.anno.UriAutowiredAnno
import com.xiaojinzi.component.base.theme.CommonTheme
import com.xiaojinzi.component.base.view.BaseActivity
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.compose.StateBar
import com.xiaojinzi.support.ktx.initOnceUseViewModel
import com.xiaojinzi.support.ktx.translateStatusBar
import kotlinx.coroutines.InternalCoroutinesApi

@RouterAnno(
    regex = "^(http|https)(.*)",
    scheme = "testScheme",
    hostAndPath = "xxx/xxx",
)
@ViewLayer
class WebAct : BaseActivity<WebViewModel>() {

    @UriAutowiredAnno
    lateinit var uri: Uri

    override fun getViewModelClass(): Class<WebViewModel> {
        return WebViewModel::class.java
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
            requiredViewModel().urlInitData.value = uri.toString()
        }

        setContent {
            CommonTheme {
                StateBar {
                    WebViewWrap()
                }
            }
        }

    }

}