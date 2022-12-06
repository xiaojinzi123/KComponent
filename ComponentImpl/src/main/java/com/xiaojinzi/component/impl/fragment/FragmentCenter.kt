package com.xiaojinzi.component.impl.fragment

import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.impl.IModuleFragmentLifecycle

data class FragmentCenterBean(
    val fragmentNameSet: Set<String>,
)

/**
 * 模块 Fragment 注册和卸载的总管
 *
 * @author xiaojinzi
 */
@CheckClassNameAnno
object FragmentCenter {

    private val moduleFragmentMap: MutableMap<String, FragmentCenterBean> = HashMap()

    fun register(
        moduleName: String,
        fragmentModule: IModuleFragmentLifecycle,
        fragmentNameSet: Set<String> = emptySet(),
    ) {
        fragmentModule.initFragment()
        if (!moduleFragmentMap.containsKey(key = moduleName)) {
            moduleFragmentMap[moduleName] = FragmentCenterBean(
                fragmentNameSet = fragmentNameSet,
            )
        }
    }

    fun unregister(
        moduleName: String,
        fragmentModule: IModuleFragmentLifecycle,
    ) {
        fragmentModule.destroyFragment()
        moduleFragmentMap.remove(key = moduleName)
    }

    fun check() {
        val set: MutableSet<String> = HashSet()
        for ((_, childRouter) in moduleFragmentMap) {
            val childRouterSet = childRouter.fragmentNameSet
            for (key in childRouterSet) {
                check(!set.contains(key)) { "the name of Fragment is exist：'$key'" }
                set.add(key)
            }
        }
    }

}