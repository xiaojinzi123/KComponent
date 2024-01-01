package com.xiaojinzi.component.app1

import android.app.Application
import com.xiaojinzi.component.anno.ModuleAppAnno
import com.xiaojinzi.component.application.IComponentApplication

/**
 * Hello
 */
@ModuleAppAnno
class AppModuleApplication : IComponentApplication {

    override fun onCreate(app: Application) {
    }

    override fun onDestroy() {
    }

}