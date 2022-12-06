package com.xiaojinzi.component.impl.interceptor

import androidx.annotation.UiThread
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.error.InterceptorNameExistException
import com.xiaojinzi.component.impl.RouterInterceptor
import com.xiaojinzi.component.support.RouterInterceptorCache
import com.xiaojinzi.component.support.Utils
import kotlin.reflect.KClass

data class InterceptorCenterBean(
    val globalInterceptorList: List<InterceptorBean>,
    val interceptorMap: Map<String, KClass<out RouterInterceptor>>
)

/**
 * 中央拦截器
 * time   : 2018/12/26
 *
 * @author : xiaojinzi
 */
@CheckClassNameAnno
object InterceptorCenter {

    /**
     * 子拦截器对象管理 map
     */
    private val moduleInterceptorMap: MutableMap<String, InterceptorCenterBean> = HashMap()

    /**
     * 公共的拦截器列表
     */
    private val mGlobalInterceptorList: MutableList<RouterInterceptor> = ArrayList()

    /**
     * 每个业务组件的拦截器 name --> Class 映射关系的总的集合
     * 这种拦截器不是全局拦截器,是随时随地使用的拦截器,见 [com.xiaojinzi.component.impl.Navigator.interceptorNames]
     */
    private val mInterceptorMap: MutableMap<String, KClass<out RouterInterceptor>> = HashMap()

    /**
     * 是否公共的拦截器列表发生变化
     */
    private var isInterceptorListHaveChange = false

    /**
     * 获取全局拦截器
     */
    @get:UiThread
    val globalInterceptorList: List<RouterInterceptor>
        get() {
            if (isInterceptorListHaveChange) {
                loadAllGlobalInterceptor()
                isInterceptorListHaveChange = false
            }
            return mGlobalInterceptorList
        }

    fun register(
        moduleName: String,
        globalInterceptorList: List<InterceptorBean>,
        interceptorMap: Map<String, KClass<out RouterInterceptor>>,
    ) {
        if (!moduleInterceptorMap.containsKey(key = moduleName)) {
            isInterceptorListHaveChange = true
            moduleInterceptorMap[moduleName] = InterceptorCenterBean(
                globalInterceptorList = globalInterceptorList,
                interceptorMap = interceptorMap,
            )
            mInterceptorMap.putAll(from = interceptorMap)
        }
    }

    fun unregister(
        moduleName: String,
    ) {
        moduleInterceptorMap.remove(key = moduleName)?.let { interceptorCenterBean ->
            // 子拦截器列表
            val childInterceptorMap = interceptorCenterBean.interceptorMap
            for ((key, value) in childInterceptorMap) {
                mInterceptorMap.remove(key = key)
                RouterInterceptorCache.removeCache(tClass = value)
            }
            isInterceptorListHaveChange = true
        }
    }

    /**
     * 按顺序弄好所有全局拦截器
     */
    @UiThread
    private fun loadAllGlobalInterceptor() {
        mGlobalInterceptorList.clear()
        val totalList: MutableList<InterceptorBean> = ArrayList()
        // 加载各个子拦截器对象中的拦截器列表
        for ((_, value) in moduleInterceptorMap) {
            val list = value.globalInterceptorList
            totalList.addAll(list)
        }
        // 排序所有的拦截器对象,按照优先级排序
        totalList.sortByDescending { it.priority }
        for (item in totalList) {
            mGlobalInterceptorList.add(
                RouterInterceptorCache.getInterceptorByClass(
                    tClass = item.interceptor
                )!!
            )
        }
    }

    @UiThread
    fun getByName(interceptorName: String): RouterInterceptor? {
        // 拿到拦截器的 Class 对象
        return mInterceptorMap[interceptorName]?.let {
            RouterInterceptorCache.getInterceptorByClass(tClass = it)
        }
    }

    /**
     * 做拦截器的名称是否重复的工作
     */
    @UiThread
    fun check() {
        Utils.checkMainThread()
        val set: MutableSet<String> = HashSet()
        for ((_, value) in moduleInterceptorMap) {
            val childInterceptor = value ?: continue
            val childInterceptorNames = childInterceptor.interceptorMap.keys
            if (childInterceptorNames.isEmpty()) {
                continue
            }
            for (interceptorName in childInterceptorNames) {
                if (set.contains(interceptorName)) {
                    throw InterceptorNameExistException("the interceptor's name is exist：$interceptorName")
                }
                set.add(interceptorName)
            }
        }
    }

}