package com.xiaojinzi.component.base.interceptor

import android.app.ProgressDialog
import com.xiaojinzi.component.anno.MainThread
import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.impl.RouterResult
import kotlinx.coroutines.delay

@MainThread
class DialogShowInterceptor : RouterInterceptor {
    override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {

        chain.request().rawOrTopAliveActivity?.let { act ->
            // 显示一个 loading 弹框
            val dialog = ProgressDialog.show(act, "温馨提示", "耗时操作进行中,2秒后结束", true, false)
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }

        return chain.proceed(request = chain.request())
    }

}