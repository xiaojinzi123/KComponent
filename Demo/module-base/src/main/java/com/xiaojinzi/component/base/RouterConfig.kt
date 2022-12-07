package com.xiaojinzi.component.base

object RouterConfig {

    const val HOST_SYSTEM = "system"
    const val SYSTEM_CALL_PHONE = "$HOST_SYSTEM/callPhone"
    const val SYSTEM_TAKE_PHOTO = "$HOST_SYSTEM/takePhoto"
    const val SYSTEM_APP_DETAIL = "$HOST_SYSTEM/appDetail"

    const val HOST_APP = "app"
    const val APP_TEST_NO_TARGET = "$HOST_APP/testNoTarget"
    const val APP_DEFAULT = "$HOST_APP/default"
    const val APP_MAIN = "$HOST_APP/main"
    const val APP_TEST_ROUTE = "$HOST_APP/testRoute"
    const val APP_FRAGMENT_ROUTE_TEST = "$HOST_APP/fragmentRouteTest"

    const val HOST_USER = "user"
    const val USER_LOGIN = "$HOST_USER/login"
    const val USER_USER_CENTER = "$HOST_USER/userCenter"

    const val HOST_BASE = "base"

    const val HOST_SUPPORT = "support"
    const val SUPPORT_WEB_TEST = "$HOST_SUPPORT/webTest"
    const val SUPPORT_TEST_ACTIVITY_RESULT = "$HOST_SUPPORT/testActivityResult"
    const val SUPPORT_TEST_PARAMETER_AUTOWIRE = "$HOST_SUPPORT/testParameterAutowire"
    const val SUPPORT_TEST_QUERY = "$HOST_SUPPORT/testQuery"
    const val SUPPORT_TEST_INJECT = "$HOST_SUPPORT/testInject"
    const val SUPPORT_PERMISSION_REQUEST = "$HOST_SUPPORT/permissionRequest"

    const val FRAGMENT_USER1 = "user1"
    const val FRAGMENT_USER2 = "user2"

    const val INTERCEPTOR_PERMISSION_LOGIN = "login"
    const val INTERCEPTOR_PERMISSION_CALL_PHONE = "permissionCallPhone"
    const val INTERCEPTOR_PERMISSION_CAMERA = "permissionCamera"

}