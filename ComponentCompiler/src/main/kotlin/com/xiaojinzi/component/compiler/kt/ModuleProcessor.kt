package com.xiaojinzi.component.compiler.kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KSTypesNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.anno.ConditionalAnno
import com.xiaojinzi.component.anno.FragmentAnno
import com.xiaojinzi.component.anno.GlobalInterceptorAnno
import com.xiaojinzi.component.anno.InterceptorAnno
import com.xiaojinzi.component.anno.ModuleAppAnno
import com.xiaojinzi.component.anno.RouterAnno
import com.xiaojinzi.component.anno.RouterDegradeAnno
import com.xiaojinzi.component.anno.ServiceAnno
import com.xiaojinzi.component.anno.ServiceDecoratorAnno
import com.xiaojinzi.component.anno.support.ComponentGeneratedAnno
import com.xiaojinzi.component.anno.support.ModuleApplicationAnno
import com.xiaojinzi.component.compiler.kt.bean.RouterAnnoBean
import com.xiaojinzi.component.packageName
import com.xiaojinzi.component.simpleClassName
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

/**
 * - ModuleApplication
 * - Fragment
 * - Service
 */
class ModuleProcessor(
    override val environment: SymbolProcessorEnvironment,
) : BaseHostProcessor(
    environment = environment,
) {

    // 从系统变量中获取临时目录的
    private val tempCacheFolder = File(
        System.getProperty("java.io.tmpdir"),
        "kcomponentKspCacheFolder/${componentModuleName.replace(oldChar = '-', newChar = '_')}",
    )

    private val annoNames = listOf(
        ModuleAppAnno::class,
        ServiceAnno::class,
        ServiceDecoratorAnno::class,
        FragmentAnno::class,
        GlobalInterceptorAnno::class,
        InterceptorAnno::class,
        RouterAnno::class,
        RouterDegradeAnno::class,
    ).map { "@${it.simpleName}" }

    private val TAG = "ModuleProcessor"

    private val componentClassName = ClassName(
        packageName = ComponentConstants.COMPONENT_CLASS_NAME.packageName(),
        ComponentConstants.COMPONENT_CLASS_NAME.simpleClassName(),
    )

    private val moduleImplClassName = ClassName(
        packageName = ComponentConstants.MODULE_IMPL_CLASS_NAME.packageName(),
        ComponentConstants.MODULE_IMPL_CLASS_NAME.simpleClassName(),
    )

    private val iApplicationLifecycleClassName = ClassName(
        packageName = ComponentConstants.APPLICATION_LIFECYCLE_INTERFACE_CLASS_NAME.packageName(),
        ComponentConstants.APPLICATION_LIFECYCLE_INTERFACE_CLASS_NAME.simpleClassName(),
    )

    private val priority: Int = environment.options["Priority"]?.toIntOrNull() ?: 0

    private fun addConditionIfCodeToFunction(
        funSpecBuilder: FunSpec.Builder,
        condition: ConditionalAnno?,
        block: (funSpecBuilder: FunSpec.Builder) -> Unit,
    ) {

        val targetCondition = condition ?: return block(funSpecBuilder)
        val conditionClassPathList = targetCondition.conditionClassPathList

        if (conditionClassPathList.isNotEmpty()) {
            val stateCodeList = mutableListOf<String>()
            val args = mutableListOf<Any>()
            conditionClassPathList.forEach { conditionClassPath ->
                stateCodeList.add(
                    element = "%T.getByClass(%T::class).matches()"
                )
                args.add(
                    element = mClassNameConditionCache,
                )
                args.add(
                    element = conditionClassPath.toClassName(),
                )
            }
            funSpecBuilder.beginControlFlow(
                controlFlow = "if(${stateCodeList.joinToString(separator = " && ")})",
                *args.toTypedArray(),
            )
            block(funSpecBuilder)
            funSpecBuilder.endControlFlow()
        } else {
            block(funSpecBuilder)
        }
    }

    private fun initApplication(
        typeSpecBuilder: TypeSpec.Builder,
        moduleAppAnnotatedList: List<KSClassDeclaration>,
    ) {

        val tempStr = moduleAppAnnotatedList
            .joinToString { item ->
                "${item.qualifiedName!!.asString()}()"
            }

        typeSpecBuilder
            .addFunction(
                funSpec = FunSpec
                    .builder("initApplication")
                    .returns(
                        returnType = mClassNameListKt.parameterizedBy(
                            iApplicationLifecycleClassName,
                        )
                    )
                    .addModifiers(
                        KModifier.OVERRIDE,
                        KModifier.PUBLIC,
                    )
                    .addStatement(
                        format = "return listOf(\n$tempStr\n)"
                    )
                    .build()
            )
    }

    @OptIn(KspExperimental::class)
    private fun aboutService(
        typeSpecBuilder: TypeSpec.Builder,
        serviceAnnotatedList: List<KSAnnotated>,
        serviceDecoratorAnnotatedList: List<KSAnnotated>,
    ) {

        val serviceDecoratorAnnotatedListMap = serviceDecoratorAnnotatedList
            .filterIsInstance<KSClassDeclaration>()
            .associateWith {
                UUID.randomUUID().toString()
            }

        val classNameServiceManager: ClassName = ClassName(
            packageName = ComponentConstants.SERVICE_MANAGER_CLASS_NAME.packageName(),
            ComponentConstants.SERVICE_MANAGER_CLASS_NAME.simpleClassName(),
        )

        val classNameServiceDecoratorCallable: ClassName = ClassName(
            packageName = ComponentConstants.DECORATOR_CALLABLE_CLASS_NAME.packageName(),
            ComponentConstants.DECORATOR_CALLABLE_CLASS_NAME.simpleClassName(),
        )

        val counter = AtomicInteger(0)

        typeSpecBuilder
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "initSpi")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .addParameter(
                        parameterSpec = ParameterSpec
                            .builder(
                                name = "application",
                                type = mClassNameAndroidApplication,
                            )
                            .build()
                    )
                    .also { funSpec ->
                        serviceAnnotatedList
                            .forEach { item ->

                                funSpec.addComment("-------------- ${item.getDescName()} -------------- ")

                                val implName = "implName${counter.incrementAndGet()}"
                                val serviceAnno = item
                                    .getAnnotationsByType(annotationKClass = ServiceAnno::class)
                                    .first()

                                val stateCode = "val %N = %L"
                                val args = mutableListOf<Any>()

                                // 参数名
                                args.add(implName)

                                val targetClass: TypeName = when (item) {
                                    is KSClassDeclaration -> {
                                        item.toClassName()
                                    }

                                    is KSFunctionDeclaration -> {
                                        item.returnTypeToTypeName()!!
                                    }

                                    else -> notSupport()
                                }

                                val getImplCallback: (KSAnnotated, FunSpec.Builder) -> Unit =
                                    { item, funSpec ->
                                        when (item) {
                                            is KSClassDeclaration -> {
                                                val targetApplicationConstructor =
                                                    item.getConstructors()
                                                        .find {
                                                            it.parameters.size == 1 && it.parameters[0].typeToClassName() == mClassNameAndroidApplication
                                                        }
                                                if (targetApplicationConstructor == null
                                                ) {
                                                    funSpec.addStatement(
                                                        format = "return %T()",
                                                        targetClass,
                                                    )
                                                } else {
                                                    funSpec.addStatement(
                                                        format = "return %T(${targetApplicationConstructor.parameters[0].name!!.asString()} = application)",
                                                        targetClass,
                                                    )
                                                }
                                            }

                                            is KSFunctionDeclaration -> {
                                                if (item.parameters.size > 1) {
                                                    notSupport()
                                                }
                                                item.parameters.firstOrNull()?.let {
                                                    if (it.typeToClassName() != mClassNameAndroidApplication) {
                                                        notSupport()
                                                    }
                                                }
                                                if (item.parameters.size == 1) {
                                                    val applicationParameter = item.parameters[0]
                                                    funSpec.addStatement(
                                                        format = "return ${item.qualifiedName!!.asString()}(${applicationParameter.name!!.asString()} = application)",
                                                        targetClass,
                                                    )
                                                } else {
                                                    funSpec.addStatement(
                                                        format = "return ${item.qualifiedName!!.asString()}()",
                                                    )
                                                }
                                            }

                                            else -> notSupport()
                                        }
                                    }

                                // 如果是单利
                                if (serviceAnno.singleTon) {
                                    args.add(
                                        element = TypeSpec
                                            .anonymousClassBuilder()
                                            .superclass(
                                                superclass = mClassNameSupportSingletonCallable.parameterizedBy(
                                                    targetClass,
                                                )
                                            )
                                            .addProperty(
                                                propertySpec = PropertySpec
                                                    .builder(name = "raw", type = targetClass)
                                                    .addModifiers(
                                                        KModifier.OVERRIDE,
                                                    )
                                                    .getter(
                                                        getter = FunSpec
                                                            .getterBuilder()
                                                            .also { funcSpec_get ->
                                                                getImplCallback.invoke(
                                                                    item, funcSpec_get
                                                                )
                                                            }
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                } else // 占位
                                {
                                    args.add(
                                        element = TypeSpec
                                            .anonymousClassBuilder()
                                            .addSuperinterface(
                                                superinterface = mClassNameSupportCallable.parameterizedBy(
                                                    targetClass,
                                                )
                                            )
                                            .addFunction(
                                                funSpec = FunSpec
                                                    .builder("get")
                                                    .addModifiers(
                                                        KModifier.OVERRIDE,
                                                    )
                                                    .returns(
                                                        returnType = targetClass,
                                                    )
                                                    .also { funcSpec_get ->
                                                        getImplCallback.invoke(
                                                            item, funcSpec_get
                                                        )
                                                    }
                                                    .build()
                                            )
                                            .build()
                                    )
                                }

                                funSpec.addStatement(stateCode, *args.toTypedArray())
                                val serviceClassPathList = serviceAnno.serviceClassPathList
                                if (serviceClassPathList.isEmpty()) {
                                    throw ProcessException(
                                        message = "${item.getDescName()} 的 @ServiceAnno 注解, value 不可以为空"
                                    )
                                }
                                val nameList = serviceAnno.name
                                if (nameList.isNotEmpty() || serviceClassPathList.size > 1) {
                                    if (serviceClassPathList.size != nameList.size) {
                                        throw ProcessException(
                                            message = "${item.getDescName()} 的 @ServiceAnno 注解, name 属性可以为空数组, 如果不为空, name 属性和 value 属性的个数必须是相等的"
                                        )
                                    }
                                }

                                serviceClassPathList.forEachIndexed { index, interfaceClassPath ->
                                    val targetName = nameList.getOrNull(index)
                                    funSpec.addStatement(
                                        "%T.register(tClass = %T::class, name = ${if (targetName == null) "%T.DEFAULT_NAME" else "%S"}, callable = %L)",
                                        classNameServiceManager,
                                        interfaceClassPath.toClassName(),
                                        targetName ?: classNameServiceManager,
                                        implName,
                                    )
                                    if (serviceAnno.autoInit) {
                                        funSpec.addStatement(
                                            "%T.registerAutoInit(tClass = %T::class, name = ${if (targetName == null) "%T.DEFAULT_NAME" else "%S"})",
                                            classNameServiceManager,
                                            interfaceClassPath.toClassName(),
                                            targetName ?: classNameServiceManager,
                                        )
                                    }
                                }

                            }
                    }
                    // 处理服务发现装饰者的问题
                    .also { funSpec ->
                        serviceDecoratorAnnotatedListMap
                            .forEach { serviceDecoratorAnnotatedItem ->

                                val ksClassDeclaration = serviceDecoratorAnnotatedItem.key
                                val uuid = serviceDecoratorAnnotatedItem.value

                                val serviceDecoratorAnno = ksClassDeclaration
                                    .getAnnotationsByType(annotationKClass = ServiceDecoratorAnno::class)
                                    .first()

                                // 装饰的目标接口
                                val decorateTargetClassName =
                                    serviceDecoratorAnno.valueClassPath.toClassName()

                                val parameterName = ksClassDeclaration.getConstructors()
                                    .first().parameters.first().name!!.asString()

                                val condition = ksClassDeclaration.getAnnotationsByType(
                                    annotationKClass = ConditionalAnno::class,
                                ).firstOrNull()

                                addConditionIfCodeToFunction(
                                    funSpecBuilder = funSpec,
                                    condition = condition,
                                ) {

                                    val implName = "implName${counter.incrementAndGet()}"

                                    funSpec.addStatement(
                                        format = "val %N = %L",
                                        implName,
                                        TypeSpec
                                            .anonymousClassBuilder()
                                            .addSuperinterface(
                                                superinterface = classNameServiceDecoratorCallable.parameterizedBy(
                                                    decorateTargetClassName,
                                                )
                                            )
                                            .addFunction(
                                                funSpec = FunSpec
                                                    .builder("get")
                                                    .addModifiers(KModifier.OVERRIDE)
                                                    .addParameter(
                                                        name = "target",
                                                        type = decorateTargetClassName,
                                                    )
                                                    .returns(
                                                        returnType = decorateTargetClassName,
                                                    )
                                                    .addStatement(
                                                        format = "return %T($parameterName = target)",
                                                        ksClassDeclaration.toClassName(),
                                                    )
                                                    .build()
                                            )
                                            .addFunction(
                                                funSpec = FunSpec
                                                    .builder("priority")
                                                    .addModifiers(KModifier.OVERRIDE)
                                                    .returns(
                                                        returnType = Int::class,
                                                    )
                                                    .addStatement(
                                                        format = "return ${serviceDecoratorAnno.priority}"
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )

                                    funSpec.addStatement(
                                        format = "%T.registerDecorator(tClass = %T::class, uid = %S, %N)",
                                        classNameServiceManager,
                                        decorateTargetClassName,
                                        uuid,
                                        implName,
                                    )

                                }

                            }
                    }
                    .build()
            )
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "destroySpi")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .also { funSpec ->
                        serviceAnnotatedList.forEach { item ->
                            val serviceAnno = item
                                .getAnnotationsByType(annotationKClass = ServiceAnno::class)
                                .first()
                            val serviceClassPathList = serviceAnno.serviceClassPathList
                            serviceClassPathList.forEachIndexed { index, interfaceClassPath ->
                                val serviceName = serviceAnno.name.getOrNull(index)
                                if (serviceName == null) {
                                    funSpec.addStatement(
                                        format = "%T.unregister(tClass = %T::class, name = %T.DEFAULT_NAME)",
                                        classNameServiceManager,
                                        interfaceClassPath.toClassName(),
                                        classNameServiceManager,
                                    )
                                } else {
                                    funSpec.addStatement(
                                        format = "%T.unregister(tClass = %T::class, name = %S)",
                                        classNameServiceManager,
                                        interfaceClassPath.toClassName(),
                                        serviceName,
                                    )
                                }
                                if (serviceAnno.autoInit) {
                                    if (serviceName == null) {
                                        funSpec.addStatement(
                                            format = "%T.registerAutoInit(tClass = %T::class)",
                                            classNameServiceManager,
                                            interfaceClassPath.toClassName(),
                                        )
                                    } else {
                                        funSpec.addStatement(
                                            format = "%T.registerAutoInit(tClass = %T::class, name = %S)",
                                            classNameServiceManager,
                                            interfaceClassPath.toClassName(),
                                            serviceName,
                                        )
                                    }
                                }
                            }
                        }

                    }
                    // 处理服务发现装饰者的问题
                    .also { funSpec ->
                        serviceDecoratorAnnotatedListMap
                            .forEach { serviceDecoratorAnnotatedItem ->
                                val uuid = serviceDecoratorAnnotatedItem.value
                                val ksClassDeclaration = serviceDecoratorAnnotatedItem.key
                                val serviceDecoratorAnno = ksClassDeclaration
                                    .getAnnotationsByType(annotationKClass = ServiceDecoratorAnno::class)
                                    .first()
                                // 装饰的目标接口
                                val decorateTargetClassName =
                                    serviceDecoratorAnno.valueClassPath.toClassName()
                                funSpec.addStatement(
                                    format = "%T.unregisterDecorator(tClass = %T::class, uid = %S)",
                                    classNameServiceManager,
                                    decorateTargetClassName,
                                    uuid,
                                )
                            }
                    }
                    .build()
            )
    }

    @OptIn(KspExperimental::class)
    private fun aboutFragment(
        typeSpecBuilder: TypeSpec.Builder,
        fragmentAnnotatedList: List<KSAnnotated>,
    ) {
        typeSpecBuilder
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "initFragment")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .also { funSpec ->
                        val counter = AtomicInteger()
                        TypeName
                        fragmentAnnotatedList.forEach { item ->
                            // 目标 Fragment 的全路径
                            val targetClassNameStr: String = when (item) {
                                is KSFunctionDeclaration -> {
                                    item.returnType
                                        ?.resolve()
                                        ?.declaration
                                        ?.qualifiedName
                                        ?.asString()
                                        ?: ""
                                }

                                is KSClassDeclaration -> {
                                    item.qualifiedName?.asString() ?: ""
                                }

                                else -> throw RuntimeException("Unsupported type")
                            }
                            val fragmentAnno: FragmentAnno = when (item) {
                                is KSFunctionDeclaration, is KSClassDeclaration -> {
                                    item.getAnnotationsByType(
                                        annotationKClass = FragmentAnno::class,
                                    ).first()
                                }

                                else -> throw RuntimeException("Unsupported type")
                            }
                            if (fragmentAnno.value.isEmpty()) {
                                throw ProcessException(message = "FragmentAnno.value can't be empty: ${item.getDescName()} ")
                            }
                            val targetClassName = ClassName(
                                packageName = targetClassNameStr.packageName(),
                                targetClassNameStr.simpleClassName(),
                            )
                            val implName = "implName${counter.getAndIncrement()}"
                            funSpec.addStatement(
                                format = "val %N = %L",
                                implName,
                                TypeSpec
                                    .anonymousClassBuilder()
                                    .addSuperinterface(
                                        superinterface = mClassNameFunction1.parameterizedBy(
                                            mClassNameAndroidBundle.copy(
                                                nullable = true,
                                            ),
                                            targetClassName,
                                        )
                                    )
                                    .addFunction(
                                        funSpec = FunSpec
                                            .builder(name = "apply")
                                            .addModifiers(
                                                KModifier.OVERRIDE,
                                            )
                                            .addParameter(
                                                parameterSpec = ParameterSpec
                                                    .builder(
                                                        name = "t",
                                                        type = mClassNameAndroidBundle.copy(
                                                            nullable = true,
                                                        ),
                                                    )
                                                    .build()
                                            )
                                            .returns(
                                                returnType = targetClassName,
                                            )
                                            .addStatement(
                                                format = "val targetBundle = t?: Bundle()",
                                            )
                                            .also {
                                                when (item) {
                                                    is KSFunctionDeclaration -> {
                                                        if (logEnable) {
                                                            logger.warn(
                                                                message = "fragment KSFunctionDeclaration = ${item.qualifiedName?.asString()}"
                                                            )
                                                        }
                                                        if (item.parameters.size != 1) {
                                                            throw RuntimeException(
                                                                "FragmentAnno 注解的方法必须只有一个参数, ${item.qualifiedName}"
                                                            )
                                                        }
                                                        it.addStatement(
                                                            format = "val fragment = ${item.qualifiedName?.asString()}(${item.parameters.first().name?.asString()} = targetBundle)",
                                                            targetClassName,
                                                        )
                                                    }

                                                    is KSClassDeclaration -> {
                                                        it.addStatement(
                                                            format = "val fragment = %T()",
                                                            targetClassName,
                                                        )
                                                        it.addStatement(
                                                            format = "fragment.arguments = targetBundle"
                                                        )
                                                    }

                                                    else -> throw RuntimeException("Unsupported type")
                                                }
                                            }
                                            .addStatement(
                                                format = "return fragment"
                                            )
                                            .build()
                                    )
                                    .build()
                            )

                            fragmentAnno.value.forEach { fragmentName ->
                                funSpec.addStatement(
                                    format = "%T.register(flag = %S, function = %N)",
                                    mClassNameFragmentManager,
                                    fragmentName,
                                    implName,
                                )
                            }

                        }
                    }
                    .build()
            )
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "destroyFragment")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .also { funSpec ->
                        fragmentAnnotatedList.forEach { item ->
                            val fragmentAnno: FragmentAnno = when (item) {
                                is KSFunctionDeclaration, is KSClassDeclaration -> {
                                    item.getAnnotationsByType(
                                        annotationKClass = FragmentAnno::class,
                                    ).first()
                                }

                                else -> throw RuntimeException("Unsupported type")
                            }
                            fragmentAnno.value.forEach { fragmentName ->
                                funSpec.addStatement(
                                    format = "%T.unregister(flag = %S)",
                                    mClassNameFragmentManager,
                                    fragmentName,
                                )
                            }
                        }
                    }
                    .build()
            )
    }


    @OptIn(KspExperimental::class)
    private fun aboutInterceptor(
        typeSpecBuilder: TypeSpec.Builder,
        globalInterceptorAnnotatedList: List<KSClassDeclaration>,
        interceptorAnnotatedList: List<KSClassDeclaration>,
    ) {

        val interceptorBeanClassName = ClassName(
            packageName = ComponentConstants.INTERCEPTOR_BEAN_CLASS_NAME.packageName(),
            ComponentConstants.INTERCEPTOR_BEAN_CLASS_NAME.simpleClassName(),
        )

        val interceptorClassName = ClassName(
            packageName = ComponentConstants.INTERCEPTOR_INTERFACE_CLASS_NAME.packageName(),
            ComponentConstants.INTERCEPTOR_INTERFACE_CLASS_NAME.simpleClassName(),
        )

        val globalInterceptorListStr = globalInterceptorAnnotatedList
            .joinToString { item ->
                val anno =
                    item.getAnnotationsByType(annotationKClass = GlobalInterceptorAnno::class)
                        .first()
                "%T(interceptor = ${item.qualifiedName!!.asString()}::class," + "priority = ${anno.priority})"
            }

        val globalInterceptorArgList = globalInterceptorAnnotatedList
            .map {
                interceptorBeanClassName
            }.toTypedArray()

        val interceptorListStr = interceptorAnnotatedList
            .joinToString { item ->
                val anno =
                    item.getAnnotationsByType(annotationKClass = InterceptorAnno::class).first()
                "\"${anno.value}\" to %L::class"
            }

        val interceptorArgList = interceptorAnnotatedList.map { item ->
            item.qualifiedName!!.asString()
        }.toTypedArray()

        typeSpecBuilder
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "initGlobalInterceptor")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .returns(
                        returnType = mClassNameListKt.parameterizedBy(
                            interceptorBeanClassName,
                        )
                    )
                    .addStatement(
                        format = "return listOf(\n $globalInterceptorListStr \n )",
                        *globalInterceptorArgList,
                    )
                    /*.addStatement(
                        format = "return listOf(\n\n)",
                    )*/
                    .build()
            )
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "initInterceptor")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .returns(
                        returnType = mClassNameMapKt.parameterizedBy(
                            String::class.asTypeName(),
                            KClass::class.asTypeName().parameterizedBy(
                                WildcardTypeName.producerOf(outType = interceptorClassName)
                            ),
                        )
                    )
                    .addStatement(
                        format = "return mapOf(\n$interceptorListStr\n)",
                        *interceptorArgList,
                    )
                    .build()
            )
    }

    private fun toRouterAnnoBean(element: KSAnnotated, routerAnno: RouterAnno): RouterAnnoBean {

        // 如果有host那就必须满足规范
        if (routerAnno.host.isNotEmpty() && routerAnno.host.contains("/")) {
            throw ProcessException(element.toString() + "the host path '" + routerAnno.host + "' can't contains '/'")
        }

        var host = routerAnno.host
        var path = routerAnno.path
        val hostAndPath = routerAnno.hostAndPath
        if ("" != hostAndPath) { // 如果用户填写了 hostAndPath 就拆分出 host 和 path 覆盖之前的
            val index = hostAndPath.indexOf('/')
            if (index < 0) {
                throw ProcessException("the hostAndPath($hostAndPath) must have '/',such as \"app/test\"")
            }
            if (index == 0 || index == hostAndPath.length - 1) {
                throw ProcessException("the hostAndPath($hostAndPath) can't start with '/' and end with '/'")
            }
            host = hostAndPath.substring(0, index)
            path = hostAndPath.substring(index + 1)
        }
        // 如果用户 host 没填
        if (host.isEmpty()) {
            host = componentModuleName
        }
        // 如果 path 没有 / 开头,会自动加一个
        if (path.isNotEmpty() && path[0] != '/') {
            path = ComponentConstants.SEPARATOR + path
        }

        if (logEnable) {
            logger.warn("routerAnno.hostAndPath = ${element.location}")
        }
        routerAnno.interceptorNames.forEach { item ->
            if (logEnable) {
                logger.warn("routerAnno.interceptorName = $item")
            }
        }

        return RouterAnnoBean(
            regex = routerAnno.regex,
            scheme = routerAnno.scheme,
            host = host,
            // 一定 '/' 开头的
            path = path,
            desc = routerAnno.desc,
            rawType = element,
            // 拦截器的顺序
            interceptorPriorities = routerAnno.interceptorPriorities.toList(),
            interceptorNamePriorities = routerAnno.interceptorNamePriorities.toList(),
            // class 拦截器
            interceptors = routerAnno.interceptorsClassPathList,
            interceptorNames = routerAnno.interceptorNames.toList(),
        )

    }

    @OptIn(KspExperimental::class)
    private fun aboutRouter(
        typeSpecBuilder: TypeSpec.Builder,
        // 可能是标记在静态方法上或者类上的
        routerAnnotatedList: List<KSAnnotated>,
    ) {

        val targetAnnotatedList = routerAnnotatedList
            .map { item ->
                item to item.getAnnotationsByType(
                    annotationKClass = RouterAnno::class,
                )
            }.flatMap { item1 ->
                item1.second.map { item2 ->
                    toRouterAnnoBean(
                        element = item1.first,
                        routerAnno = item2,
                    ) to item1.first
                }
            }

        val routerBeanClassName = ClassName(
            packageName = ComponentConstants.ROUTER_BEAN_CLASS_NAME.packageName(),
            ComponentConstants.ROUTER_BEAN_CLASS_NAME.simpleClassName(),
        )

        val customerIntentCallClassName = ClassName(
            packageName = ComponentConstants.CUSTOMER_INTENT_CALL_CLASS_NAME.packageName(),
            ComponentConstants.CUSTOMER_INTENT_CALL_CLASS_NAME.simpleClassName(),
        )

        val pageInterceptorClassName = ClassName(
            packageName = ComponentConstants.PAGEINTERCEPTOR_BEAN_CLASS_NAME.packageName(),
            ComponentConstants.PAGEINTERCEPTOR_BEAN_CLASS_NAME.simpleClassName(),
        )

        val routerStr = targetAnnotatedList
            .joinToString { item ->
                val routeAnnoBean = item.first
                StringBuffer()
                    .append("%T(")
                    .append("\nregex = %S,")
                    .apply {
                        if (routeAnnoBean.scheme.isNullOrEmpty()) {
                            this.append("\nuri = defaultScheme + %S,")
                        } else {
                            this.append("\nuri = %S,")
                        }
                    }
                    .append("\ndesc = %S,")
                    .apply {
                        this.append("\npageInterceptors = listOf(")
                        routeAnnoBean.interceptors.forEach {
                            this.append("\n%T(priority = %L, interceptorClass = %T::class,),")
                        }
                        routeAnnoBean.interceptorNames.forEach {
                            this.append("\n%T(priority = %L, interceptorName = %S,),")
                        }
                        this.append("),")
                    }
                    .apply {
                        when (item.second) {
                            is KSClassDeclaration -> {
                                this.append("\ntargetClass = %L::class,")
                            }

                            is KSFunctionDeclaration -> {
                                this.append("\ncustomerIntentCall = object : %T {")
                                this.append("\n\toverride fun get(request: RouterRequest): %T {")
                                this.append("\n\t\t\treturn %L(\n\t\t\t\t%N = request\n\t\t\t)")
                                this.append("\n\t}")
                                this.append("\n}")
                            }

                            else -> throw ProcessException(
                                message = "not support"
                            )
                        }
                    }
                    .append("\n)")
                    .toString()
            }

        val routerArgList = targetAnnotatedList
            .map { item ->
                val routeAnnoBean = item.first
                listOfNotNull(
                    routerBeanClassName,
                    item.first.regex,
                    if (item.first.scheme.isNullOrEmpty()) {
                        "://${item.first.hostAndPath()}"
                    } else {
                        "${item.first.scheme}://${item.first.hostAndPath()}"
                    },
                    item.first.desc ?: "",
                ) + routeAnnoBean.interceptors
                    .mapIndexed { index, interceptorClassPathStr ->
                        interceptorClassPathStr to
                                (routeAnnoBean.interceptorPriorities.getOrNull(
                                    index
                                ) ?: 0)
                    }
                    .map {
                        listOf(
                            pageInterceptorClassName,
                            it.second,
                            it.first.toClassName(),
                        )
                    }.flatten() + routeAnnoBean.interceptorNames
                    .mapIndexed { index, interceptorName ->
                        interceptorName to
                                (routeAnnoBean.interceptorNamePriorities.getOrNull(
                                    index
                                ) ?: 0)
                    }
                    .map {
                        listOf(
                            pageInterceptorClassName,
                            it.second,
                            it.first,
                        )
                    }.flatten() + when (val element = item.second) {
                    is KSClassDeclaration -> {
                        listOf(element.qualifiedName!!.asString())
                    }

                    is KSFunctionDeclaration -> {
                        if (logEnable) {
                            logger.warn("element.qualifiedName = ${element.qualifiedName?.asString()}")
                        }
                        listOf(
                            customerIntentCallClassName,
                            mClassNameIntent,
                            element.qualifiedName!!.asString(),
                            element.parameters.first().name!!.asString(),
                        )
                    }

                    else -> throw ProcessException(
                        message = "not support"
                    )
                }

            }
            .flatten()
            .toTypedArray()

        typeSpecBuilder
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "initRegExRouterMap")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .returns(
                        returnType = mClassNameMapKt.parameterizedBy(
                            String::class.asTypeName(),
                            routerBeanClassName
                        )
                    )
                    .addStatement(
                        format = "return mapOf()"
                    )
                    .build()
            )
            .addFunction(
                funSpec = FunSpec
                    .builder(name = "initRouterList")
                    .addModifiers(
                        KModifier.OVERRIDE,
                    )
                    .returns(
                        returnType = mClassNameListKt.parameterizedBy(
                            routerBeanClassName
                        )
                    )
                    .addStatement(
                        format = "val defaultScheme = %T.requiredConfig().defaultScheme",
                        componentClassName,
                    )
                    .addStatement(
                        format = "return listOf(\n$routerStr\n)",
                        *routerArgList,
                    )
                    /*.addStatement(
                        format = "return mapOf()"
                    )*/
                    .build()
            )

    }

    @OptIn(KspExperimental::class)
    private fun aboutRouterDegrade(
        typeSpecBuilder: TypeSpec.Builder,
        // 标记类上的
        routerDegradeAnnotatedList: List<KSAnnotated>,
    ) {

        val classNameRouterDegradeBean =
            ComponentConstants.ROUTER_DEGRADE_BEAN_CLASS_NAME.toClassName()

        typeSpecBuilder
            .addFunction(
                funSpec = FunSpec
                    .builder("initRouterDegrade")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(
                        returnType = List::class.asClassName().parameterizedBy(
                            classNameRouterDegradeBean,
                        )
                    )
                    .also { funSpec ->

                        val codeList = mutableListOf<String>()

                        val args = mutableListOf<Any>()

                        routerDegradeAnnotatedList
                            .filterIsInstance<KSClassDeclaration>()
                            .forEach { ksClassDeclaration ->

                                val routerDegradeAnno = ksClassDeclaration.getAnnotationsByType(
                                    annotationKClass = RouterDegradeAnno::class
                                ).first()

                                codeList.add(
                                    element = "%T(priority = ${routerDegradeAnno.priority}, targetClass = %T::class)",
                                )

                                args.add(
                                    element = classNameRouterDegradeBean,
                                )

                                args.add(
                                    element = ksClassDeclaration.toClassName(),
                                )

                            }

                        funSpec.addStatement(
                            format = "return listOf(${codeList.joinToString(separator = ", ")})",
                            *args.toTypedArray(),
                        )

                    }
                    .build()
            )

    }

    private val moduleAppAnnotatedList: MutableList<KSClassDeclaration> = mutableListOf()
    private val serviceAnnotatedList: MutableList<KSAnnotated> = mutableListOf()
    private val serviceDecoratorAnnotatedList: MutableList<KSAnnotated> = mutableListOf()
    private val fragmentAnnotatedList: MutableList<KSAnnotated> = mutableListOf()
    private val globalInterceptorAnnotatedList: MutableList<KSClassDeclaration> = mutableListOf()
    private val interceptorAnnotatedList: MutableList<KSClassDeclaration> = mutableListOf()
    private val routerAnnotatedList: MutableList<KSAnnotated> = mutableListOf()
    private val routerDegradeAnnotatedList: MutableList<KSAnnotated> = mutableListOf()

    override fun initProcess(resolver: Resolver) {
        super.initProcess(resolver)
        moduleAppAnnotatedList.clear()
        serviceAnnotatedList.clear()
        serviceDecoratorAnnotatedList.clear()
        fragmentAnnotatedList.clear()
        globalInterceptorAnnotatedList.clear()
        interceptorAnnotatedList.clear()
        routerAnnotatedList.clear()
        routerDegradeAnnotatedList.clear()
    }

    override fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated> {

        // 模块 Application 的
        moduleAppAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = ModuleAppAnno::class.qualifiedName!!
                )
                .onEach {
                    if (logEnable) {
                        logger.warn(
                            "$TAG $componentModuleName moduleAppAnnotatedList item = $it"
                        )
                    }
                }
                .mapNotNull { it as? KSClassDeclaration }
                .filterNot { it.qualifiedName == null }
                .toList()
        )

        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName moduleAppAnnotatedList = $moduleAppAnnotatedList"
            )
        }

        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName moduleAppAnnotatedList.size = ${moduleAppAnnotatedList.size}"
            )
        }

        // Service 的
        serviceAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = ServiceAnno::class.qualifiedName!!
                )
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName serviceAnnotatedList.size = ${serviceAnnotatedList.size}"
            )
        }

        // ServiceDecorator 的
        serviceDecoratorAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = ServiceDecoratorAnno::class.qualifiedName!!
                )
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName serviceDecoratorAnnotatedList.size = ${serviceDecoratorAnnotatedList.size}"
            )
        }

        // Fragment 的
        fragmentAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = FragmentAnno::class.qualifiedName!!
                )
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName fragmentAnnotatedList.size = ${fragmentAnnotatedList.size}"
            )
        }

        // 全局拦截器的
        globalInterceptorAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = GlobalInterceptorAnno::class.qualifiedName!!
                )
                .mapNotNull { it as? KSClassDeclaration }
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName globalInterceptorAnnotatedList.size = ${globalInterceptorAnnotatedList.size}"
            )
        }

        // 拦截器
        interceptorAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = InterceptorAnno::class.qualifiedName!!
                )
                .mapNotNull { it as? KSClassDeclaration }
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName interceptorAnnotatedList.size = ${interceptorAnnotatedList.size}"
            )
        }

        // 路由的
        routerAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = RouterAnno::class.qualifiedName!!
                )
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName routerAnnotatedList.size = ${routerAnnotatedList.size}"
            )
        }

        // 路由降级的
        routerDegradeAnnotatedList.addAll(
            elements = resolver
                .getSymbolsWithAnnotation(
                    annotationName = RouterDegradeAnno::class.qualifiedName!!
                )
                .toList(),
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName routerDegradeAnnotatedList.size = ${routerDegradeAnnotatedList.size}"
            )
        }

        generateFile()

        return emptyList()

    }

    private fun generateFile() {
        val isAllEmpty = moduleAppAnnotatedList.isEmpty()
                && serviceAnnotatedList.isEmpty()
                && serviceDecoratorAnnotatedList.isEmpty()
                && fragmentAnnotatedList.isEmpty()
                && globalInterceptorAnnotatedList.isEmpty()
                && interceptorAnnotatedList.isEmpty()
                && routerAnnotatedList.isEmpty()
                && routerDegradeAnnotatedList.isEmpty()

        val packageNameStr = "com.xiaojinzi.component.impl"
        val classNameStr = ComponentUtil.transformHostForClass(
            componentModuleName
        ) + ComponentUtil.MODULE

        val typeSpec = TypeSpec
            .classBuilder(
                name = classNameStr
            )
            .addModifiers(KModifier.FINAL)
            .superclass(superclass = moduleImplClassName)
            .addAnnotation(annotation = mClassNameAndroidKeepAnno)
            .addAnnotation(annotation = ModuleApplicationAnno::class)
            .addAnnotation(annotation = ComponentGeneratedAnno::class)
            .addProperty(
                propertySpec = PropertySpec
                    .builder(
                        name = "moduleName",
                        type = String::class,
                        KModifier.PUBLIC,
                        KModifier.OVERRIDE,
                    )
                    .initializer(
                        codeBlock = CodeBlock.of("%S", componentModuleName)
                    )
                    .build()
            )
            .addProperty(
                propertySpec = PropertySpec
                    .builder(
                        name = "priority",
                        type = Int::class,
                        KModifier.PUBLIC,
                        KModifier.OVERRIDE,
                    )
                    .initializer(
                        codeBlock = CodeBlock.of("%L", priority)
                    )
                    .build()
            )
            .apply {
                initApplication(
                    typeSpecBuilder = this,
                    moduleAppAnnotatedList = moduleAppAnnotatedList,
                )
                aboutService(
                    typeSpecBuilder = this,
                    serviceAnnotatedList = serviceAnnotatedList,
                    serviceDecoratorAnnotatedList = serviceDecoratorAnnotatedList,
                )
                aboutFragment(
                    typeSpecBuilder = this,
                    fragmentAnnotatedList = fragmentAnnotatedList,
                )
                aboutInterceptor(
                    typeSpecBuilder = this,
                    globalInterceptorAnnotatedList = globalInterceptorAnnotatedList,
                    interceptorAnnotatedList = interceptorAnnotatedList,
                )
                aboutRouter(
                    typeSpecBuilder = this,
                    routerAnnotatedList = routerAnnotatedList,
                )
                aboutRouterDegrade(
                    typeSpecBuilder = this,
                    routerDegradeAnnotatedList = routerDegradeAnnotatedList,
                )
            }
            .build()

        val fileSpec = FileSpec
            .builder(
                packageName = packageNameStr,
                fileName = classNameStr,
            )
            .addType(typeSpec = typeSpec)
            .build()

        try {
            codeGenerator.generatedFile.apply {
                if (logEnable) {
                    logger.warn("$TAG $componentModuleName 一共有 ${this.size} 个生成的文件")
                }
            }.forEachIndexed { index, file ->
                if (logEnable) {
                    logger.warn("$TAG $componentModuleName 第${index + 1}个文件：${file.path}")
                }
            }
            val sources = (moduleAppAnnotatedList + serviceAnnotatedList +
                    serviceDecoratorAnnotatedList + fragmentAnnotatedList +
                    globalInterceptorAnnotatedList + interceptorAnnotatedList +
                    routerAnnotatedList + routerDegradeAnnotatedList
                    )
                .mapNotNull { it.containingFile }
                .toTypedArray()

            val targetFileInCache =
                kspOptimizeUniqueName?.run {
                    File(
                        File(tempCacheFolder, this),
                        "${fileSpec.packageName}.${fileSpec.name}.kt",
                    )
                }
            targetFileInCache?.parentFile?.mkdirs()

            if (logEnable) {
                logger.warn("$TAG $componentModuleName kspOptimize = $kspOptimize")
                logger.warn("$TAG $componentModuleName isAllEmpty = $isAllEmpty")
                logger.warn(
                    "$TAG $componentModuleName " +
                            "targetFileInCache.exist = ${targetFileInCache?.exists() == true}, " +
                            "targetFileInCache.path = ${targetFileInCache?.path}"
                )
            }
            codeGenerator.createNewFile(
                // dependencies = Dependencies.ALL_FILES,
                dependencies = if (sources.isEmpty()) {
                    Dependencies.ALL_FILES
                } else {
                    Dependencies(
                        aggregating = true,
                        sources = sources,
                    )
                },
                packageName = fileSpec.packageName,
                fileName = fileSpec.name,
            ).use { outputStream ->
                if (kspOptimize && isAllEmpty) {
                    targetFileInCache?.let { targetFileInCache1 ->
                        if (targetFileInCache1.exists() && targetFileInCache1.isFile) {
                            if (logEnable) {
                                logger.warn("$TAG $componentModuleName ksp 出现 bug之后的弥补手段生效!, targetFileInCache= ${targetFileInCache1.path}")
                            }
                            targetFileInCache1.inputStream().use {
                                it.copyTo(out = outputStream)
                            }
                        } else {
                            throw YOU_SHOULD_RERUN_EXCEPTION
                        }
                    } ?: throw YOU_SHOULD_CONFIG_KSP_OPTIMIZE_UNIQUE_NAME_EXCEPTION
                } else {
                    outputStream.write(
                        fileSpec.toString().toByteArray()
                    )
                    // 保存到缓存文件夹中
                    runCatching {
                        // targetFileInCache.delete()
                        targetFileInCache?.outputStream()?.use {
                            it.write(
                                fileSpec.toString().toByteArray()
                            )
                            it.flush()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (logEnable) {
                logger.warn("$TAG $componentModuleName 生成文件异常啦~~~")
                logger.exception(e)
            }
        }
    }

    override fun finish() {
        super.finish()
        // generateFile()
        if (logEnable) {
            logger.warn("$TAG $componentModuleName finish")
        }
    }

    override fun onError() {
        super.onError()
        if (logEnable) {
            logger.warn("$TAG $componentModuleName onError")
        }
    }

}

@AutoService(SymbolProcessorProvider::class)
class ModuleProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModuleProcessor(
            environment = environment,
        )
    }

}

@OptIn(KspExperimental::class)
val RouterAnno.interceptorsClassPathList: List<String>
    get() {
        return try {
            this.interceptors.forEach { item ->
                // 走不到这里的
                println("item = $item")
            }
            emptyList()
        } catch (e: KSTypesNotPresentException) {
            e.ksTypes.map { it.declaration.qualifiedName!!.asString() }
        } catch (e: Exception) {
            emptyList()
        }
    }

@OptIn(KspExperimental::class)
val ServiceAnno.serviceClassPathList: List<String>
    get() {
        return try {
            this.value.forEach { item ->
                // 走不到这里的
                println("item = $item")
            }
            emptyList()
        } catch (e: KSTypesNotPresentException) {
            e.ksTypes.map { it.declaration.qualifiedName!!.asString() }
        } catch (e: Exception) {
            emptyList()
        }
    }

@OptIn(KspExperimental::class)
val ConditionalAnno.conditionClassPathList: List<String>
    get() {
        return try {
            this.conditions.forEach { item ->
                // 走不到这里的
                println("item = $item")
            }
            emptyList()
        } catch (e: KSTypesNotPresentException) {
            e.ksTypes.map { it.declaration.qualifiedName!!.asString() }
        } catch (e: Exception) {
            emptyList()
        }
    }

@OptIn(KspExperimental::class)
val ServiceDecoratorAnno.valueClassPath: String
    get() {
        return try {
            this.value
            // 走不到这里的
            ""
        } catch (e: KSTypeNotPresentException) {
            e.ksType.declaration.qualifiedName!!.asString()
        } catch (e: Exception) {
            ""
        }
    }