package com.xiaojinzi.component

import android.app.Application
import com.xiaojinzi.component.anno.support.NeedOptimizeAnno
import com.xiaojinzi.component.bean.InterceptorThreadType
import com.xiaojinzi.component.support.AttrAutoWireMode
import com.xiaojinzi.component.support.Utils.checkNullPointer

/**
 * 这是组件化的一个配置类
 *
 * @see Component.init
 */
class Config private constructor(builder: Builder) {

    val application: Application
    val defaultScheme: String
    val isErrorCheck: Boolean
    val isInitRouterAsync: Boolean
    val isOptimizeInit: Boolean
    val isAutoRegisterModule: Boolean
    val isTipWhenUseApplication: Boolean
    val isUseRouteRepeatCheckInterceptor: Boolean
    val routeRepeatCheckDuration: Long
    val notifyModuleChangedDelayTime: Long
    val interceptorDefaultThread: InterceptorThreadType

    @NeedOptimizeAnno("需要重命名为正确的")
    val attrAutoWireMode: AttrAutoWireMode

    init {
        application = builder.application!!
        isErrorCheck = builder.isErrorCheck
        isInitRouterAsync = builder.isInitRouterAsync
        isOptimizeInit = builder.isOptimizeInit
        isAutoRegisterModule = builder.isAutoRegisterModule
        isTipWhenUseApplication = builder.isTipWhenUseApplication
        defaultScheme = builder.defaultScheme!!
        isUseRouteRepeatCheckInterceptor = builder.isUseRouteRepeatCheckInterceptor
        routeRepeatCheckDuration = builder.routeRepeatCheckDuration
        notifyModuleChangedDelayTime = builder.notifyModuleChangedDelayTime
        attrAutoWireMode = builder.attrAutoWireMode
        interceptorDefaultThread = builder.interceptorDefaultThread
    }

    class Builder(application: Application) {

        var application: Application?
        var defaultScheme: String? = "router"

        // 是否进行检查, 默认是打开的, 仅在 debug 的时候有效
        var isErrorCheck = true

        // 默认路由部分的初始化是异步的
        var isInitRouterAsync = true
        var isOptimizeInit = false
        var isAutoRegisterModule = false
        var isTipWhenUseApplication = true
        var isUseRouteRepeatCheckInterceptor = true
        var routeRepeatCheckDuration: Long = 1000
        var notifyModuleChangedDelayTime = 0L
        var attrAutoWireMode = AttrAutoWireMode.Default
        var interceptorDefaultThread: InterceptorThreadType = InterceptorThreadType.IO

        /*标记是否已经使用*/
        private var isUsed = false

        init {
            checkNullPointer(application, "application")
            this.application = application
        }

        fun defaultScheme(defaultScheme: String): Builder {
            this.defaultScheme = defaultScheme
            return this
        }

        fun initRouterAsync(isInitRouterAsync: Boolean): Builder {
            this.isInitRouterAsync = isInitRouterAsync
            return this
        }

        fun errorCheck(isCheck: Boolean): Builder {
            isErrorCheck = isCheck
            return this
        }

        fun optimizeInit(isOptimizeInit: Boolean): Builder {
            this.isOptimizeInit = isOptimizeInit
            return this
        }

        fun autoRegisterModule(isAutoRegisterModule: Boolean): Builder {
            this.isAutoRegisterModule = isAutoRegisterModule
            return this
        }

        /**
         * 设置是否在跳转的时候使用 [Application] 的时候日志提醒
         */
        fun tipWhenUseApplication(isTipWhenUseApplication: Boolean): Builder {
            this.isTipWhenUseApplication = isTipWhenUseApplication
            return this
        }

        /**
         * 设置是否使用内置的路由跳转的重复检查的拦截器
         * 在一定时间内, Router 跳转如果发现 host 和 path 是一样的, 则认定为是一致的.
         * 那么第二次将会被拦截. 时间您可以通过 [.routeRepeatCheckDuration] 设置
         */
        fun useRouteRepeatCheckInterceptor(isUseRouteRepeatCheckInterceptor: Boolean): Builder {
            this.isUseRouteRepeatCheckInterceptor = isUseRouteRepeatCheckInterceptor
            return this
        }

        fun routeRepeatCheckDuration(routeRepeatCheckDuration: Long): Builder {
            this.routeRepeatCheckDuration = routeRepeatCheckDuration
            return this
        }

        fun notifyModuleChangedDelayTime(notifyModuleChangedDelayTime: Long): Builder {
            this.notifyModuleChangedDelayTime = notifyModuleChangedDelayTime
            return this
        }

        fun attrAutoWireMode(attrAutoWireMode: AttrAutoWireMode): Builder {
            this.attrAutoWireMode = attrAutoWireMode
            return this
        }

        fun interceptorDefaultThread(interceptorDefaultThread: InterceptorThreadType): Builder {
            this.interceptorDefaultThread = interceptorDefaultThread
            return this
        }

        fun build(): Config {
            // 参数检查
            checkNullPointer(application, "application")
            checkNullPointer(defaultScheme, "application")
            if (isUsed) {
                throw UnsupportedOperationException("this builder only can build once!")
            }
            if (isAutoRegisterModule) {
                if (!isOptimizeInit) {
                    throw UnsupportedOperationException("you must call optimizeInit(true) method")
                }
            }
            if (attrAutoWireMode === AttrAutoWireMode.Unspecified) {
                throw UnsupportedOperationException("you can't set Unspecified of AttrAutoWireMode")
            }
            isUsed = true
            // 提前创建对象
            val config = Config(this)
            // 解除占用
            application = null
            defaultScheme = null
            return config
        }

    }

    companion object {
        fun with(application: Application): Builder {
            return Builder(application)
        }
    }
}