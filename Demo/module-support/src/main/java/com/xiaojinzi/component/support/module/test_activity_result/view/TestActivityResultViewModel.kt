package com.xiaojinzi.component.support.module.test_activity_result.view

import com.xiaojinzi.component.support.module.test_activity_result.domain.TestActivityResultUseCase
import com.xiaojinzi.component.support.module.test_activity_result.domain.TestActivityResultUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class TestActivityResultViewModel(
    private val useCase: TestActivityResultUseCase = TestActivityResultUseCaseImpl(),
): BaseViewModel(),
    TestActivityResultUseCase by useCase{
}