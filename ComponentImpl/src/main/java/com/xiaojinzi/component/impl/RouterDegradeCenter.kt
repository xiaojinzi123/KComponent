package com.xiaojinzi.component.impl

import androidx.annotation.Keep
import com.xiaojinzi.component.Component.requiredConfig
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.bean.RouterDegradeBean
import com.xiaojinzi.component.router.IComponentCenterRouterDegrade
import com.xiaojinzi.component.router.IComponentHostRouterDegrade
import com.xiaojinzi.component.support.ASMUtil
import com.xiaojinzi.component.support.RouterDegradeCache
import com.xiaojinzi.component.support.Utils
import java.util.*

@Keep
data class RouterDegradeItem(
    /**
     * 优先级
     */
    val priority: Int,
    val routerDegrade: RouterDegrade
)

@CheckClassNameAnno
object RouterDegradeCenter {

    /**
     * 子拦截器对象管理 map
     */
    private val moduleRouterDegradeMap: MutableMap<String, List<RouterDegradeBean>> =
        mutableMapOf()

    /**
     * 全局的降级处理, 数据一定是排序过的
     */
    private val mGlobalRouterDegradeList: MutableList<RouterDegradeItem> = mutableListOf()

    /**
     * 是否降级处理列表发生变化
     */
    private var isRouterDegradeListHaveChange = false

    val globalRouterDegradeList: List<RouterDegrade>
        get() {
            if (isRouterDegradeListHaveChange) {
                loadAllGlobalRouterDegrade()
                isRouterDegradeListHaveChange = false
            }
            val result: MutableList<RouterDegrade> = ArrayList()
            for (item in mGlobalRouterDegradeList) {
                result.add(item.routerDegrade)
            }
            return result
        }

    /**
     * 按顺序弄好所有全局拦截器
     */
    private fun loadAllGlobalRouterDegrade() {
        mGlobalRouterDegradeList.clear()
        moduleRouterDegradeMap
            .asSequence()
            .flatMap { it.value }
            .map {
                RouterDegradeItem(
                    priority = it.priority,
                    routerDegrade = RouterDegradeCache.getRouterDegradeByClass(tClass = it.targetClass!!)!!
                )
            }
            .sortedByDescending { it.priority }
            .forEach {
                mGlobalRouterDegradeList.add(it)
            }
    }

    fun register(
        moduleName: String,
        list: List<RouterDegradeBean>
    ) {
        isRouterDegradeListHaveChange = true
        moduleRouterDegradeMap[moduleName] = list
    }

    fun unregister(moduleName: String,) {
        moduleRouterDegradeMap.remove(key = moduleName)
        isRouterDegradeListHaveChange = true
    }

}