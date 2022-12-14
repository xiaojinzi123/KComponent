package com.xiaojinzi.component.user.spi

import com.xiaojinzi.component.anno.ServiceAnno
import com.xiaojinzi.component.base.spi.UserInfoDto
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.impl.service.ServiceManager
import com.xiaojinzi.support.ktx.MutableSharedStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

@ServiceAnno(
    value = [UserSpi::class],
    name = ["testUserSpi2"]
)
fun userSpi(): UserSpi {

    return object : UserSpi {

        override val userInfoObservableDto: Flow<UserInfoDto?> = emptyFlow()

        override val isLoginObservableDto: Flow<Boolean> = emptyFlow()

        override suspend fun login(userName: String, userPassword: String) {
        }

    }

}

@ServiceAnno(
    value = [UserSpi::class, UserSpi::class],
    name = [ServiceManager.DEFAULT_NAME, "testUserSpi1"],
)
class UserSpiImpl : UserSpi {

    override val userInfoObservableDto = MutableSharedStateFlow<UserInfoDto?>(
        initValue = null,
    )

    override val isLoginObservableDto = userInfoObservableDto.map { it != null }

    override suspend fun login(userName: String, userPassword: String) {
        userInfoObservableDto.emit(
            value = UserInfoDto(
                name = userName,
                password = userPassword,
            )
        )
    }

}