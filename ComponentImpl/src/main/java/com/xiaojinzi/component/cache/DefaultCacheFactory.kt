package com.xiaojinzi.component.cache

import com.xiaojinzi.component.Component

class DefaultCacheFactory<K, V> : Cache.Factory<K, V> {

    override fun build(type: CacheType): Cache<K, V> {
        return LruCache(type.calculateCacheSize(Component.getApplication()))
    }

    companion object {
        val INSTANCE = DefaultCacheFactory<Any, Any>()
    }

}