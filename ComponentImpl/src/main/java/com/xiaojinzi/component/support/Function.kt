package com.xiaojinzi.component.support

import com.xiaojinzi.component.anno.support.NeedOptimizeAnno
import java.lang.Exception

interface Function<T, R> {

    /**
     * 做一个转化,从一个对象变成另一个对象
     */
    @NeedOptimizeAnno("参数名字要改一下")
    @Throws(Exception::class)
    fun apply(t: T): R

}