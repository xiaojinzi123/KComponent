package com.xiaojinzi.component.user.spi

import com.xiaojinzi.component.anno.ServiceAnno
import com.xiaojinzi.component.base.spi.UserInfoDto
import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.support.ktx.MutableSharedStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ServiceAnno(UserSpi::class)
class UserSpiImpl : UserSpi {

    override val userInfoObservableDto: Flow<UserInfoDto?> = MutableSharedStateFlow(
        initValue = null
    )
    override val isLoginObservableDto = userInfoObservableDto.map { it != null }

    override suspend fun login(userName: String, userPassword: String) {
        TODO("Not yet implemented")
    }

}