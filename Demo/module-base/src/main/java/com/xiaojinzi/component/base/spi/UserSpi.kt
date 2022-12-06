package com.xiaojinzi.component.base.spi

import android.os.Parcelable
import androidx.annotation.Keep
import com.xiaojinzi.support.annotation.HotObservable
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserInfoDto(
    val name: String,
    val password: String,

) : Parcelable

interface UserSpi {

    /**
     * 用户信息
     */
    @HotObservable(HotObservable.Pattern.BEHAVIOR, isShared = true)
    val userInfoObservableDto: Flow<UserInfoDto?>

    /**
     * 是否登录了
     */
    @HotObservable(HotObservable.Pattern.BEHAVIOR, isShared = false)
    val isLoginObservableDto: Flow<Boolean>

    /**
     * 登录
     */
    suspend fun login(
        userName: String,
        userPassword: String,
    )

}