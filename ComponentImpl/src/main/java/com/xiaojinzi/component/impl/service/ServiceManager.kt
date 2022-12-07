package com.xiaojinzi.component.impl.service

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.xiaojinzi.component.anno.support.CheckClassNameAnno
import com.xiaojinzi.component.anno.support.NotAppUseAnno
import com.xiaojinzi.component.error.ServiceRepeatCreateException
import com.xiaojinzi.component.support.Callable
import com.xiaojinzi.component.support.DecoratorCallable
import com.xiaojinzi.component.support.SingletonCallable
import com.xiaojinzi.component.support.Utils
import java.util.*
import kotlin.reflect.KClass

/**
 * 服务的容器,使用这个服务容器你需要判断获取到的服务是否为空,对于使用者来说还是比较不方便的
 * 建议使用 Service 扩展的版本 RxService
 *
 * @author xiaojinzi
 */
@CheckClassNameAnno
object ServiceManager {

    const val DEFAULT_NAME = ""

    /**
     * Service 的集合. 线程不安全的
     */
    private val serviceMap: MutableMap<KClass<*>, HashMap<String, Callable<*>>> = mutableMapOf()

    /**
     * Service 装饰者的集合. 线程不安全的
     */
    private val serviceDecoratorMap: MutableMap<KClass<*>, HashMap<String, DecoratorCallable<*>>> =
        mutableMapOf()

    private val uniqueServiceSet: MutableSet<String> = HashSet()

    /**
     * 需要自动初始化的 Service 的 class
     */
    private val _autoInitMap: MutableMap<KClass<*>, MutableSet<String>> = mutableMapOf()
    private val autoInitMap = _autoInitMap

    private fun getAutoInitMapValue(tClass: KClass<*>): MutableSet<String> {
        if (!_autoInitMap.containsKey(key = tClass)) {
            _autoInitMap[tClass] = mutableSetOf()
        }
        return _autoInitMap[tClass]!!
    }

    /**
     * 注册自动注册的 Service Class
     */
    @AnyThread
    @JvmOverloads
    @NotAppUseAnno
    fun <T : Any> registerAutoInit(tClass: KClass<T>, name: String = DEFAULT_NAME) {
        getAutoInitMapValue(tClass = tClass).add(element = name)
    }

    /**
     * 反注册自动注册的 Service Class
     */
    @AnyThread
    @JvmOverloads
    @NotAppUseAnno
    fun <T : Any> unregisterAutoInit(tClass: KClass<T>, name: String = DEFAULT_NAME) {
        getAutoInitMapValue(tClass = tClass).remove(
            element = name,
        )
    }

    /**
     *
     * 初始化那些需要自动初始化的 Service
     */
    @WorkerThread
    @NotAppUseAnno
    fun autoInitService() {
        for ((key, valueList) in autoInitMap) {
            valueList.forEach { name ->
                // 初始化实现类
                get(tClass = key, name = name)
            }
        }
    }

    /**
     * 注册一个装饰者
     *
     * @param tClass   装饰目标的接口
     * @param uid      注册的这个装饰者的唯一的标记, 方便后续进行反注册
     * @param callable 装饰者的对象获取者
     * @param <T>      装饰目标
    </T> */
    @AnyThread
    @NotAppUseAnno
    fun <T : Any> registerDecorator(
        tClass: KClass<T>,
        uid: String,
        callable: DecoratorCallable<out T>
    ) {
        synchronized(serviceDecoratorMap) {
            var map = serviceDecoratorMap[tClass]
            if (map == null) {
                map = HashMap()
                serviceDecoratorMap[tClass] = map
            }
            if (map.containsKey(uid)) {
                throw RuntimeException(tClass.simpleName + " the key of '" + uid + "' is exist")
            }
            map.put(uid, callable)
        }
    }

    /**
     * 注册一个装饰者
     *
     * @param tClass 装饰目标的接口
     * @param uid    注册的这个装饰者的唯一的标记
     * @param <T>    装饰目标
    </T> */
    @AnyThread
    @NotAppUseAnno
    fun <T : Any> unregisterDecorator(tClass: KClass<T>, uid: String) {
        synchronized(serviceDecoratorMap) {
            val map = serviceDecoratorMap[tClass]
            map?.remove(uid)
        }
    }

    @AnyThread
    @NotAppUseAnno
    fun <T : Any> register(tClass: KClass<T>, callable: Callable<out T>) {
        register(tClass = tClass, name = DEFAULT_NAME, callable = callable)
    }

    /**
     * 你可以注册一个服务,服务的初始化可以是懒加载的
     * 注册的时候, 不会初始化目标 Service 的
     * [.get] 方法内部才会初始化目标 Service
     */
    @AnyThread
    @NotAppUseAnno
    fun <T : Any> register(
        tClass: KClass<T>,
        name: String = DEFAULT_NAME,
        callable: Callable<out T>
    ) {
        synchronized(serviceMap) {
            var implServiceMap = serviceMap[tClass]
            if (implServiceMap == null) {
                implServiceMap = HashMap()
                serviceMap[tClass] = implServiceMap
            }
            if (implServiceMap.containsKey(name)) {
                throw RuntimeException(tClass.simpleName + " the key of '" + name + "' is exist")
            }
            implServiceMap.put(name, callable)
        }
    }

    @AnyThread
    @NotAppUseAnno
    fun <T : Any> unregister(tClass: KClass<T>) {
        unregister(tClass, DEFAULT_NAME)
    }

    @AnyThread
    @NotAppUseAnno
    fun <T : Any> unregister(tClass: KClass<T>, name: String) {
        Utils.checkNullPointer(tClass, "tClass")
        Utils.checkNullPointer(name, "name")
        synchronized(serviceMap) {
            val implServiceMap = serviceMap[tClass]
            if (implServiceMap != null) {
                val callable = implServiceMap.remove(name) ?: return
                if (callable is SingletonCallable<*>) {
                    if (callable.isInit) {
                        callable.destroy()
                    }
                }
            }
        }
    }

    /**
     * 装饰某一个 Service
     *
     * @param tClass   目标 Service class
     * @param target   目标对象
     * @return 返回一个增强的目标对象的装饰者
     */
    @AnyThread
    @NotAppUseAnno
    fun <T : Any> decorate(tClass: KClass<T>, target: T): T {
        Utils.checkNullPointer(tClass, "tClass")
        Utils.checkNullPointer(target, "target")
        var result = target
        synchronized(serviceDecoratorMap) {
            val map = serviceDecoratorMap[tClass]
            if (map != null) {
                val values: Collection<DecoratorCallable<*>> = map.values
                // 排序
                val list: List<DecoratorCallable<*>> = ArrayList(values)
                Collections.sort(list) { o1, o2 -> o1.priority() - o2.priority() }
                for (callable in list) {
                    val realCallable = callable as DecoratorCallable<T>
                    result = realCallable.get(result)
                }
            }
        }
        return result
    }

    /**
     * Service 的创建可能不是在主线程. 所以Service 初始化的时候请注意这一点.
     * 内部会保证创建的过程是线程安全的
     *
     * @param tClass 目标对象的 Class 对象
     * @param <T>    目标对象的实例对象
     * @return 目标对象的实例对象
    </T> */
    @AnyThread
    @JvmOverloads
    fun <T : Any> get(tClass: KClass<T>, name: String = DEFAULT_NAME): T? {
        Utils.checkNullPointer(tClass, "tClass")
        Utils.checkNullPointer(name, "name")
        synchronized(serviceMap) {
            val uniqueName = tClass.simpleName + ":" + name
            if (uniqueServiceSet.contains(uniqueName)) {
                throw ServiceRepeatCreateException("className is " + tClass.simpleName + ", serviceName is '" + name + "'")
            }
            uniqueServiceSet.add(uniqueName)
            try {
                var result: T? = null
                val implServiceMap = serviceMap[tClass]
                if (implServiceMap != null) {
                    val callable = implServiceMap[name]
                    if (callable != null) {
                        // 如果没创建, 这时候会创建了目标 service 对象
                        val t = Utils.checkNullPointer(callable.get()) as T
                        result = decorate(tClass, t)
                    }
                }
                uniqueServiceSet.remove(uniqueName)
                return result
            } catch (e: Exception) {
                uniqueServiceSet.remove(uniqueName)
                throw e
            }
        }
    }

    /**
     * @return 肯定不会为 null
     * @see .get
     */
    @AnyThread
    fun <T : Any> requiredGet(tClass: KClass<T>): T {
        return requiredGet(tClass = tClass, name = DEFAULT_NAME)
    }

    /**
     * @return 肯定不会为 null
     * @see .get
     */
    @AnyThread
    fun <T : Any> requiredGet(tClass: KClass<T>, name: String): T {
        return Utils.checkNullPointer(get(tClass = tClass, name))
    }

}

fun <T : Any> KClass<T>.service(name: String = ServiceManager.DEFAULT_NAME): T? {
    return ServiceManager.get(tClass = this, name = name)
}

fun <T : Any> KClass<T>.serviceRequired(name: String = ServiceManager.DEFAULT_NAME): T {
    return ServiceManager.requiredGet(tClass = this, name = name)
}