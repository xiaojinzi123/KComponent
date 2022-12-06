package com.xiaojinzi.component.support

import android.util.Log
import com.xiaojinzi.component.Component.isDebug
import androidx.annotation.AnyThread
import com.xiaojinzi.component.support.LogUtil

/**
 * 用于打印日志
 * time   : 2019/01/25
 *
 * @author : xiaojinzi
 */
object LogUtil {

    private const val TAG = "-------- Component --------"

    @AnyThread
    fun loge(message: String) {
        loge(TAG, message)
    }

    @AnyThread
    fun loge(tag: String, message: String) {
        if (isDebug) {
            Log.e(tag, message)
        }
    }

    @AnyThread
    fun logw(message: String) {
        logw(TAG, message)
    }

    @AnyThread
    fun logw(tag: String, message: String) {
        if (isDebug) {
            Log.w(tag, message)
        }
    }

    @AnyThread
    fun log(message: String) {
        log(TAG, message)
    }

    @AnyThread
    fun log(tag: String, message: String) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }

}