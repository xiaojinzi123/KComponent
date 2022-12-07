package com.xiaojinzi.component.demo

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.interceptor.CameraPermissionInterceptor
import com.xiaojinzi.component.impl.RouterRequest
import com.xiaojinzi.component.support.ParameterSupport

/**
 * 系统 App 详情
 *
 * @param request
 * @return
 */
@RouterAnno(
    hostAndPath = RouterConfig.SYSTEM_APP_DETAIL,
)
fun appDetail(request: RouterRequest): Intent {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.parse("package:" + request.rawContext!!.packageName)
    return intent
}

@RouterAnno(
    hostAndPath = RouterConfig.SYSTEM_CALL_PHONE,
    interceptorNames = [RouterConfig.INTERCEPTOR_PERMISSION_CALL_PHONE],
)
fun callPhone(request: RouterRequest): Intent {
    val tel = ParameterSupport.getString(request.bundle, "tel")
        ?: throw NullPointerException("the tel is empty")
    return Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
}

@RouterAnno(
    hostAndPath = RouterConfig.SYSTEM_TAKE_PHOTO,
    interceptors = [CameraPermissionInterceptor::class],
)
fun takePhoto(request: RouterRequest): Intent {
    val intent = Intent()
    // 指定开启系统相机的Action
    intent.action = MediaStore.ACTION_IMAGE_CAPTURE
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    return intent
}