package com.xiaojinzi.component.app1.spi

import com.xiaojinzi.component.anno.ServiceDecoratorAnno
import com.xiaojinzi.component.base.spi.UserSpi
import kotlinx.coroutines.flow.map

@ServiceDecoratorAnno(UserSpi::class)
class UserSpiAppendNameDecorator(target: UserSpi) : UserSpi by target {

    override val userInfoObservableDto = target
        .userInfoObservableDto
        .map { it?.copy(name = it.name + "_增强") }

}