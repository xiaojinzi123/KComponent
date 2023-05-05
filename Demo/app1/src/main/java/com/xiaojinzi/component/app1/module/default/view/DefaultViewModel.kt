package com.xiaojinzi.component.app1.module.default.view

import com.xiaojinzi.component.app1.module.default.domain.DefaultUseCase
import com.xiaojinzi.component.app1.module.default.domain.DefaultUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class DefaultViewModel(
    private val useCase: DefaultUseCase = DefaultUseCaseImpl(),
): BaseViewModel(),
    DefaultUseCase by useCase{
}