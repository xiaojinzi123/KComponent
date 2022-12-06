package com.xiaojinzi.component.base.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.UiContext
import com.xiaojinzi.component.anno.router.*
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.bean.ActivityResult
import com.xiaojinzi.component.impl.BiCallback
import io.reactivex.Single

@RouterApiAnno
interface RouterApi {

    @RequestCodeAnno
    @NavigateAnno(resultCodeMatch = Activity.RESULT_OK)
    @HostAndPathAnno(RouterConfig.SUPPORT_PERMISSION_REQUEST)
    suspend fun requestPermission(
        @UiContext context: Context,
        @ParameterAnno permission: String,
        @ParameterAnno permissionDesc: String,
    )

    @HostAndPathAnno(RouterConfig.SYSTEM_APP_DETAIL)
    fun toAppDetail(
        @UiContext context: Context,
    )

    @HostAndPathAnno(RouterConfig.SUPPORT_WEB_TEST)
    fun toWebTestView1(
        @UiContext context: Context,
    )

    @RequestCodeAnno
    @NavigateAnno(forResult = true)
    @HostAndPathAnno(RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
    fun toTestActivityResultView1(
        @UiContext context: Context,
        callback: BiCallback<ActivityResult>,
    )

    @RequestCodeAnno
    @NavigateAnno(forResult = true)
    @HostAndPathAnno(RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
    fun toTestActivityResultView11(
        @UiContext context: Context,
        callback: (ActivityResult) -> Unit,
    )

    @RequestCodeAnno
    @NavigateAnno(forResult = true)
    @HostAndPathAnno(RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
    suspend fun toTestActivityResultView111(
        @UiContext context: Context,
    ): ActivityResult

    @RequestCodeAnno
    @NavigateAnno(forResult = true)
    @HostAndPathAnno(RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
    fun toTestActivityResultView1111(
        @UiContext context: Context,
    ): Single<ActivityResult>

    @RequestCodeAnno
    @NavigateAnno(forIntent = true)
    @HostAndPathAnno(RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
    fun toTestActivityResultView2(
        @UiContext context: Context,
    ): Single<Intent>

    @HostAndPathAnno(RouterConfig.SUPPORT_TEST_ACTIVITY_RESULT)
    suspend fun toTestActivityResultView3(
        @UiContext context: Context,
    )

}