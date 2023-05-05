package com.xiaojinzi.app2.module.main.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.anno.ServiceAutowiredAnno
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.base.theme.CommonTheme
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseAct
import com.xiaojinzi.support.compose.StateBar
import com.xiaojinzi.support.ktx.initOnceUseViewModel
import com.xiaojinzi.support.ktx.translateStatusBar
import kotlinx.coroutines.InternalCoroutinesApi

@RouterAnno(
    hostAndPath = RouterConfig.APP2_MAIN,
)
@ViewLayer
class MainAct : BaseAct<MainViewModel>() {

    @ServiceAutowiredAnno
    lateinit var userSpi: UserSpi

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    @OptIn(
        ExperimentalFoundationApi::class,
        InternalCoroutinesApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalPagerApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.translateStatusBar()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        intent.extras?.let { bundle ->
            startProxyRouter(bundle = bundle)
        }

        initOnceUseViewModel {
        }

        setContent {
            CommonTheme {
                StateBar {
                    MainViewWrap()
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.extras?.let { bundle ->
            startProxyRouter(bundle = bundle)
        }
    }

    private fun startProxyRouter(bundle: Bundle) {
        if (Router.isProxyIntentExist(bundle)) {
            Router.with(this)
                .proxyBundle(bundle)
                .forward()
        }
    }

}