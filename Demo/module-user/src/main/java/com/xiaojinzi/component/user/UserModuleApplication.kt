package com.xiaojinzi.component.user

import android.app.Application
import com.xiaojinzi.component.anno.ModuleAppAnno
import com.xiaojinzi.component.application.IComponentApplication
import com.xiaojinzi.component.application.IModuleNotifyChanged
import com.xiaojinzi.support.ktx.LogSupport

/**
 * 写点注释123
 */
@ModuleAppAnno
class UserModuleApplication : IComponentApplication, IModuleNotifyChanged {

    override fun onCreate(app: Application) {
        LogSupport.d(
            tag = "UserModuleApplication",
            content = "onCreate called",
        )
    }

    override fun onDestroy() {
        LogSupport.d(
            tag = "UserModuleApplication",
            content = "onDestroy called",
        )
    }

    override fun onModuleChanged(app: Application) {
        LogSupport.d(
            tag = "UserModuleApplication",
            content = "onModuleChanged called",
        )
    }

}