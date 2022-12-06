package com.xiaojinzi.component.base.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import com.xiaojinzi.component.anno.router.*
import com.xiaojinzi.component.base.InterceptorConfig
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.base.interceptor.DialogShowInterceptor
import com.xiaojinzi.component.base.interceptor.TimeConsumingInterceptor
import com.xiaojinzi.component.bean.ActivityResult
import com.xiaojinzi.component.impl.BiCallback
import com.xiaojinzi.component.impl.Call
import com.xiaojinzi.component.impl.Callback
import com.xiaojinzi.component.impl.Navigator
import com.xiaojinzi.component.lib.resource.SubParcelable
import com.xiaojinzi.component.lib.resource.User
import com.xiaojinzi.component.lib.resource.UserWithParcelable
import com.xiaojinzi.component.lib.resource.UserWithSerializable
import com.xiaojinzi.component.support.Action
import com.xiaojinzi.component.support.Consumer
import com.xiaojinzi.component.support.NavigationDisposable
import io.reactivex.Completable
import io.reactivex.Single

/**
 * App 模块的路由跳转接口
 */
@RouterApiAnno
@SchemeAnno("SampleApiScheme")
@HostAnno(RouterConfig.HOST_APP)
@CategoryAnno(Intent.CATEGORY_APP_BROWSER)
@FlagAnno(Intent.FLAG_ACTIVITY_CLEAR_TASK)
interface SampleApi {

    @PathAnno(RouterConfig.APP_TEST_NO_TARGET) // 使用一个拦截器
    @CategoryAnno(Intent.CATEGORY_APP_BROWSER, Intent.CATEGORY_APP_CONTACTS)
    @FlagAnno(Intent.FLAG_ACTIVITY_CLEAR_TASK, Intent.FLAG_ACTIVITY_CLEAR_TOP)
    @UseInterceptorAnno(
        names = [InterceptorConfig.USER_LOGIN, InterceptorConfig.CALL_PHONE_PERMISSION],
        classes = [DialogShowInterceptor::class, TimeConsumingInterceptor::class],
    )
    @SchemeAnno("testScheme")
    @UserInfoAnno("xiaojinzi")
    fun test(
        context: Context?,
        @RequestCodeAnno requestCode: Int,
        @ParameterAnno("data") data: String?,
        callback: Callback?,
        @OptionsAnno options: Bundle,
        @BeforeRouteSuccessActionAnno beforeAction: Action,
        @BeforeStartActivityActionAnno beforeStartAction: Action,
        @AfterStartActivityActionAnno afterStartAction: Action,
        @AfterRouteActionAnno afterAction: Action,
        @AfterRouteErrorActionAnno afterErrorAction: Action,
        @AfterRouteEventActionAnno afterEventAction: Action,
        intentConsumer: Consumer<Intent>
    ): Navigator

    @NavigateAnno
    @UrlAnno("www.baidu.com")
    @HostAnno(RouterConfig.HOST_APP)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    @RequestCodeAnno()
    fun test1(
        @ParameterAnno("data") data: String?,
        callback: Callback?,
    )

    @NavigateAnno(forResult = true)
    @HostAndPathAnno(RouterConfig.HOST_APP + "/" + RouterConfig.APP_TEST_NO_TARGET)
    fun test2(
        context: Context?,
        @ParameterAnno("data") data: String?, callback: BiCallback<ActivityResult>
    )

    @NavigateAnno
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test3(
        context: Context?,
        @ParameterAnno("data") data: String?,
        callback: Callback?
    ): NavigationDisposable

    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test4(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Call

    @NavigateAnno(forResult = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test5(
        context: Context?,
        @ParameterAnno("data") data: String?,
        callback: BiCallback<ActivityResult>,
    ): NavigationDisposable

    @RequestCodeAnno
    @NavigateAnno(forResult = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test5Rx(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Single<ActivityResult>

    @RequestCodeAnno
    @NavigateAnno(forResult = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    suspend fun test5Suspend(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): ActivityResult

    @NavigateAnno(forIntent = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test6(
        context: Context?,
        @ParameterAnno("data") data: String?,
        callback: BiCallback<Intent>,
    ): NavigationDisposable

    @NavigateAnno(forIntent = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test6Rx(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Single<Intent>

    @NavigateAnno(forIntent = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    suspend fun test6_Suspend(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Intent

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_CANCELED)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    suspend fun test66_Suspend(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Intent

    @NavigateAnno(forResultCode = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test7(
        context: Context?,
        @ParameterAnno("data") data: String?,
        callback: BiCallback<Int>,
    ): NavigationDisposable

    @NavigateAnno(forResultCode = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test7Rx(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Single<Int>

    @NavigateAnno(forResultCode = true)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    suspend fun test7Suspend(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Int

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test8_void(
        context: Context?,
        @ParameterAnno("data") data: String?,
        callback: BiCallback<Intent>
    )

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test8(
        context: Context?,
        @ParameterAnno("data") data: String?,
        callback: (Intent) -> Unit,
    ): NavigationDisposable

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test8Rx(
        context: Context?,
        @ParameterAnno("data") data: String?
    ): Single<Intent>

    @NavigateAnno(resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test9(context: Context, callback: Callback)

    @NavigateAnno(resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test9(context: Context, callback: () -> Unit)

    @NavigateAnno(resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    suspend fun test9_suspend(context: Context)

    @NavigateAnno(resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test9_rx(context: Context?): Completable

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test10(context: Context?, callback: (Intent) -> Unit)

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test10_disposable(context: Context?, callback: BiCallback<Intent>): NavigationDisposable

    @NavigateAnno(forIntent = true, resultCodeMatch = Activity.RESULT_OK)
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test10_rx(context: Context?): Single<Intent>

    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test11(context: Context?): Completable?

    /**
     * 测试基本类型的支持
     */
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test111(
        context: Context?,
        @ParameterAnno("data1") data1: String?,
        @ParameterAnno("data2") data2: Byte,
        @ParameterAnno("data3") data3: Short,
        @ParameterAnno("data4") data4: Int,
        @ParameterAnno("data5") data5: Long,
        @ParameterAnno("data6") data6: Float,
        @ParameterAnno("data7") data7: Double,
        @ParameterAnno("data8") data8: User?,
        @ParameterAnno("data9") data9: UserWithParcelable?,
        @ParameterAnno("data10") data10: UserWithSerializable?,
        @ParameterAnno("data11") data11: CharSequence?,
        @ParameterAnno("data12") data12: Bundle?,
        @ParameterBundleAnno data13: Bundle?,
        callback: Callback?,
    )

    /**
     * 测试数组
     */
    @NavigateAnno
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test112(
        context: Context?,
        @ParameterAnno("data1") data1: ByteArray?,
        @ParameterAnno("data2") data2: CharArray?,
        @ParameterAnno("data3") data3: Array<String>?,
        @ParameterAnno("data4") data4: ShortArray?,
        @ParameterAnno("data5") data5: IntArray?,
        @ParameterAnno("data6") data6: LongArray?,
        @ParameterAnno("data7") data7: FloatArray?,
        @ParameterAnno("data8") data8: DoubleArray?,
        @ParameterAnno("data9") data9: BooleanArray?,
        @ParameterAnno("data10") data10: Array<Parcelable>?,
        @ParameterAnno("data11") data101: Array<UserWithParcelable>?,
        @ParameterAnno("data12") data11: Array<CharSequence>?,
        callback: Callback?
    )

    @NavigateAnno
    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test113(
        context: Context?,
        @ParameterAnno("data1") stringArrayList: ArrayList<String>?,
        @ParameterAnno("data2") integerArrayList: ArrayList<Int>?,
        @ParameterAnno("data3") parcelableArrayList: ArrayList<UserWithParcelable>?,
        @ParameterAnno("data4") charSequenceArrayList: ArrayList<CharSequence>?,
        @ParameterAnno("data5") parcelableSparseArray: SparseArray<Parcelable>?,
        @ParameterAnno("data6") userParcelableSparseArray: SparseArray<UserWithParcelable>?,
        callback: Callback?
    ): NavigationDisposable

    @PathAnno(RouterConfig.APP_TEST_NO_TARGET)
    fun test114(
        activity: Activity?,
        @ParameterAnno("data1") data1: ByteArray?,
        @ParameterAnno("data2") data2: CharArray?,
        @ParameterAnno("data3") data3: Array<String>?,
        @ParameterAnno("data4") data4: ShortArray?,
        @ParameterAnno("data5") data5: IntArray?,
        @ParameterAnno("data6") data6: LongArray?,
        @ParameterAnno("data7") data7: FloatArray?,
        @ParameterAnno("data8") data8: DoubleArray?,
        @ParameterAnno("data9") data9: BooleanArray?,
        @ParameterAnno("data10") data10: Array<Parcelable>?,
        @ParameterAnno("data101") data101: Array<UserWithParcelable>?,
        @ParameterAnno("data11") data11: Array<CharSequence>?,
        @ParameterAnno("data40") data40: String?,
        @ParameterAnno("data41") data41: CharSequence?,
        @ParameterAnno("data42") data42: Byte,
        @ParameterAnno("data43") data43: Char,
        @ParameterAnno("data44") data44: Boolean,
        @ParameterAnno("data45") data45: Short,
        @ParameterAnno("data46") data46: Int,
        @ParameterAnno("data47") data47: Long,
        @ParameterAnno("data48") data48: Float,
        @ParameterAnno("data49") data49: Double,
        @ParameterAnno("data30") data30: ArrayList<CharSequence>?,
        @ParameterAnno("data31") data31: ArrayList<String>?,
        @ParameterAnno("data32") data32: ArrayList<Int>?,
        @ParameterAnno("data33") data33: ArrayList<Parcelable>?,
        @ParameterAnno("data34") data34: ArrayList<UserWithParcelable>?,
        @ParameterAnno("data341") data341: ArrayList<UserWithSerializable>?,
        @ParameterAnno("data35") data35: ArrayList<SubParcelable>?,
        @ParameterAnno("data36") data36: SparseArray<Parcelable>?,
        @ParameterAnno("data37") data37: SparseArray<UserWithParcelable>?,
        @ParameterAnno("data38") data38: SparseArray<SubParcelable>?,
        @ParameterAnno("data12") data12: User?,
        @ParameterAnno("data13") data13: UserWithSerializable?,
        @ParameterAnno("data14") data14: UserWithParcelable?
    )
}