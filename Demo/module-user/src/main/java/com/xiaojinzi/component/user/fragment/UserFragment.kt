package com.xiaojinzi.component.user.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xiaojinzi.component.Component
import com.xiaojinzi.component.anno.AttrValueAutowiredAnno
import com.xiaojinzi.component.anno.FragmentAnno
import com.xiaojinzi.component.base.RouterConfig
import com.xiaojinzi.component.user.R
import com.xiaojinzi.component.user.databinding.UserTest1Binding

class UserFragment : Fragment(R.layout.user_test1) {

    companion object {

        @FragmentAnno(RouterConfig.FRAGMENT_USER2)
        fun newInstance(
            args: Bundle = Bundle(),
        ): UserFragment {
            val fragment = UserFragment()
            fragment.arguments = args
            return fragment
        }

    }

    private val viewBinding by lazy {
        UserTest1Binding.bind(requireView())
    }

    @AttrValueAutowiredAnno
    var data: String? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Component.inject(target = this)
        viewBinding.tv.text = "我是 User 模块的一个 Fragment\n传递过来的数据是：$data"
    }

}

@FragmentAnno(RouterConfig.FRAGMENT_USER1)
fun newInstance(
    args: Bundle = Bundle(),
): UserFragment {
    val fragment = UserFragment()
    fragment.arguments = args
    return fragment
}