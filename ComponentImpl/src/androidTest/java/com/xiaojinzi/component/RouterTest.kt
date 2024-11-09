package com.xiaojinzi.component

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.xiaojinzi.component.activities.LoginAct
import com.xiaojinzi.component.bean.RouterBean
import com.xiaojinzi.component.impl.Router
import com.xiaojinzi.component.impl.RouterCenter
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

object TestRouterConfig {

    const val SCHEME = "router"

    const val HOST_USER = "user"
    const val USER_LOGIN = "$SCHEME://$HOST_USER/login"

}

@RunWith(AndroidJUnit4::class)
class RouterTest {

    @Test
    fun testStartActivity() {
        val componentKClass = Mockito.mock<Component>()
        Mockito.`when`(componentKClass.requiredConfig()).thenReturn(
            Config(),
        )
        Router
            .with(context = InstrumentationRegistry.getInstrumentation().context)
            .url(url = TestRouterConfig.USER_LOGIN)
            .forward {
                throw NullPointerException()
            }
    }

    init {

        RouterCenter.register(
            moduleName = TestRouterConfig.HOST_USER,
            routerMap = listOf(
                RouterBean(
                    uri = TestRouterConfig.USER_LOGIN,
                    targetClass = LoginAct::class,
                )
            ),
        )

    }

}