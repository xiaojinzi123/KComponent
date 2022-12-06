package com.xiaojinzi.component.compiler.kt.bean

import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * time   : 2018/07/26
 *
 * @author : xiaojinzi
 */
class RouterAnnoBean(

    val regex: String? = null,
    val scheme: String? = null,
    val host: String? = null,
    val path: String? = null,
    val desc: String? = null,

    val interceptorPriorities: List<Int>,

    val interceptorNamePriorities: List<Int>,

    // value是实现类的全类名
    val interceptors: List<String> = emptyList(),

    // 拦截器的一个别名
    val interceptorNames: List<String> = emptyList(),

    // 可是是一个Activity 类或者是一个静态方法
    val rawType: KSAnnotated,

) {

    /**
     * host 和 path 之间一定有 /
     */
    fun hostAndPath(): String {
        return host + path
    }

    init {

        // 对拦截器的优先级的定义进行校验
        if (interceptorPriorities.isNotEmpty()) {
            if (interceptorPriorities.size != interceptors.size) {
                throw RuntimeException("interceptorPriorities size must equal interceptors size")
            }
        }
        if (interceptorNamePriorities.isNotEmpty()) {
            if (interceptorNamePriorities.size != interceptorNames.size) {
                throw RuntimeException("interceptorNamePriorities size must equal interceptorNames size")
            }
        }

    }

}