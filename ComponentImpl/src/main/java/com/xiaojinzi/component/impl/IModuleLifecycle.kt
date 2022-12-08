package com.xiaojinzi.component.impl

import android.app.Application
import androidx.annotation.UiThread
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.application.IApplicationLifecycle
import com.xiaojinzi.component.application.IModuleNotifyChanged
import com.xiaojinzi.component.bean.RouterBean
import com.xiaojinzi.component.bean.RouterDegradeBean
import com.xiaojinzi.component.impl.interceptor.InterceptorBean
import com.xiaojinzi.component.support.IBaseLifecycle
import kotlin.reflect.KClass

@UiThread
@CheckClassNameAnno
interface IModuleFragmentLifecycle {

    /**
     * 注册 Fragment
     */
    fun initFragment()

    /**
     * 反注册 Fragment
     */
    fun destroyFragment()

}

/**
 * @see IApplicationLifecycle
 */
@UiThread
@CheckClassNameAnno
interface IModuleLifecycle : IModuleFragmentLifecycle, IBaseLifecycle, IModuleNotifyChanged {

    /**
     * 此模块的名字
     */
    val moduleName: String

    /**
     * 此模块的优先级
     */
    val priority: Int

    /**
     * 模块配置的 Application 类
     */
    fun initApplication(): List<IApplicationLifecycle>

    /**
     * 注册服务发现
     */
    fun initSpi(application: Application)

    /**
     * 反注册服务发现
     */
    fun destroySpi()

    /**
     * 返回化全局的拦截器
     */
    fun initGlobalInterceptor(): List<InterceptorBean>

    /**
     * 返回化拦截器
     */
    fun initInterceptor(): Map<String, KClass<out RouterInterceptor>>

    /**
     * 返回这个模块的正则匹配的路由信息
     */
    fun initRegExRouterMap(): Map<String, RouterBean>

    /**
     * 返回这个模块的路由信息
     */
    fun initRouterList(): List<RouterBean>

    /**
     * 返回降级的表
     */
    fun initRouterDegrade(): List<RouterDegradeBean>

}