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
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
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

private data class ConditionalAnnoInfo(
    val conditionClassPathList: List<String>,
)

private data class ApplicationInfo(
    val containingFile: KSFile?,
    val qualifiedNameOfClass: String,
)

private sealed class ServiceInfo(
    open val containingFile: KSFile?,
    open val descName: String,
    open val serviceAnnoInfo: ServiceAnnoInfo,
    // 被 ServiceAnno 标记的类的 class 类型 或者 方法返回值的 class 类型
    open val classTypeName: TypeName,
) //
{

    data class ServiceAnnoInfo(
        val serviceClassPathList: List<String>,
        val name: List<String>,
        val singleTon: Boolean,
        val autoInit: Boolean,
    )

    data class ServiceClass(
        override val containingFile: KSFile?,
        override val descName: String,
        override val serviceAnnoInfo: ServiceAnnoInfo,
        override val classTypeName: TypeName,
        val applicationParameterName: String?,
    ) : ServiceInfo(
        containingFile = containingFile,
        descName = descName,
        serviceAnnoInfo = serviceAnnoInfo,
        classTypeName = classTypeName,
    )

    data class ServiceMethod(
        override val containingFile: KSFile?,
        override val descName: String,
        override val serviceAnnoInfo: ServiceAnnoInfo,
        override val classTypeName: TypeName,
        val applicationParameterName: String?,
        // @ServiceAnno 标记的方法的 com.xxx.xxx.testName
        val qualifiedName: String,
    ) : ServiceInfo(
        containingFile = containingFile,
        descName = descName,
        serviceAnnoInfo = serviceAnnoInfo,
        classTypeName = classTypeName,
    )

}

private data class ServiceDecoratorInfo(
    val uuid: String,
    val containingFile: KSFile?,
    val descName: String,
    val classClassName: ClassName,
    val serviceDecoratorAnnoInfo: ServiceDecoratorAnnoInfo,
    val conditionalAnnoInfo: ConditionalAnnoInfo?,
    // 装饰的目标接口
    val decorateTargetClassName: ClassName,
    // 被 @ServiceDecoratorAnno 标记的类的 class 类型的构造函数的参数名.
    // 被标记的只有一个构造函数, 并且参数只有一个
    val constructorParameterName: String,
) {

    data class ServiceDecoratorAnnoInfo(
        val priority: Int,
        val valueClassPath: String,
    )

    override fun toString(): String {
        return "ServiceDecoratorInfo(uuid='$uuid', descName='$descName', classClassName=$classClassName, constructorParameterName='$constructorParameterName')"
    }

}

private sealed class FragmentInfo(
    open val containingFile: KSFile?,
    open val descName: String,
    // 目标 Fragment 的全路径
    open val targetClassNameStr: String,
    open val fragmentAnnoInfo: FragmentAnnoInfo,
) //
{

    data class FragmentAnnoInfo(
        val value: List<String>,
    )

    data class ServiceClass(
        override val containingFile: KSFile?,
        override val descName: String,
        override val targetClassNameStr: String,
        override val fragmentAnnoInfo: FragmentAnnoInfo,
    ) : FragmentInfo(
        containingFile = containingFile,
        descName = descName,
        targetClassNameStr = targetClassNameStr,
        fragmentAnnoInfo = fragmentAnnoInfo,
    )

    data class ServiceMethod(
        override val containingFile: KSFile?,
        override val descName: String,
        override val targetClassNameStr: String,
        override val fragmentAnnoInfo: FragmentAnnoInfo,
        // com.xxx.xxx.testName
        val methodQualifiedName: String,
        // 只能有一个参数类型, xxx: Bundle
        val parameterName: String?,
    ) : FragmentInfo(
        containingFile = containingFile,
        descName = descName,
        targetClassNameStr = targetClassNameStr,
        fragmentAnnoInfo = fragmentAnnoInfo,
    )

}

private data class GlobalInterceptorInfo(
    val containingFile: KSFile?,
    val descName: String,
    val qualifiedNameStr: String,
    val globalInterceptorAnnoInfo: GlobalInterceptorAnnoInfo,
) {

    data class GlobalInterceptorAnnoInfo(
        val priority: Int,
    )

}

private data class InterceptorInfo(
    val containingFile: KSFile?,
    val descName: String,
    val qualifiedNameStr: String,
    val interceptorAnnoInfo: InterceptorAnnoInfo,
) //
{

    data class InterceptorAnnoInfo(
        val value: String,
    )

}

private sealed class RouterInfo(
    open val containingFile: KSFile?,
    open val descName: String,
    open val qualifiedNameStr: String,
    open val routerAnnoBean: RouterAnnoBean,
) {

    data class ServiceClass(
        override val containingFile: KSFile?,
        override val descName: String,
        override val qualifiedNameStr: String,
        override val routerAnnoBean: RouterAnnoBean,
    ) : RouterInfo(
        containingFile = containingFile,
        descName = descName,
        qualifiedNameStr = qualifiedNameStr,
        routerAnnoBean = routerAnnoBean,
    )

    data class ServiceMethod(
        override val containingFile: KSFile?,
        override val descName: String,
        override val qualifiedNameStr: String,
        override val routerAnnoBean: RouterAnnoBean,
        val firstParameterName: String,
    ) : RouterInfo(
        containingFile = containingFile,
        descName = descName,
        qualifiedNameStr = qualifiedNameStr,
        routerAnnoBean = routerAnnoBean,
    )

}

private data class RouterDegradeInfo(
    val containingFile: KSFile?,
    val descName: String,
    val classClassName: ClassName,
    val routerDegradeAnno: RouterDegradeAnno,
)

/**
 * - ModuleApplication
 * - Fragment
 * - Service
 * https://github.com/aasitnikov/ksp-aggregating-issue/blob/master/experiments/processor/src/main/java/com/example/experiments/processor/ExperimentsProcessor.kt
 */
private class ModuleProcessor(
    override val environment: SymbolProcessorEnvironment,
) : BaseProcessor(
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
        conditionalAnnoInfo: ConditionalAnnoInfo?,
        block: (funSpecBuilder: FunSpec.Builder) -> Unit,
    ) {

        val targetCondition = conditionalAnnoInfo ?: return block(funSpecBuilder)
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
        moduleAppInfoList: List<ApplicationInfo>,
    ) {

        val tempStr = moduleAppInfoList
            .joinToString { item ->
                "${item.qualifiedNameOfClass}()"
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
        serviceInfoList: List<ServiceInfo>,
        serviceDecoratorInfoList: List<ServiceDecoratorInfo>,
    ) {

        if (logEnable) {
            logger.warn("aboutService serviceDecoratorInfoList = ${serviceDecoratorInfoList.joinToString()}")
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
                        serviceInfoList.forEach { serviceInfo ->

                            funSpec.addComment("-------------- ${serviceInfo.descName} -------------- ")

                            val implName = "implName${counter.incrementAndGet()}"

                            val stateCode = "val %N = %L"
                            val args = mutableListOf<Any>()

                            // 参数名
                            args.add(implName)

                            val getImplCallback: (ServiceInfo, FunSpec.Builder) -> Unit =
                                { innerServiceInfo, funSpec ->
                                    when (innerServiceInfo) {
                                        is ServiceInfo.ServiceClass -> {
                                            if (
                                                innerServiceInfo.applicationParameterName.isNullOrEmpty()
                                            ) {
                                                funSpec.addStatement(
                                                    format = "return %T()",
                                                    innerServiceInfo.classTypeName,
                                                )
                                            } else {
                                                funSpec.addStatement(
                                                    format = "return %T(${innerServiceInfo.applicationParameterName} = application)",
                                                    innerServiceInfo.classTypeName,
                                                )
                                            }
                                        }

                                        is ServiceInfo.ServiceMethod -> {
                                            if (innerServiceInfo.applicationParameterName.isNullOrEmpty()) {
                                                funSpec.addStatement(
                                                    format = "return ${innerServiceInfo.qualifiedName}()",
                                                )
                                            } else {
                                                funSpec.addStatement(
                                                    format = "return ${innerServiceInfo.qualifiedName}(${innerServiceInfo.applicationParameterName} = application)",
                                                    innerServiceInfo.classTypeName,
                                                )
                                            }
                                        }
                                    }
                                }

                            // 如果是单利
                            if (serviceInfo.serviceAnnoInfo.singleTon) {
                                args.add(
                                    element = TypeSpec
                                        .anonymousClassBuilder()
                                        .superclass(
                                            superclass = mClassNameSupportSingletonCallable.parameterizedBy(
                                                serviceInfo.classTypeName,
                                            )
                                        )
                                        .addProperty(
                                            propertySpec = PropertySpec
                                                .builder(
                                                    name = "raw",
                                                    type = serviceInfo.classTypeName
                                                )
                                                .addModifiers(
                                                    KModifier.OVERRIDE,
                                                )
                                                .getter(
                                                    getter = FunSpec
                                                        .getterBuilder()
                                                        .also { funcSpec_get ->
                                                            getImplCallback.invoke(
                                                                serviceInfo, funcSpec_get
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
                                                serviceInfo.classTypeName,
                                            )
                                        )
                                        .addFunction(
                                            funSpec = FunSpec
                                                .builder("get")
                                                .addModifiers(
                                                    KModifier.OVERRIDE,
                                                )
                                                .returns(
                                                    returnType = serviceInfo.classTypeName,
                                                )
                                                .also { funcSpec_get ->
                                                    getImplCallback.invoke(
                                                        serviceInfo, funcSpec_get
                                                    )
                                                }
                                                .build()
                                        )
                                        .build()
                                )
                            }

                            funSpec.addStatement(stateCode, *args.toTypedArray())
                            val serviceClassPathList =
                                serviceInfo.serviceAnnoInfo.serviceClassPathList
                            if (serviceClassPathList.isEmpty()) {
                                throw ProcessException(
                                    message = "${serviceInfo.descName} 的 @ServiceAnno 注解, value 不可以为空"
                                )
                            }
                            val nameList = serviceInfo.serviceAnnoInfo.name
                            if (nameList.isNotEmpty() || serviceClassPathList.size > 1) {
                                if (serviceClassPathList.size != nameList.size) {
                                    throw ProcessException(
                                        message = "${serviceInfo.descName} 的 @ServiceAnno 注解, name 属性可以为空数组, 如果不为空, name 属性和 value 属性的个数必须是相等的"
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
                                if (serviceInfo.serviceAnnoInfo.autoInit) {
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
                        serviceDecoratorInfoList.forEach { serviceDecoratorInfo ->

                            addConditionIfCodeToFunction(
                                funSpecBuilder = funSpec,
                                conditionalAnnoInfo = serviceDecoratorInfo.conditionalAnnoInfo,
                            ) {

                                val implName = "implName${counter.incrementAndGet()}"

                                funSpec.addStatement(
                                    format = "val %N = %L",
                                    implName,
                                    TypeSpec
                                        .anonymousClassBuilder()
                                        .addSuperinterface(
                                            superinterface = classNameServiceDecoratorCallable.parameterizedBy(
                                                serviceDecoratorInfo.decorateTargetClassName,
                                            )
                                        )
                                        .addFunction(
                                            funSpec = FunSpec
                                                .builder("get")
                                                .addModifiers(KModifier.OVERRIDE)
                                                .addParameter(
                                                    name = "target",
                                                    type = serviceDecoratorInfo.decorateTargetClassName,
                                                )
                                                .returns(
                                                    returnType = serviceDecoratorInfo.decorateTargetClassName,
                                                )
                                                .addStatement(
                                                    format = "return %T(${serviceDecoratorInfo.constructorParameterName} = target)",
                                                    serviceDecoratorInfo.classClassName,
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
                                                    format = "return ${serviceDecoratorInfo.serviceDecoratorAnnoInfo.priority}"
                                                )
                                                .build()
                                        )
                                        .build()
                                )

                                funSpec.addStatement(
                                    format = "%T.registerDecorator(tClass = %T::class, uid = %S, %N)",
                                    classNameServiceManager,
                                    serviceDecoratorInfo.decorateTargetClassName,
                                    serviceDecoratorInfo.uuid,
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
                        serviceInfoList.forEach { serviceInfo ->
                            val serviceClassPathList =
                                serviceInfo.serviceAnnoInfo.serviceClassPathList
                            serviceClassPathList.forEachIndexed { index, interfaceClassPath ->
                                val serviceName = serviceInfo.serviceAnnoInfo.name.getOrNull(index)
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
                                if (serviceInfo.serviceAnnoInfo.autoInit) {
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
                        serviceDecoratorInfoList
                            .forEach { serviceDecoratorInfo ->
                                // 装饰的目标接口
                                val decorateTargetClassName =
                                    serviceDecoratorInfo.serviceDecoratorAnnoInfo.valueClassPath.toClassName()
                                funSpec.addStatement(
                                    format = "%T.unregisterDecorator(tClass = %T::class, uid = %S)",
                                    classNameServiceManager,
                                    decorateTargetClassName,
                                    serviceDecoratorInfo.uuid,
                                )
                            }
                    }
                    .build()
            )
    }

    @OptIn(KspExperimental::class)
    private fun aboutFragment(
        typeSpecBuilder: TypeSpec.Builder,
        fragmentInfoList: List<FragmentInfo>,
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
                        fragmentInfoList.forEach { fragmentInfo ->

                            if (fragmentInfo.fragmentAnnoInfo.value.isEmpty()) {
                                throw ProcessException(message = "FragmentAnno.value can't be empty: ${fragmentInfo.descName} ")
                            }
                            val targetClassName = ClassName(
                                packageName = fragmentInfo.targetClassNameStr.packageName(),
                                fragmentInfo.targetClassNameStr.simpleClassName(),
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
                                                when (fragmentInfo) {
                                                    is FragmentInfo.ServiceMethod -> {
                                                        if (logEnable) {
                                                            logger.warn(
                                                                message = "fragment KSFunctionDeclaration = ${fragmentInfo.descName}"
                                                            )
                                                        }
                                                        it.addStatement(
                                                            format = "val fragment = ${fragmentInfo.methodQualifiedName}(${fragmentInfo.parameterName} = targetBundle)",
                                                            targetClassName,
                                                        )
                                                    }

                                                    is FragmentInfo.ServiceClass -> {
                                                        it.addStatement(
                                                            format = "val fragment = %T()",
                                                            targetClassName,
                                                        )
                                                        it.addStatement(
                                                            format = "fragment.arguments = targetBundle"
                                                        )
                                                    }
                                                }
                                            }
                                            .addStatement(
                                                format = "return fragment"
                                            )
                                            .build()
                                    )
                                    .build()
                            )

                            fragmentInfo.fragmentAnnoInfo.value.forEach { fragmentName ->
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
                        fragmentInfoList.forEach { fragmentInfo ->
                            fragmentInfo.fragmentAnnoInfo.value.forEach { fragmentName ->
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
        globalInterceptorInfoList: List<GlobalInterceptorInfo>,
        interceptorInfoList: List<InterceptorInfo>,
    ) {

        val interceptorBeanClassName = ClassName(
            packageName = ComponentConstants.INTERCEPTOR_BEAN_CLASS_NAME.packageName(),
            ComponentConstants.INTERCEPTOR_BEAN_CLASS_NAME.simpleClassName(),
        )

        val interceptorClassName = ClassName(
            packageName = ComponentConstants.INTERCEPTOR_INTERFACE_CLASS_NAME.packageName(),
            ComponentConstants.INTERCEPTOR_INTERFACE_CLASS_NAME.simpleClassName(),
        )

        val globalInterceptorListStr = globalInterceptorInfoList
            .joinToString { item ->
                "%T(interceptor = ${item.qualifiedNameStr}::class," + "priority = ${item.globalInterceptorAnnoInfo.priority})"
            }

        val globalInterceptorArgList = globalInterceptorInfoList
            .map {
                interceptorBeanClassName
            }.toTypedArray()

        val interceptorListStr = interceptorInfoList
            .joinToString { item ->
                "\"${item.interceptorAnnoInfo.value}\" to %L::class"
            }

        val interceptorArgList = interceptorInfoList.map { item ->
            item.qualifiedNameStr
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
        routerInfoList: List<RouterInfo>,
    ) {

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

        val routerStr = routerInfoList
            .joinToString { item ->
                StringBuffer()
                    .append("%T(")
                    .append("\nregex = %S,")
                    .apply {
                        if (item.routerAnnoBean.scheme.isNullOrEmpty()) {
                            this.append("\nuri = defaultScheme + %S,")
                        } else {
                            this.append("\nuri = %S,")
                        }
                    }
                    .append("\ndesc = %S,")
                    .apply {
                        this.append("\npageInterceptors = listOf(")
                        item.routerAnnoBean.interceptors.forEach { _ ->
                            this.append("\n%T(priority = %L, interceptorClass = %T::class,),")
                        }
                        item.routerAnnoBean.interceptorNames.forEach { _ ->
                            this.append("\n%T(priority = %L, interceptorName = %S,),")
                        }
                        this.append("),")
                    }
                    .apply {
                        when (item) {
                            is RouterInfo.ServiceClass -> {
                                this.append("\ntargetClass = %L::class,")
                            }

                            is RouterInfo.ServiceMethod -> {
                                this.append("\ncustomerIntentCall = object : %T {")
                                this.append("\n\toverride fun get(request: RouterRequest): %T {")
                                this.append("\n\t\t\treturn %L(\n\t\t\t\t%N = request\n\t\t\t)")
                                this.append("\n\t}")
                                this.append("\n}")
                            }
                        }
                    }
                    .append("\n)")
                    .toString()
            }

        val routerArgList = routerInfoList
            .map { routerInfo ->
                listOfNotNull(
                    routerBeanClassName,
                    routerInfo.routerAnnoBean.regex,
                    if (routerInfo.routerAnnoBean.scheme.isNullOrEmpty()) {
                        "://${routerInfo.routerAnnoBean.hostAndPath()}"
                    } else {
                        "${routerInfo.routerAnnoBean.scheme}://${routerInfo.routerAnnoBean.hostAndPath()}"
                    },
                    routerInfo.routerAnnoBean.desc ?: "",
                ) + routerInfo.routerAnnoBean.interceptors
                    .mapIndexed { index, interceptorClassPathStr ->
                        interceptorClassPathStr to
                                (routerInfo.routerAnnoBean.interceptorPriorities.getOrNull(
                                    index
                                ) ?: 0)
                    }
                    .map {
                        listOf(
                            pageInterceptorClassName,
                            it.second,
                            it.first.toClassName(),
                        )
                    }.flatten() + routerInfo.routerAnnoBean.interceptorNames
                    .mapIndexed { index, interceptorName ->
                        interceptorName to
                                (routerInfo.routerAnnoBean.interceptorNamePriorities.getOrNull(
                                    index
                                ) ?: 0)
                    }
                    .map {
                        listOf(
                            pageInterceptorClassName,
                            it.second,
                            it.first,
                        )
                    }.flatten() + when (routerInfo) {
                    is RouterInfo.ServiceClass -> {
                        listOf(routerInfo.qualifiedNameStr)
                    }

                    is RouterInfo.ServiceMethod -> {
                        if (logEnable) {
                            logger.warn("routerInfo.descName = ${routerInfo.descName}")
                        }
                        listOf(
                            customerIntentCallClassName,
                            mClassNameIntent,
                            routerInfo.qualifiedNameStr,
                            routerInfo.firstParameterName,
                        )
                    }
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
        routerDegradeInfoList: List<RouterDegradeInfo>,
    ) //
    {

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

                        routerDegradeInfoList
                            .forEach { routerDegradeInfo ->

                                codeList.add(
                                    element = "%T(priority = ${routerDegradeInfo.routerDegradeAnno.priority}, targetClass = %T::class)",
                                )

                                args.add(
                                    element = classNameRouterDegradeBean,
                                )

                                args.add(
                                    element = routerDegradeInfo.classClassName,
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

    private val moduleAppInfoList: MutableList<ApplicationInfo> = mutableListOf()
    private val serviceInfoList: MutableList<ServiceInfo> = mutableListOf()
    private val serviceDecoratorInfoList: MutableList<ServiceDecoratorInfo> = mutableListOf()
    private val fragmentInfoList: MutableList<FragmentInfo> = mutableListOf()
    private val globalInterceptorInfoList: MutableList<GlobalInterceptorInfo> = mutableListOf()
    private val interceptorInfoList: MutableList<InterceptorInfo> = mutableListOf()
    private val routerInfoList: MutableList<RouterInfo> = mutableListOf()
    private val routerDegradeInfoList: MutableList<RouterDegradeInfo> = mutableListOf()

    override fun initProcess(resolver: Resolver) {
        super.initProcess(resolver)
        moduleAppInfoList.clear()
        serviceInfoList.clear()
        serviceDecoratorInfoList.clear()
        fragmentInfoList.clear()
        globalInterceptorInfoList.clear()
        interceptorInfoList.clear()
        routerInfoList.clear()
        routerDegradeInfoList.clear()
    }

    @OptIn(KspExperimental::class)
    override fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated> {

        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName ------------- 第 $round 轮 开始了 -------------"
            )
        }

        val (moduleAppValidList, moduleAppInValidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = ModuleAppAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        // 模块 Application 的
        moduleAppInfoList.addAll(
            moduleAppValidList
                .filterIsInstance<KSClassDeclaration>()
                .mapNotNull { item ->
                    item.qualifiedName?.asString()?.let { qualifiedNameOfClass ->
                        ApplicationInfo(
                            containingFile = item.containingFile,
                            qualifiedNameOfClass = qualifiedNameOfClass,
                        )
                    }
                }
        )

        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName moduleAppInfoList = $moduleAppInfoList"
            )
        }

        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName moduleAppInfoList.size = ${moduleAppInfoList.size}"
            )
        }

        val (serviceValidList, serviceInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = ServiceAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        // Service 的
        serviceInfoList.addAll(
            serviceValidList.map { item ->
                val containingFile = item.containingFile
                val descName = item.getDescName()
                val serviceAnno = item
                    .getAnnotationsByType(annotationKClass = ServiceAnno::class)
                    .first()
                val serviceAnnoInfo = ServiceInfo.ServiceAnnoInfo(
                    serviceClassPathList = serviceAnno.serviceClassPathList,
                    name = serviceAnno.name.toList(),
                    singleTon = serviceAnno.singleTon,
                    autoInit = serviceAnno.autoInit,
                )
                when (item) {
                    is KSClassDeclaration -> {
                        val targetApplicationConstructor =
                            item.getConstructors()
                                .find {
                                    it.parameters.size == 1 && it.parameters[0].typeToClassName() == mClassNameAndroidApplication
                                }

                        ServiceInfo.ServiceClass(
                            containingFile = containingFile,
                            descName = descName,
                            serviceAnnoInfo = serviceAnnoInfo,
                            classTypeName = item.toClassName(),
                            applicationParameterName = if (
                                targetApplicationConstructor == null
                            ) {
                                null
                            } else {
                                targetApplicationConstructor.parameters[0].name!!.asString()
                            },
                        )
                    }

                    is KSFunctionDeclaration -> {
                        val classTypeName = item.returnTypeToTypeName()!!
                        if (item.parameters.size > 1) {
                            notSupport(
                                message = "$classTypeName can not have more than one parameter",
                            )
                        }
                        item.parameters.firstOrNull()?.let {
                            if (it.typeToClassName() != mClassNameAndroidApplication) {
                                notSupport()
                            }
                        }
                        ServiceInfo.ServiceMethod(
                            containingFile = containingFile,
                            descName = descName,
                            serviceAnnoInfo = serviceAnnoInfo,
                            classTypeName = classTypeName,
                            applicationParameterName = item.parameters.getOrNull(0)?.name?.asString(),
                            qualifiedName = item.qualifiedName!!.asString(),
                        )
                    }

                    else -> notSupport()
                }
            }
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName serviceInfoList.size = ${serviceInfoList.size}"
            )
        }

        val (serviceDecoratorValidList, serviceDecoratorInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = ServiceDecoratorAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        // ServiceDecorator 的
        serviceDecoratorInfoList.addAll(
            elements = serviceDecoratorValidList
                .filterIsInstance<KSClassDeclaration>()
                .map { item ->
                    val uuid = UUID.randomUUID().toString()
                    val containingFile = item.containingFile
                    val descName = item.getDescName()
                    val serviceDecoratorAnno = item
                        .getAnnotationsByType(annotationKClass = ServiceDecoratorAnno::class)
                        .first()
                    serviceDecoratorAnno.valueClassPath
                    ServiceDecoratorInfo(
                        uuid = uuid,
                        containingFile = containingFile,
                        descName = descName,
                        classClassName = item.toClassName(),
                        serviceDecoratorAnnoInfo = ServiceDecoratorInfo.ServiceDecoratorAnnoInfo(
                            priority = serviceDecoratorAnno.priority,
                            valueClassPath = serviceDecoratorAnno.valueClassPath,
                        ),
                        conditionalAnnoInfo = item
                            .getAnnotationsByType(annotationKClass = ConditionalAnno::class)
                            .firstOrNull()?.let { anno ->
                                ConditionalAnnoInfo(
                                    conditionClassPathList = anno.conditionClassPathList,
                                )
                            },
                        decorateTargetClassName = serviceDecoratorAnno.valueClassPath.toClassName(),
                        constructorParameterName = checkNotNull(
                            value = item.getConstructors()
                                .firstOrNull()?.parameters?.firstOrNull()?.name?.asString(),
                            lazyMessage = {},
                        ),
                    )
                },
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName serviceDecoratorInfoList.size = ${serviceDecoratorInfoList.size}"
            )
        }

        val (fragmentValidList, fragmentInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = FragmentAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        // Fragment 的
        fragmentInfoList.addAll(
            elements = fragmentValidList
                .filter { it is KSClassDeclaration || it is KSFunctionDeclaration }
                .map { item ->
                    val containingFile = item.containingFile
                    val descName = item.getDescName()
                    val fragmentAnno = item.getAnnotationsByType(
                        annotationKClass = FragmentAnno::class,
                    ).first()
                    val fragmentAnnoInfo = FragmentInfo.FragmentAnnoInfo(
                        value = fragmentAnno.value.toList(),
                    )
                    when (item) {
                        is KSFunctionDeclaration -> {
                            if (item.parameters.size != 1) {
                                throw RuntimeException(
                                    "FragmentAnno 注解的方法必须只有一个参数, $descName"
                                )
                            }
                            FragmentInfo.ServiceMethod(
                                containingFile = containingFile,
                                descName = descName,
                                targetClassNameStr = item.returnType
                                    ?.resolve()
                                    ?.declaration
                                    ?.qualifiedName
                                    ?.asString()
                                    ?: "",
                                fragmentAnnoInfo = fragmentAnnoInfo,
                                methodQualifiedName = item.qualifiedName!!.asString(),
                                parameterName = item.parameters.first().name?.asString(),
                            )
                        }

                        is KSClassDeclaration -> {
                            FragmentInfo.ServiceClass(
                                containingFile = containingFile,
                                descName = descName,
                                targetClassNameStr = item.qualifiedName?.asString() ?: "",
                                fragmentAnnoInfo = fragmentAnnoInfo,
                            )
                        }

                        else -> throw RuntimeException("Unsupported type: $descName")
                    }
                },
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName fragmentInfoList.size = ${fragmentInfoList.size}"
            )
        }

        val (globalInterceptorValidList, globalInterceptorInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = GlobalInterceptorAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        // 全局拦截器的
        globalInterceptorInfoList.addAll(
            elements = globalInterceptorValidList
                .filterIsInstance<KSClassDeclaration>()
                .map { item ->
                    val containingFile = item.containingFile
                    val descName = item.getDescName()
                    GlobalInterceptorInfo(
                        containingFile = containingFile,
                        descName = descName,
                        qualifiedNameStr = item.qualifiedName!!.asString(),
                        globalInterceptorAnnoInfo = item
                            .getAnnotationsByType(annotationKClass = GlobalInterceptorAnno::class)
                            .first().let { anno ->
                                GlobalInterceptorInfo.GlobalInterceptorAnnoInfo(
                                    priority = anno.priority,
                                )
                            },
                    )
                },
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName globalInterceptorInfoList.size = ${globalInterceptorInfoList.size}"
            )
        }

        val (interceptorValidList, interceptorInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = InterceptorAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }
        // 拦截器
        interceptorInfoList.addAll(
            elements = interceptorValidList
                .filterIsInstance<KSClassDeclaration>()
                .map { item ->
                    val containingFile = item.containingFile
                    val descName = item.getDescName()
                    InterceptorInfo(
                        containingFile = containingFile,
                        descName = descName,
                        qualifiedNameStr = item.qualifiedName!!.asString(),
                        interceptorAnnoInfo = item
                            .getAnnotationsByType(annotationKClass = InterceptorAnno::class)
                            .first().let {
                                InterceptorInfo.InterceptorAnnoInfo(
                                    value = it.value
                                )
                            },
                    )
                },
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName interceptorInfoList.size = ${interceptorInfoList.size}"
            )
        }

        val (routerValidList, routerInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = RouterAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        // 路由的
        routerInfoList.addAll(
            elements = routerValidList
                .map { item ->
                    val containingFile = item.containingFile
                    val descName = item.getDescName()
                    val routerAnno = item
                        .getAnnotationsByType(annotationKClass = RouterAnno::class)
                        .first()
                    when (item) {
                        is KSFunctionDeclaration -> {
                            RouterInfo.ServiceMethod(
                                containingFile = containingFile,
                                descName = descName,
                                qualifiedNameStr = item.qualifiedName!!.asString(),
                                routerAnnoBean = toRouterAnnoBean(
                                    element = item,
                                    routerAnno = routerAnno,
                                ),
                                firstParameterName = item.parameters.first().name!!.asString(),
                            )
                        }

                        is KSClassDeclaration -> {
                            RouterInfo.ServiceClass(
                                containingFile = containingFile,
                                descName = descName,
                                qualifiedNameStr = item.qualifiedName!!.asString(),
                                routerAnnoBean = toRouterAnnoBean(
                                    element = item,
                                    routerAnno = routerAnno,
                                ),
                            )
                        }

                        else -> notSupport()
                    }

                },
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName routerInfoList.size = ${routerInfoList.size}"
            )
        }

        val (routerDegradeValidList, routerDegradeInvalidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = RouterDegradeAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }
        // 路由降级的
        routerDegradeInfoList.addAll(
            elements = routerDegradeValidList
                .filterIsInstance<KSClassDeclaration>()
                .map { item ->
                    val containingFile = item.containingFile
                    val descName = item.getDescName()
                    RouterDegradeInfo(
                        containingFile = containingFile,
                        descName = descName,
                        classClassName = item.toClassName(),
                        routerDegradeAnno = item
                            .getAnnotationsByType(annotationKClass = RouterDegradeAnno::class)
                            .first(),
                    )
                },
        )
        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName routerDegradeInfoList.size = ${routerDegradeInfoList.size}"
            )
        }

        return (moduleAppInValidList + serviceInvalidList + serviceDecoratorInvalidList +
                fragmentInvalidList + globalInterceptorInvalidList + interceptorInvalidList +
                routerInvalidList + routerDegradeInvalidList).apply {
            if (logEnable) {
                logger.warn(
                    "$TAG $componentModuleName ------------- 第 $round 轮 结束了 -------------"
                )
            }
        }

    }

    private fun generateFile() {

        if (logEnable) {
            logger.warn(
                "$TAG $componentModuleName 开始生成文件"
            )
            for (file in codeGenerator.generatedFile) {
                logger.warn(
                    "$TAG $componentModuleName generatedFile.item = ${file.path}"
                )
            }
        }

        val allMarkedList = (routerDegradeInfoList)

        val sources = (moduleAppInfoList
            .mapNotNull { it.containingFile } + serviceInfoList
            .mapNotNull { it.containingFile } + serviceDecoratorInfoList
            .mapNotNull { it.containingFile } + fragmentInfoList
            .mapNotNull { it.containingFile } + globalInterceptorInfoList
            .mapNotNull { it.containingFile } + interceptorInfoList
            .mapNotNull { it.containingFile } + routerInfoList
            .mapNotNull { it.containingFile } + allMarkedList
            .mapNotNull { it.containingFile })
            .toTypedArray()

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
                    moduleAppInfoList = moduleAppInfoList,
                )
                aboutService(
                    typeSpecBuilder = this,
                    serviceInfoList = serviceInfoList,
                    serviceDecoratorInfoList = serviceDecoratorInfoList,
                )
                aboutFragment(
                    typeSpecBuilder = this,
                    fragmentInfoList = fragmentInfoList,
                )
                aboutInterceptor(
                    typeSpecBuilder = this,
                    globalInterceptorInfoList = globalInterceptorInfoList,
                    interceptorInfoList = interceptorInfoList,
                )
                aboutRouter(
                    typeSpecBuilder = this,
                    routerInfoList = routerInfoList,
                )
                aboutRouterDegrade(
                    typeSpecBuilder = this,
                    routerDegradeInfoList = routerDegradeInfoList,
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
            val targetDataArray = fileSpec.toString().toByteArray()
            codeGenerator.createNewFile(
                dependencies = if (sources.isEmpty()) {
                    Dependencies.ALL_FILES
                } else {
                    Dependencies(
                        aggregating = false,
                        sources = sources,
                    )
                },
                packageName = fileSpec.packageName,
                fileName = fileSpec.name,
            ).use { outputStream ->
                outputStream.write(
                    targetDataArray
                )
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
        generateFile()
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