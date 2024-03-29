package com.xiaojinzi.component.user.interceptor

import com.xiaojinzi.component.anno.InterceptorAnno
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.impl.RouterResult
import com.xiaojinzi.component.impl.service.service
import kotlinx.coroutines.flow.first

/**
 * 登录拦截器
 */
@InterceptorAnno(value = RouterConfig.INTERCEPTOR_PERMISSION_LOGIN)
class LoginRouterInterceptor : RouterInterceptor {

    override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {

        val isLogin = UserSpi::class
            .service()
            ?.isLoginObservableDto?.first() == true

        // 如果没有登录
        if (!isLogin) {
            val context = chain.request().rawAliveContext!!
            Router.with(context = context)
                .hostAndPath(hostAndPath = RouterConfig.USER_LOGIN)
                .requestCodeRandom()
                .resultCodeMatchAwait()
        }

        // 放行
        return chain.proceed(request = chain.request())

    }

}