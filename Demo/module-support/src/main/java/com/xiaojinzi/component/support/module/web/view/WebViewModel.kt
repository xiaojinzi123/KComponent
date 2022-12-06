package com.xiaojinzi.component.support.module.web.view

import com.xiaojinzi.component.support.module.web.domain.WebUseCase
import com.xiaojinzi.component.support.module.web.domain.WebUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class WebViewModel(
    private val useCase: WebUseCase = WebUseCaseImpl(),
): BaseViewModel(),
    WebUseCase by useCase{
}