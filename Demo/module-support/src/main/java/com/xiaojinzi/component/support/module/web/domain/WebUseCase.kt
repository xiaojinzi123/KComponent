package com.xiaojinzi.component.support.module.web.domain

import com.xiaojinzi.support.annotation.HotObservable
import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl
import com.xiaojinzi.support.ktx.MutableInitOnceData

@ViewModelLayer
interface WebUseCase : BaseUseCase {

    val urlInitData: MutableInitOnceData<String?>

}

@ViewModelLayer
class WebUseCaseImpl(
) : BaseUseCaseImpl(), WebUseCase {

    override val urlInitData = MutableInitOnceData<String?>()

}