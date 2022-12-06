package com.xiaojinzi.component.support.module.web_test.view

import com.xiaojinzi.component.support.module.web_test.domain.WebTestUseCase
import com.xiaojinzi.component.support.module.web_test.domain.WebTestUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class WebTestViewModel(
    private val useCase: WebTestUseCase = WebTestUseCaseImpl(),
): BaseViewModel(),
    WebTestUseCase by useCase{
}