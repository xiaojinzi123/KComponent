package com.xiaojinzi.component.demo.module.test_route.view

import com.xiaojinzi.component.demo.module.test_route.domain.TestRouteUseCase
import com.xiaojinzi.component.demo.module.test_route.domain.TestRouteUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class TestRouteViewModel(
    private val useCase: TestRouteUseCase = TestRouteUseCaseImpl(),
): BaseViewModel(),
    TestRouteUseCase by useCase{
}