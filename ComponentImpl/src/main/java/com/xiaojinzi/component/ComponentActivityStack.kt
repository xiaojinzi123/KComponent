package com.xiaojinzi.component

import android.app.Activity
import com.xiaojinzi.component.support.Utils
import java.util.*

/**
 * Component 的 Activity 栈
 *
 * @author xiaojinzi
 */
object ComponentActivityStack {
    /**
     * the stack will be save all reference of Activity
     */
    private val activityStack: Stack<Activity> = Stack()

    /**
     * 是否是空的
     */
    val isEmpty: Boolean
        @Synchronized
        get() = activityStack.isEmpty()

    /**
     * 返回顶层的 Activity
     */
    val topActivity: Activity?
        @Synchronized
        get() = if (isEmpty) null else activityStack[activityStack.lastIndex]

    /**
     * 返回顶层第二个的 Activity
     */
    val secondTopActivity: Activity?
        @Synchronized
        get() = if (isEmpty || activityStack.size < 2) null else activityStack[activityStack.lastIndex - 1]

    /**
     * 返回底层的 Activity
     */
    val bottomActivity: Activity?
        @Synchronized
        get() = if (isEmpty || activityStack.size < 1) null else activityStack[0]

    /**
     * 返回顶层的活着的 Activity
     */
    val topAliveActivity: Activity?
        @Synchronized
        get() {
            var result: Activity? = null
            if (!isEmpty) {
                val size = activityStack.size
                for (i in size - 1 downTo 0) {
                    val activity = activityStack[i]
                    // 如果已经销毁, 就下一个
                    if (!Utils.isActivityDestroyed(activity)) {
                        result = activity
                        break
                    }
                }
            }
            return result
        }

    /**
     * 进入栈
     */
    @Synchronized
    fun pushActivity(activity: Activity) {
        if (activityStack.contains(activity)) {
            return
        }
        activityStack.add(activity)
    }

    /**
     * remove the reference of Activity
     *
     * @author xiaojinzi
     */
    @Synchronized
    fun removeActivity(activity: Activity) {
        activityStack.remove(activity)
    }

    /**
     * 返回顶层的 Activity除了某一个
     */
    @Synchronized
    fun getTopActivityExcept(clazz: Class<out Activity?>): Activity? {
        val size = activityStack.size
        for (i in size - 1 downTo 0) {
            val itemActivity = activityStack[i]
            if (itemActivity.javaClass != clazz) {
                return itemActivity
            }
        }
        return null
    }


    /**
     * 是否存在某一个 Activity
     */
    @Synchronized
    fun isExistActivity(clazz: Class<out Activity>): Boolean {
        for (activity in activityStack) {
            if (activity.javaClass == clazz) {
                return true
            }
        }
        return false
    }

    @Synchronized
    fun isExistOtherActivityExcept(clazz: Class<out Activity>): Boolean {
        for (activity in activityStack) {
            if (activity.javaClass != clazz) {
                return true
            }
        }
        return false
    }

}