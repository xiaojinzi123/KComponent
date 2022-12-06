package com.xiaojinzi.component.impl.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.xiaojinzi.component.error.FragmentNotFoundException
import com.xiaojinzi.component.impl.fragment.FragmentManager.get
import com.xiaojinzi.component.support.Utils.isMainThread
import com.xiaojinzi.component.support.Utils.postActionToMainThread
import io.reactivex.Single

/**
 * 关于 Rx版本的增强版本,使用这个类在服务上出现的任何错误,如果您不想处理
 * 这里都能帮您自动过滤掉,如果你写了错误处理,则会回调给您
 * time   : 2019/10/12
 *
 * @author : xiaojinzi
 */
object RxFragmentManager {

    /**
     * 这里最主要的实现就是把出现的错误转化为 [ServiceInvokeException]
     * 然后就可以当用户不想处理RxJava的错误的时候 [com.xiaojinzi.component.support.RxErrorConsumer] 进行忽略了
     * 获取实现类,这个方法实现了哪些这里罗列一下：
     * 1. 保证在找不到 Fragment 的时候不会有异常, 你只管写正确情况的逻辑代码
     * 2. 保证 Fragment 在主线程上被创建
     * 3. 在保证了第一点的情况下保证不改变 RxJava 的执行线程
     * 4. 保证调用任何一个服务实现类的时候出现的错误用 [ServiceInvokeException]
     * 代替,当然了,真实的错误在 [Throwable.getCause] 中可以获取到
     */
    fun with(fragmentFlag: String, bundle: Bundle?): Single<Fragment> {
        return Single.fromCallable {
            var tempImpl: Fragment? = null
            tempImpl = if (isMainThread()) {
                get(fragmentFlag, bundle)
            } else {
                // 这段代码如何为空的话会直接抛出异常
                blockingGetInChildThread(fragmentFlag, bundle)
            }
            val serviceImpl = tempImpl ?: throw FragmentNotFoundException(fragmentFlag)
            serviceImpl
        }
    }

    /**
     * 在主线程中去创建对象,然后在其他线程拿到
     */
    private fun blockingGetInChildThread(fragmentFlag: String, bundle: Bundle?): Fragment {
        return Single.create<Fragment> { emitter -> // 主线程去获取,因为框架任何一个用户自定义的类创建的时候都会在主线程上被创建
            postActionToMainThread(Runnable {
                if (emitter.isDisposed) {
                    return@Runnable
                }
                val fragment = get(fragmentFlag, bundle)
                if (fragment == null) {
                    emitter.onError(FragmentNotFoundException("fragmentFlag is '+fragmentFlag+'"))
                } else {
                    emitter.onSuccess(fragment)
                }
            })
        }.blockingGet()
    }

}