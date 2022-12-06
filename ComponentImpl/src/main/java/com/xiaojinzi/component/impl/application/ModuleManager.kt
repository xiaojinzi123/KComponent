package com.xiaojinzi.component.impl.application

import android.app.Application
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.xiaojinzi.component.Component
import com.xiaojinzi.component.Component.check
import com.xiaojinzi.component.Component.getApplication
import com.xiaojinzi.component.Component.requiredConfig
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.application.IModuleNotifyChanged
import com.xiaojinzi.component.cache.ClassCache
import com.xiaojinzi.component.impl.IModuleLifecycle
import com.xiaojinzi.component.impl.RouterCenter
import com.xiaojinzi.component.impl.RouterDegradeCenter
import com.xiaojinzi.component.impl.fragment.FragmentCenter
import com.xiaojinzi.component.impl.interceptor.InterceptorCenter
import com.xiaojinzi.component.impl.service.ServiceManager
import com.xiaojinzi.component.support.ASMUtil
import com.xiaojinzi.component.support.LogUtil
import com.xiaojinzi.component.support.Utils
import java.util.*

/**
 * 这个类必须放在 [ComponentUtil.IMPL_OUTPUT_PKG] 包下面
 * 这是是管理每一个模块之前联系的管理类,加载模块的功能也是这个类负责的
 *
 * @author xiaojinzi
 */
object ModuleManager {

    private val moduleApplicationMap: MutableMap<String, IModuleLifecycle> = HashMap()

    fun findModuleApplication(moduleName: String): IModuleLifecycle? {
        var result: IModuleLifecycle? = null
        if (requiredConfig().isOptimizeInit) {
            LogUtil.log("\"$moduleName\" will try to load by bytecode")
            result = ASMUtil.findModuleApplicationAsmImpl(
                ComponentUtil.transformHostForClass(moduleName)
            )
        } else {
            LogUtil.log("\"$moduleName\" will try to load by reflection")
            try {
                // 先找正常的
                val clazz =
                    Class.forName(ComponentUtil.genHostModuleApplicationClassName(moduleName))
                result = clazz.newInstance() as IModuleLifecycle
            } catch (ignore: Exception) {
                // ignore
            }
            if (result == null) {
                try {
                    // 找默认的
                    val clazz = Class.forName(
                        ComponentUtil.genDefaultHostModuleApplicationClassName(moduleName)
                    )
                    result = clazz.newInstance() as IModuleLifecycle
                } catch (ignore: Exception) {
                    // ignore
                }
            }
        }
        return result
    }

    @UiThread
    fun register(module: IModuleLifecycle) {
        if (moduleApplicationMap.containsKey(key = module.moduleName)) {
            LogUtil.loge("The module \"" + module.moduleName + "\" is already registered")
        } else {
            // 标记已经注册
            moduleApplicationMap[module.moduleName] = module
            // 模块的 Application 的 onCreate 执行
            module.onCreate(app = getApplication())
            // 路由的部分的注册, 可选的异步还是同步
            val r = Runnable {
                RouterCenter.register(
                    moduleName = module.moduleName,
                    routerMap = module.initRouterList(),
                )
                InterceptorCenter.register(
                    moduleName = module.moduleName,
                    globalInterceptorList = module.initGlobalInterceptor(),
                    interceptorMap = module.initInterceptor(),
                )
                RouterDegradeCenter.register(
                    moduleName = module.moduleName,
                    list = module.initRouterDegrade(),
                )
                FragmentCenter.register(
                    moduleName = module.moduleName,
                    fragmentModule = module,
                )
                notifyModuleChanged()
            }
            // 路由是否异步初始化
            if (requiredConfig().isInitRouterAsync) {
                Utils.postActionToWorkThread(r)
            } else {
                r.run()
            }
        }
    }

    fun register(moduleName: String) {
        if (moduleApplicationMap.containsKey(moduleName)) {
            LogUtil.loge("the host '$moduleName' is already load")
            return
        } else {
            val module = findModuleApplication(moduleName)
            if (module == null) {
                LogUtil.log("模块 '$moduleName' 加载失败")
            } else {
                register(module = module)
            }
        }
    }

    /**
     * 自动注册, 需要开启 [com.xiaojinzi.component.Config.Builder.optimizeInit]
     * 表示使用 Gradle 插件优化初始化
     */
    fun autoRegister() {
        if (!requiredConfig().isOptimizeInit) {
            LogUtil.logw("you can't use this method to register module. Because you not turn on 'optimizeInit' by calling method 'Config.Builder.optimizeInit(true)' when you init")
        }
        val moduleNames = ASMUtil.getModuleNames()
        if (moduleNames.isNotEmpty()) {
            registerArr(*moduleNames.toTypedArray())
        }
    }

    /**
     * 注册业务模块, 可以传多个名称
     *
     * @param hosts host 的名称数组
     */
    fun registerArr(vararg hosts: String) {
        val appList: MutableList<IModuleLifecycle> = ArrayList(hosts.size)
        for (host in hosts) {
            val moduleApplication = findModuleApplication(moduleName = host)
            if (moduleApplication == null) {
                LogUtil.log("模块 '$host' 加载失败")
            } else {
                appList.add(element = moduleApplication)
            }
        }
        // 处理优先级, 数值大的先加载
        appList.sortWith { o1, o2 -> o2.priority - o1.priority }
        for (moduleApplication in appList) {
            register(module = moduleApplication)
        }
    }

    @UiThread
    private fun unregister(module: IModuleLifecycle) {
        moduleApplicationMap.remove(module.moduleName)
        module.onDestroy()
        Utils.postActionToWorkThread {
            RouterCenter.unregister(moduleName = module.moduleName)
            InterceptorCenter.unregister(moduleName = module.moduleName)
            RouterDegradeCenter.unregister(moduleName = module.moduleName)
            FragmentCenter.unregister(
                moduleName = module.moduleName,
                fragmentModule = module,
            )
            // 清空缓存
            ClassCache.clear()
            notifyModuleChanged()
        }
    }

    fun unregister(moduleName: String) {
        val module = moduleApplicationMap[moduleName]
        if (module == null) {
            LogUtil.log("模块 '$moduleName' 卸载失败")
        } else {
            unregister(module = module)
        }
    }

    fun unregisterAll() {
        // 创建一个 HashSet 是为了能循环的时候删除集合中的元素
        for (host in HashSet(moduleApplicationMap.keys)) {
            unregister(host)
        }
    }

    @AnyThread
    private fun notifyModuleChanged() {
        // 当前的值
        val compareValue = Utils.COUNTER.incrementAndGet()
        Utils.postDelayActionToMainThread({
            // 说明没有改变过
            if (compareValue == Utils.COUNTER.get()) {
                // LogUtil.loge("通知 " + compareValue);
                doNotifyModuleChanged()
            } else {
                // LogUtil.loge("放弃通知 " + compareValue);
            }
        }, requiredConfig().notifyModuleChangedDelayTime)
    }

    @UiThread
    private fun doNotifyModuleChanged() {
        val application: Application = getApplication()
        moduleApplicationMap
            .values
            .filterIsInstance<IModuleNotifyChanged>()
            .forEach {
                it.onModuleChanged(app = application)
            }
        // 内部有 debug 判断
        check()
        // 触发自动初始化
        Utils.postActionToWorkThread { ServiceManager.autoInitService() }
    }

}