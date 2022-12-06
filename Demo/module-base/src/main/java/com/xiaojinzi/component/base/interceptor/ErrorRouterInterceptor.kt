package com.xiaojinzi.component.base.interceptor

import com.xiaojinzi.component.error.ignore.NavigationException
import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.impl.RouterResult

class ErrorRouterInterceptor : RouterInterceptor {

    override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
        throw NavigationException(
            message = "拦截器测试错误"
        )
    }

}