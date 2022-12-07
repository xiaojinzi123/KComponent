package com.xiaojinzi.component.anno

import com.xiaojinzi.component.anno.support.UiThreadCreateAnno
import kotlin.reflect.KClass

/**
 * 标记一个实现类, 是某一个 Service 接口的增强实现
 * 被标记的对象的创建由真正的实现类决定的.
 * 并且被标记的对象, 必须有增强目标 Service Api 的构造方法
 * 被标记的类不会被缓存. 因为真正被增强的目标才是真实的实现类.
 * 装饰类都是真正的实现类被使用了. 才会进行装饰
 */
@UiThreadCreateAnno
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.BINARY)
annotation class ServiceDecoratorAnno(

    /**
     * 这个服务对应的接口
     */
    val value: KClass<*>,

    /**
     * 优先级, 值越大优先级越高
     */
    val priority: Int = 0

)