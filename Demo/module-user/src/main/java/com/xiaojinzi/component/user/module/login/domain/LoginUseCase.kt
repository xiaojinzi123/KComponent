package com.xiaojinzi.component.user.module.login.domain

import android.content.Context
import androidx.annotation.UiContext
import com.xiaojinzi.support.annotation.HotObservable
import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl
import com.xiaojinzi.support.ktx.MutableSharedStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ViewModelLayer
interface LoginUseCase : BaseUseCase {

    /**
     * 用户名
     */
    @HotObservable(HotObservable.Pattern.BEHAVIOR, isShared = true)
    val userNameObservableDto: MutableSharedStateFlow<String>

    /**
     * 密码
     */
    @HotObservable(HotObservable.Pattern.BEHAVIOR, isShared = true)
    val userPasswordObservableDto: MutableSharedStateFlow<String>

    /**
     * 是否可以继续, 登录按钮是否好使
     */
    @HotObservable(HotObservable.Pattern.BEHAVIOR, isShared = false)
    val canNextObservableDto: Flow<Boolean>

    /**
     * 登录
     */
    fun login(
        @UiContext context: Context
    )

}

@ViewModelLayer
class LoginUseCaseImpl(
) : BaseUseCaseImpl(), LoginUseCase {

    override val userNameObservableDto = MutableSharedStateFlow(initValue = "靓仔")

    override val userPasswordObservableDto = MutableSharedStateFlow(initValue = "123456")

    override val canNextObservableDto = combine(
        userNameObservableDto,
        userPasswordObservableDto
    ) { userName, userPassword ->
        userName.trim().isNotEmpty() && userPassword.trim().isNotEmpty()
    }

    override fun login(context: Context) {

    }

}