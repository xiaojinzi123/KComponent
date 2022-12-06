package com.xiaojinzi.component

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.annotation.UiThread
import com.xiaojinzi.component.anno.support.NotAppUseAnno
import com.xiaojinzi.component.impl.RouterCenter
import com.xiaojinzi.component.impl.application.ModuleManager
import com.xiaojinzi.component.impl.fragment.FragmentCenter
import com.xiaojinzi.component.impl.interceptor.InterceptorCenter
import com.xiaojinzi.component.support.Inject
import com.xiaojinzi.component.support.LogUtil
import com.xiaojinzi.component.support.Utils

/**
 * 组件化类,需要被初始化
 * 组件化的配置类,可以拿到 Application
 * time   : 2018/08/09
 *
 * @author : xiaojinzi
 */
object Component {

    const val GITHUB_URL = "https://github.com/xiaojinzi123/KComponent"
    const val DOC_URL = "https://github.com/xiaojinzi123/KComponent/wiki"

    /**
     * 是否初始化过了
     */
    private var isInit = false

    /**
     * 是否是 debug 状态
     */
    var isDebug = false
        private set

    private lateinit var application: Application

    /**
     * 配置对象
     */
    private var mConfig: Config? = null

    /**
     * 初始化
     *
     * @see Config 初始化的配置对象
     */
    @UiThread
    fun init(
        application: Application,
        isDebug: Boolean,
        config: Config = Config(),
    ) {
        // 做必要的检查
        if (isInit) {
            throw RuntimeException("you have init Component already!")
        }
        Utils.checkMainThread()
        Component.application = application
        Component.isDebug = isDebug
        mConfig = config
        if (isDebug) {
            printComponent()
        }
        // 注册
        application.registerActivityLifecycleCallbacks(ComponentLifecycleCallback())
        if (mConfig!!.isOptimizeInit && mConfig!!.isAutoRegisterModule) {
            ModuleManager.autoRegister()
        }
        isInit = true
    }

    fun requiredConfig(): Config {
        checkInit()
        return mConfig!!
    }

    fun getApplication(): Application {
        return application
    }

    private fun checkInit() {
        if (mConfig == null) {
            throw RuntimeException("you must init Component first!")
        }
    }

    @UiThread
    fun inject(target: Any) {
        inject(target = target, bundle = null, isAutoWireAttrValue = true, isAutoWireService = true)
    }

    @UiThread
    fun injectAttrValueFromIntent(target: Any, intent: Intent?) {
        injectAttrValueFromBundle(target = target, bundle = intent?.extras)
    }

    @UiThread
    fun injectAttrValueFromBundle(target: Any, bundle: Bundle?) {
        inject(target, bundle, isAutoWireAttrValue = true)
    }

    @UiThread
    fun injectService(target: Any) {
        inject(target = target, bundle = null, isAutoWireService = true)
    }

    /**
     * 注入功能
     *
     * @param target              目标, 可能是任意的类
     * @param bundle              属性注入的 Bundle 数据提供者
     * @param isAutoWireAttrValue 是否注入属性值
     * @param isAutoWireService   是否注入 Service
     */
    @UiThread
    @Suppress("UNCHECKED_CAST")
    private fun inject(
        target: Any,
        bundle: Bundle?,
        isAutoWireAttrValue: Boolean = false,
        isAutoWireService: Boolean = false,
    ) {
        Utils.checkMainThread()
        val injectClassName = target.javaClass.name + ComponentConstants.INJECT_SUFFIX
        try {
            val targetInjectClass = Class.forName(injectClassName)
            val inject = targetInjectClass.newInstance() as Inject<Any>
            if (isAutoWireService) {
                inject.injectService(target)
            }
            if (isAutoWireAttrValue) {
                if (bundle == null) {
                    inject.injectAttrValue(target)
                } else {
                    Utils.checkNullPointer(bundle, "bundle")
                    inject.injectAttrValue(target, bundle)
                }
            }
        } catch (ignore: Exception) {
            LogUtil.log("class '" + target.javaClass.name + "' inject fail. ${ignore.message}")
        }
    }

    /**
     * 使用者应该在开发阶段调用这个函数来检查以下的问题：
     * 1.路由表在不同的子路由表中是否有重复
     * 2.服务在不同模块中的声明是否也有重复的名称
     */
    @NotAppUseAnno
    fun check() {
        if (isDebug && requiredConfig().isErrorCheck) {
            RouterCenter.check()
            InterceptorCenter.check()
            FragmentCenter.check()
        }
    }

    /**
     * 打印宣传内容和 logo
     */
    private fun printComponent() {
        val sb = StringBuffer()
        sb.append(" \n")

        // 打印logo C
        sb.append("\n")
        sb.append("             *********\n")
        sb.append("          ****        ****\n")
        sb.append("       ****              ****\n")
        sb.append("     ****\n")
        sb.append("    ****\n")
        sb.append("    ****\n")
        sb.append("    ****\n")
        sb.append("     ****\n")
        sb.append("       ****              ****\n")
        sb.append("          ****        ****\n")
        sb.append("             *********\n")
        sb.append("感谢您选择 KComponent 组件化框架. \n有任何问题欢迎提 issue 或者扫描 github 上的二维码进入群聊@群主\n")
        sb.append("Github 地址：$GITHUB_URL \n")
        sb.append("文档地址：$DOC_URL \n")
        LogUtil.logw(sb.toString())
    }

}