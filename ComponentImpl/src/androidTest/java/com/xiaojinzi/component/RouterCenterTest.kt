package com.xiaojinzi.component

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xiaojinzi.component.bean.RouterBean
import com.xiaojinzi.component.impl.RouterCenter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [com.xiaojinzi.component.impl.RouterCenter]
 */
@RunWith(AndroidJUnit4::class)
class RouterCenterTest {

    @Test
    fun testCheckRepeatRoute() {

        RouterCenter.register(
            moduleName = "module1",
            routerMap = listOf(
                RouterBean(
                    uri = "https://module1/index",
                )
            )
        )

        RouterCenter.register(
            moduleName = "module2",
            routerMap = listOf(
                RouterBean(
                    uri = "https://module1/index",
                )
            )
        )

        Assert.assertTrue(
            kotlin.runCatching {
                RouterCenter.check()
            }.isFailure
        )

    }

    @Test
    fun testIsSameTarget() {

        val uri1 = Uri.parse("https://module1/index")

        RouterCenter.register(
            moduleName = "test",
            routerMap = listOf(
                RouterBean(
                    uri = uri1.toString(),
                    targetClass = Unit::class
                ),
            )
        )

        Assert.assertTrue(
            RouterCenter.isSameTarget(
                uri1 = uri1,
                uri2 = uri1,
            )
        )

    }

    @Test
    fun testIsMatchUri() {

        val uri = Uri.parse("https://module1/index")

        RouterCenter.register(
            moduleName = "test",
            routerMap = listOf(
                RouterBean(
                    uri = uri.toString(),
                    targetClass = Unit::class
                ),
            )
        )

        Assert.assertTrue(
            RouterCenter.isMatchUri(
                uri = uri,
            )
        )

    }

}