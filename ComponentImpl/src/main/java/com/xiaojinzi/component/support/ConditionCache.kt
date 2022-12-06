package com.xiaojinzi.component.support

import com.xiaojinzi.component.Component.isDebug
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.cache.ClassCache.get
import com.xiaojinzi.component.cache.ClassCache.put
import com.xiaojinzi.component.error.CreateInterceptorException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 条件的缓存 [Condition]
 *
 *
 * time   : 2018/12/03
 *
 * @author : xiaojinzi
 */
@CheckClassNameAnno
object ConditionCache {

    /**
     * 内部做了缓存,如果缓存中没有就会反射创建拦截器对象
     */
    @Synchronized
    fun getByClass(tClass: KClass<out Condition>): Condition {
        var t = get<Condition>(clazz = tClass)
        if (t != null) {
            return t
        }
        try {
            // 创建拦截器
            t = tClass.primaryConstructor!!.call()
            put(tClass, t)
        } catch (e: Exception) {
            if (isDebug) {
                throw CreateInterceptorException(e)
            }
        }
        return t!!
    }

}