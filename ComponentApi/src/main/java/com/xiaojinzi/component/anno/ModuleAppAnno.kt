package com.xiaojinzi.component.anno

/**
 * 这是一个标识模块的生命周期类的注解,因为用注解标识的类肯定在某一个模块,所以标识了就知道那个类是哪个模块下面的
 * 这个注解没法加优先级, 因为加载的顺序是用户自己决定的
 *
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.BINARY)
annotation class ModuleAppAnno 