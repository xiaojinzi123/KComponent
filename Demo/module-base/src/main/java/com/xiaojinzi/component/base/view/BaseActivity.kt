package com.xiaojinzi.component.base.view

import android.os.Bundle
import com.xiaojinzi.component.Component
import com.xiaojinzi.support.architecture.mvvm1.BaseAct
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

open class BaseActivity<VM : BaseViewModel>: BaseAct<VM>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Component.inject(target = this)
    }

}