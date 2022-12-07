package com.xiaojinzi.component.user.module.user_center.view

import com.xiaojinzi.component.user.module.user_center.domain.UserCenterUseCase
import com.xiaojinzi.component.user.module.user_center.domain.UserCenterUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class UserCenterViewModel(
    private val useCase: UserCenterUseCase = UserCenterUseCaseImpl(),
): BaseViewModel(),
    UserCenterUseCase by useCase{
}