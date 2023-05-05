package com.xiaojinzi.component.app1.module.default.domain

import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl

@ViewModelLayer
interface DefaultUseCase : BaseUseCase {
    // TODO
}

@ViewModelLayer
class DefaultUseCaseImpl(
) : BaseUseCaseImpl(), DefaultUseCase {
    // TODO
}