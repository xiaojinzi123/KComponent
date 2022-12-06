package com.xiaojinzi.component.demo

import com.xiaojinzi.component.anno.GlobalInterceptorAnno
import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.impl.RouterResult

@GlobalInterceptorAnno
class AppRouterInterceptor: RouterInterceptor {

    override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
        return chain.proceed(
            request = chain.request(),
        )
    }

}