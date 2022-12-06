package com.xiaojinzi.component.impl

import androidx.fragment.app.Fragment
import com.xiaojinzi.component.impl.fragment.FragmentManager.get

/**
 * 一个为 [Fragment] 设计的导航器
 */
class FragmentNavigator(
    private val fragmentFlag: String,
    private val bundleBuilder: IBundleBuilderImpl<FragmentNavigator> = IBundleBuilderImpl(),
) : IBundleBuilder<FragmentNavigator> by bundleBuilder {

    fun navigate(): Fragment? {
        return get(fragmentFlag, bundle)
    }

    init {
        bundleBuilder.thisObject = this
    }

}