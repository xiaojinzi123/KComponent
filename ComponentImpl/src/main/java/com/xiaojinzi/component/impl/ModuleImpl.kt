package com.xiaojinzi.component.impl

import android.app.Application
import androidx.annotation.CallSuper
import com.xiaojinzi.component.application.IApplicationLifecycle
import com.xiaojinzi.component.bean.RouterBean
import com.xiaojinzi.component.bean.RouterDegradeBean
import com.xiaojinzi.component.impl.interceptor.InterceptorBean

abstract class ModuleImpl : IModuleLifecycle {

    private var isInit = false
    private var moduleApplicationList: List<IApplicationLifecycle>? = null

    @CallSuper
    override fun onCreate(app: Application) {
        if (isInit) {
            throw RuntimeException("ModuleImpl can only be initialized once")
        }
        initSpi(application = app)
        moduleApplicationList = initApplication()
        moduleApplicationList?.forEach {
            it.onCreate(app = app)
        }
        isInit = true
    }

    override fun onDestroy() {
        moduleApplicationList?.forEach {
            it.onDestroy()
        }
        destroySpi()
        isInit = false
        moduleApplicationList = null
    }

}