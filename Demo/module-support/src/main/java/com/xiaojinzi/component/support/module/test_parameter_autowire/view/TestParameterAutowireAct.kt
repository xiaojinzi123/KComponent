package com.xiaojinzi.component.support.module.test_parameter_autowire.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
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
    hostAndPath = RouterConfig.SUPPORT_TEST_INJECT
)
@RouterAnno(
    hostAndPath = RouterConfig.SUPPORT_TEST_QUERY
)
@RouterAnno(
    hostAndPath = RouterConfig.SUPPORT_TEST_PARAMETER_AUTOWIRE,
)
@ViewLayer
class TestParameterAutowireAct : BaseActivity<TestParameterAutowireViewModel>() {

    @AttrValueAutowiredAnno
    var data1: String? = null

    @AttrValueAutowiredAnno
    var data2: String? = null

    @AttrValueAutowiredAnno
    var name: String? = null

    @AttrValueAutowiredAnno
    var pass: String? = null

    override fun getViewModelClass(): Class<TestParameterAutowireViewModel> {
        return TestParameterAutowireViewModel::class.java
    }

    @OptIn(
        InternalCoroutinesApi::class, ExperimentalMaterialApi::class,
        ExperimentalAnimationApi::class, ExperimentalPagerApi::class,
        ExperimentalFoundationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.translateStatusBar()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        initOnceUseViewModel {
            requiredViewModel().apply {
                this.data1InitData.value = data1
                this.data2InitData.value = data1
                this.nameInitData.value = name
                this.passInitData.value = pass
            }
        }

        setContent {
            CommonTheme {
                StateBar {
                    TestParameterAutowireViewWrap()
                }
            }
        }

    }

}