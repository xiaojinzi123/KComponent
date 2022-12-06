package com.xiaojinzi.component.support.module.test_parameter_autowire.view

import com.xiaojinzi.component.support.module.test_parameter_autowire.domain.TestParameterAutowireUseCase
import com.xiaojinzi.component.support.module.test_parameter_autowire.domain.TestParameterAutowireUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class TestParameterAutowireViewModel(
    private val useCase: TestParameterAutowireUseCase = TestParameterAutowireUseCaseImpl(),
): BaseViewModel(),
    TestParameterAutowireUseCase by useCase{
}