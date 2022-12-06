package com.xiaojinzi.component.demo.module.main.domain

import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl

@ViewModelLayer
interface MainUseCase : BaseUseCase {

    companion object {
        const val CHANNEL_ID = "ComponentChannel"
    }

}

@ViewModelLayer
class MainUseCaseImpl(
) : BaseUseCaseImpl(), MainUseCase {
}