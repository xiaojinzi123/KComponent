package com.xiaojinzi.component.app1.module.main.view

import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.impl.RouterRequest
import kotlin.reflect.KClass

@RouterAnno(
    hostAndPath = "test/xxxxx",
)
fun toMainView(
    request: RouterRequest,
): KClass<MainAct> = MainAct::class