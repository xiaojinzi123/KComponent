package com.xiaojinzi.component.support

import androidx.annotation.UiThread
import com.xiaojinzi.component.impl.RouterRequest

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeBeforeRouteAction() {
    this.beforeRouteAction?.invoke()
}

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeBeforeStartActivityAction() {
    this.beforeStartActivityAction?.invoke()
}

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeAfterStartActivityAction() {
    this.afterStartActivityAction?.invoke()
}

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeAfterRouteSuccessAction() {
    this.afterRouteSuccessAction?.invoke()
    this.executeAfterRouteEventAction()
}

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeAfterActivityResultRouteSuccessAction() {
    this.afterActivityResultRouteSuccessAction?.invoke()
    this.executeAfterRouteEventAction()
}

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeAfterRouteErrorAction() {
    this.afterRouteErrorAction?.invoke()
    this.executeAfterRouteEventAction()
}

@UiThread
@Throws(Exception::class)
fun RouterRequest.executeAfterRouteEventAction() {
    this.afterRouteEventAction?.invoke()
}