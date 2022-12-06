package com.xiaojinzi.component.support

import android.app.Application
import android.content.Context
import com.xiaojinzi.component.Component.getApplication
import com.xiaojinzi.component.Component.isDebug
import com.xiaojinzi.component.cache.ClassCache.get
import com.xiaojinzi.component.cache.ClassCache.put
import com.xiaojinzi.component.error.CreateInterceptorException
import com.xiaojinzi.component.impl.RouterDegrade
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

/**
 * 支持缓存自定义的降级处理工具类
 *
 * @author : xiaojinzi
 */
object RouterDegradeCache {

    /**
     * 内部做了缓存,如果缓存中没有就会反射创建拦截器对象
     */
    @Synchronized
    fun getRouterDegradeByClass(tClass: KClass<out RouterDegrade>): RouterDegrade? {
        var t = get<RouterDegrade>(clazz = tClass)
        if (t != null) {
            return t
        }
        try {
            // 创建拦截器
            t = create(tClass = tClass)
            if (t == null) {
                throw InstantiationException("do you write default constructor or a constructor with parameter 'Application' or  a constructor with parameter 'Context' ")
            } else {
                put(tClass, t)
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
    private fun create(tClass: KClass<out RouterDegrade>): RouterDegrade? {
        val constructors = tClass.constructors ?: return null
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

}