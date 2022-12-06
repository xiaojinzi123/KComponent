package com.xiaojinzi.component.cache

import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.cache.CacheType.Companion.CLASS_CACHE
import kotlin.reflect.KClass

/**
 * Class 的缓存的工具类
 */
@CheckClassNameAnno
object ClassCache {

    private val classCache = DefaultCacheFactory.INSTANCE.build(CLASS_CACHE)

    @Synchronized
    fun <T> put(clazz: KClass<*>, o: T) {
        classCache.put(clazz, o)
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T> get(clazz: KClass<*>): T? {
        return classCache[clazz] as T?
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T> remove(clazz: KClass<*>): T? {
        return classCache.remove(clazz) as T?
    }

    @Synchronized
    fun clear() {
        classCache.clear()
    }

}