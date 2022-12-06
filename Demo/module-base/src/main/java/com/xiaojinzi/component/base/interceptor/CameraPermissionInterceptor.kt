package com.xiaojinzi.component.base.interceptor

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.xiaojinzi.component.anno.InterceptorAnno
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.api.RouterApi
import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.impl.RouterResult
import com.xiaojinzi.component.impl.routeApi

@InterceptorAnno(RouterConfig.INTERCEPTOR_PERMISSION_CAMERA)
class CameraPermissionInterceptor : RouterInterceptor {

    override suspend fun intercept(chain: RouterInterceptor.Chain): RouterResult {
        val context = chain.request().rawAliveContext!!

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            RouterApi::class
                .routeApi()
                .requestPermission(
                    context = context,
                    permission = Manifest.permission.CAMERA,
                    permissionDesc = "申请相机权限",
                )
        }
        return chain.proceed(request = chain.request())
    }

}