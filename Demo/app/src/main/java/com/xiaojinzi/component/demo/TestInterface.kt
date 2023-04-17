package com.xiaojinzi.component.demo

import io.reactivex.Single

interface TestInterface {

    fun test(): Single<String>

}