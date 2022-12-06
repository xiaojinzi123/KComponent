package com.xiaojinzi.component

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.xiaojinzi.component.ComponentActivityStack.pushActivity
import com.xiaojinzi.component.ComponentActivityStack.removeActivity

/**
 * 注册的声明周期回调,用于取消一些调用,这些调用在界面销毁之后
 */
internal class ComponentLifecycleCallback : ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        pushActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        // ignore
    }

    override fun onActivityResumed(activity: Activity) {
        // ignore
    }

    override fun onActivityPaused(activity: Activity) {
        // ignore
    }

    override fun onActivityStopped(activity: Activity) {
        // ignore
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // ignore
    }

    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity = activity)
    }

}