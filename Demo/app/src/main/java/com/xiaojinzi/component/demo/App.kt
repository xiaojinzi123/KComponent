package com.xiaojinzi.component.demo

import android.app.Application
import com.xiaojinzi.component.Component
import com.xiaojinzi.component.Config
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.impl.*
import com.xiaojinzi.component.impl.service.serviceRequired
import com.xiaojinzi.component.support.ASMUtil
import com.xiaojinzi.support.init.AppInstance
import com.xiaojinzi.support.ktx.LogSupport

/**
 *
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AppInstance.app = this

        LogSupport.d(
            tag = "demoApp",
            content = "moduleNames = ${ASMUtil.getModuleNames()}"
        )

        Component.init(
            application = this,
            isDebug = BuildConfig.DEBUG,
            config = Config.Builder()
                .optimizeInit(isOptimizeInit = true)
                .autoRegisterModule(isAutoRegisterModule = true)
                .build()
        )

        UserSpi::class.serviceRequired()

        /*ModuleManager.registerArr(
            RouterConfig.HOST_BASE,
            RouterConfig.HOST_APP,
            RouterConfig.HOST_USER,
            RouterConfig.HOST_SUPPORT,
        )*/

        Router.addRouterListener(
            listener = object : RouterListener {
                override fun onSuccess(successResult: RouterResult) {
                    LogSupport.d(
                        tag = "RouterListener",
                        content = "onSuccess, url = ${successResult.originalRequest.uri}"
                    )
                }

                override fun onActivityResultSuccess(successResult: ActivityResultRouterResult) {
                    LogSupport.d(
                        tag = "RouterListener",
                        content = "onActivityResultSuccess, url = ${successResult.routerResult.originalRequest.uri}"
                    )
                }

                override fun onError(errorResult: RouterErrorResult) {
                    LogSupport.d(
                        tag = "RouterListener",
                        content = "onError, url = ${errorResult.originalRequest?.uri}"
                    )
                }

                override fun onCancel(originalRequest: RouterRequest) {
                    LogSupport.d(
                        tag = "RouterListener",
                        content = "onCancel, url = ${originalRequest.uri}"
                    )
                }
            }
        )

        if (BuildConfig.DEBUG) {
            Component.check()
        }

    }

}