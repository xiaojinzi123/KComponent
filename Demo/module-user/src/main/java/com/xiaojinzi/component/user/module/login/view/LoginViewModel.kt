package com.xiaojinzi.component.user.module.login.view

import com.xiaojinzi.component.user.module.login.domain.LoginUseCase
import com.xiaojinzi.component.user.module.login.domain.LoginUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class LoginViewModel(
    private val useCase: LoginUseCase = LoginUseCaseImpl(),
): BaseViewModel(),
    LoginUseCase by useCase{
}