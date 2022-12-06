package com.xiaojinzi.component.support.module.permission_request.domain

import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl
import com.xiaojinzi.support.ktx.MutableInitOnceData

@ViewModelLayer
interface PermissionRequestUseCase : BaseUseCase {

    val permissionInitData: MutableInitOnceData<String>

    val permissionDescInitData: MutableInitOnceData<String?>

}

@ViewModelLayer
class PermissionRequestUseCaseImpl(
) : BaseUseCaseImpl(), PermissionRequestUseCase {

    override val permissionInitData = MutableInitOnceData<String>()

    override val permissionDescInitData = MutableInitOnceData<String?>()

}