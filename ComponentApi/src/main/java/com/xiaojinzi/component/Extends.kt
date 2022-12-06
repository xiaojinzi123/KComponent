package com.xiaojinzi.component

fun String.packageName(): String {
    return substring(0, lastIndexOf('.'))
}

fun String.simpleClassName(): String {
    return substring(lastIndexOf('.') + 1)
}