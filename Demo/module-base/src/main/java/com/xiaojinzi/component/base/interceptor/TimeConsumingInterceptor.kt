package com.xiaojinzi.component.base.interceptor

import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.impl.RouterResult

class TimeConsumingInterceptor: RouterInterceptor {

    override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
        return chain.proceed(request = chain.request())
    }

}