package com.xiaojinzi.component.demo.module.fragment_route_test.view

import com.xiaojinzi.component.demo.module.fragment_route_test.domain.FragmentRouteTestUseCase
import com.xiaojinzi.component.demo.module.fragment_route_test.domain.FragmentRouteTestUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class FragmentRouteTestViewModel(
    private val useCase: FragmentRouteTestUseCase = FragmentRouteTestUseCaseImpl(),
): BaseViewModel(),
    FragmentRouteTestUseCase by useCase{
}