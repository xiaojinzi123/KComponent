package com.xiaojinzi.component.support.module.test_parameter_autowire.domain

import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl
import com.xiaojinzi.support.ktx.MutableInitOnceData

@ViewModelLayer
interface TestParameterAutowireUseCase : BaseUseCase {

    val data1InitData: MutableInitOnceData<String?>

    val data2InitData: MutableInitOnceData<String?>

    val nameInitData: MutableInitOnceData<String?>

    val passInitData: MutableInitOnceData<String?>

}

@ViewModelLayer
class TestParameterAutowireUseCaseImpl(
) : BaseUseCaseImpl(), TestParameterAutowireUseCase {

    override val data1InitData = MutableInitOnceData<String?>()

    override val data2InitData = MutableInitOnceData<String?>()

    override val nameInitData = MutableInitOnceData<String?>()

    override val passInitData = MutableInitOnceData<String?>()

}