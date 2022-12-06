package com.xiaojinzi.component.support.module.permission_request.view

import com.xiaojinzi.component.support.module.permission_request.domain.PermissionRequestUseCase
import com.xiaojinzi.component.support.module.permission_request.domain.PermissionRequestUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class PermissionRequestViewModel(
    private val useCase: PermissionRequestUseCase = PermissionRequestUseCaseImpl(),
): BaseViewModel(),
    PermissionRequestUseCase by useCase{
}