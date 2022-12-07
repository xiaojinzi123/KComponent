package com.xiaojinzi.component.user.module.login.domain

import android.app.Activity
import android.content.Context
import androidx.annotation.UiContext
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.impl.service.service
import com.xiaojinzi.support.annotation.HotObservable
import com.xiaojinzi.support.annotation.ViewModelLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCase
import com.xiaojinzi.support.architecture.mvvm1.BaseUseCaseImpl
import com.xiaojinzi.support.ktx.ErrorIgnoreContext
import com.xiaojinzi.support.ktx.MutableSharedStateFlow
import com.xiaojinzi.support.ktx.getActivity
import com.xiaojinzi.support.ktx.tryFinishActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
        scope.launch(context = ErrorIgnoreContext) {

            val name = userNameObservableDto.first()
            val password = userPasswordObservableDto.first()

            // 进行登录
            UserSpi::class.service()!!.login(
                userName = name,
                userPassword = password
            )

            context.getActivity()?.apply {
                this.setResult(Activity.RESULT_OK)
            }

            // 销毁界面
            context.tryFinishActivity()

        }
    }

}