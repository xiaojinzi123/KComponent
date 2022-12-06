package com.xiaojinzi.component.base.support

import com.xiaojinzi.component.base.spi.UserSpi
import com.xiaojinzi.component.impl.service.ServiceManager

object Services {

    val userSpi: UserSpi? = ServiceManager.get(
        tClass = UserSpi::class,
    )

}