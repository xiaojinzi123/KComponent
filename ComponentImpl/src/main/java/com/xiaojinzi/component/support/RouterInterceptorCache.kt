package com.xiaojinzi.component.support

import android.app.Application
import android.content.Context
import androidx.annotation.UiThread
import com.xiaojinzi.component.Component.getApplication
import com.xiaojinzi.component.Component.isDebug
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.anno.support.NeedOptimizeAnno
import com.xiaojinzi.component.cache.ClassCache
import com.xiaojinzi.component.error.CreateInterceptorException
import com.xiaojinzi.component.impl.RouterInterceptor
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

/**
 * 支持缓存自定义拦截器,工具类
 * 目前就只有给 目标页面在 [RouterAnno.interceptors]
 * or [RouterAnno.interceptorNames]
 * or [com.xiaojinzi.component.impl.Navigator.interceptors]
 * or [com.xiaojinzi.component.impl.Navigator.interceptorNames]
 * 写的拦截器做缓存
 *
 *
 * time   : 2018/12/03
 *
 * @author : xiaojinzi
 */
@CheckClassNameAnno
object RouterInterceptorCache {

    /**
     * 内部做了缓存,如果缓存中没有就会反射创建拦截器对象
     */
    @UiThread
    @Synchronized
    fun getInterceptorByClass(tClass: KClass<out RouterInterceptor>): RouterInterceptor? {
        var t = ClassCache.get<RouterInterceptor>(tClass)
        if (t != null) {
            return t
        }
        try {
            // 创建拦截器
            t = create(tClass = tClass)
            if (t == null) {
                throw InstantiationException("do you write default constructor or a constructor with parameter 'Application' or  a constructor with parameter 'Context' ")
            } else {
                ClassCache.put(tClass, t)
            }
        } catch (e: Exception) {
            if (isDebug) {
                throw CreateInterceptorException(e)
            }
        }
        return t
    }

    @Throws(
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    @NeedOptimizeAnno("这里的 Application 参数需要测试一下")
    private fun create(tClass: KClass<out RouterInterceptor>): RouterInterceptor? {
        val constructors = tClass.constructors
        // 这里为什么使用 for 循环而不是直接获取空参数的构造函数或者以下有某个参数的构造函数
        // 是因为你获取的时候会有异常抛出,三种情况你得 try{}catch{}三次
        for (constructor in constructors) {
            val parameterTypes = constructor.typeParameters
            if (parameterTypes.isEmpty()) {
                return constructor.call()
            }
            if (parameterTypes.size == 1 && parameterTypes[0] == Application::class) {
                return constructor.call(getApplication())
            }
            if (parameterTypes.size == 1 && parameterTypes[0] == Context::class.java) {
                return constructor.call(getApplication())
            }
        }
        return null
    }

    @Synchronized
    fun removeCache(tClass: KClass<out RouterInterceptor>) {
        ClassCache.remove<Any>(tClass)
    }

}