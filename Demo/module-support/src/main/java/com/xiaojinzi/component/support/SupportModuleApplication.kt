package com.xiaojinzi.component.support

import android.app.Application
import com.xiaojinzi.component.anno.ModuleAppAnno
import com.xiaojinzi.component.application.IComponentApplication
import com.xiaojinzi.component.application.IModuleNotifyChanged
import com.xiaojinzi.support.ktx.LogSupport

/**
 * 写点注释12345
 */
@ModuleAppAnno
class SupportModuleApplication : IComponentApplication, IModuleNotifyChanged {

    override fun onCreate(app: Application) {
        LogSupport.d(
            tag = "SupportModuleApplication",
            content = "onCreate called",
        )
    }

    override fun onDestroy() {
        LogSupport.d(
            tag = "SupportModuleApplication",
            content = "onDestroy called",
        )
    }

    override fun onModuleChanged(app: Application) {
        LogSupport.d(
            tag = "SupportModuleApplication",
            content = "onModuleChanged called",
        )
    }

}