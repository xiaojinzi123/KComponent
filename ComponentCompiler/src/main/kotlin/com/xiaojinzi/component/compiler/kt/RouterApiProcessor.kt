package com.xiaojinzi.component.compiler.kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.anno.router.*
import com.xiaojinzi.component.anno.support.ComponentGeneratedAnno
import com.xiaojinzi.component.packageName
import com.xiaojinzi.component.simpleClassName

private data class RouterApiInfo(
    // 生成的类要实现的接口的 String 全类名
    val fullClassName: String,
    // 生成的类要实现的接口的 ClassName
    val targetRouterApiInterfaceClassName: ClassName = fullClassName.toClassName(),
    // 默认的 SchemeAnno
    val defaultSchemeAnno: SchemeAnno?,
    // 默认的 HostAnno
    val defaultHostAnno: HostAnno?,
    // 默认的 CategoryAnno 的值
    val defaultCategoryValueList: List<String>,
    // 默认的 FlagAnno 的值
    val defaultFlagValueList: List<Int>,
    val functionInfoList: List<FunctionInfo>,
) {

    data class FunctionInfo(
        // 导航的注解, 默认可以省略
        val navigateAnno: NavigateAnno?,
        // SchemeAnno 信息
        val schemeAnno: SchemeAnno?,
        // userInfo 信息
        val userInfoAnno: UserInfoAnno?,
        // 标记路由的 url
        val urlAnno: UrlAnno?,
        // 标记路由的 host
        val hostAnno: HostAnno?,
        // 标记路由的 path
        val pathAnno: PathAnno?,
        // 标记路由的地址
        val hostAndPathAnno: HostAndPathAnno?,
        val categoryValueList: List<String>,
        val flagValueList: List<Int>,
        // 使用的拦截器
        val useInterceptorAnno: UseInterceptorAnno?,
        // RequestCodeAnno 信息
        val requestCodeAnno: RequestCodeAnno?,
        val checkRepeatAnno: CheckRepeatAnno?,
        // 方法返回值类型的声明
        val returnTypeKsDeclaration: KSDeclaration?,
        // 是否是 suspend 函数
        val isSuspendMethod: Boolean,
        // 方法名字
        val methodName: String,
        // 是否返回 Rx 的 Single
        val isSingleReturnType: Boolean,
        // 是否返回 Rx 的 Completable
        val isCompletableReturnType: Boolean,
        // 是否返回 NavigationDisposable
        val isComponentNavigationDisposableReturnType: Boolean,
        // 是否返回 Navigator
        val isComponentNavigatorReturnType: Boolean,
        // 是否返回 Component 的 Call
        val isComponentCallReturnType: Boolean,
    )

}

private class RouterApiProcessor(
    override val environment: SymbolProcessorEnvironment,
) : BaseProcessor(
    environment = environment,
) {

    private val TAG = "RouterApiProcessor"

    var componentCallbackKSClassDeclaration: KSClassDeclaration? = null

    var componentActivityResultKSClassDeclaration: KSClassDeclaration? = null

    var componentNavigatorKSClassDeclaration: KSClassDeclaration? = null

    var componentCallKSClassDeclaration: KSClassDeclaration? = null

    var componentBiCallbackKSClassDeclaration: KSClassDeclaration? = null

    var androidIntentKSClassDeclaration: KSClassDeclaration? = null

    var androidContextKSClassDeclaration: KSClassDeclaration? = null

    var rxSingleKSClassDeclaration: KSClassDeclaration? = null

    var navigationDisposableKSClassDeclaration: KSClassDeclaration? = null

    var rxCompletableKSClassDeclaration: KSClassDeclaration? = null

    var kotlinFunction0KSClassDeclaration: KSClassDeclaration? = null

    var kotlinFunction1KSClassDeclaration: KSClassDeclaration? = null

    private val collectList = mutableListOf<RouterApiInfo>()

    override fun initProcess(resolver: Resolver) {
        super.initProcess(resolver)
        componentCallbackKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.CALLBACK_CLASS_NAME)

        componentActivityResultKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.COMPONENT_ACTIVITY_RESULT_CLASS_NAME)

        componentNavigatorKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.NAVIGATOR_CLASS_NAME)

        componentCallKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.CALL_CLASS_NAME)

        componentBiCallbackKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.BICALLBACK_CLASS_NAME)

        androidIntentKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_INTENT)

        androidContextKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_CONTEXT)

        rxSingleKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.RXJAVA_SINGLE)

        navigationDisposableKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.NAVIGATIONDISPOSABLE_CLASS_NAME)

        rxCompletableKSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.RXJAVA_COMPLETABLE)

        kotlinFunction0KSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.KOTLIN_FUNCTION0)

        kotlinFunction1KSClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.KOTLIN_FUNCTION1)
    }

    @OptIn(KspExperimental::class)
    private fun createFile(
        routerApiInfo: RouterApiInfo,
    ) {

        // 生成的目标类的 String className
        val targetClassSimpleName = routerApiInfo.fullClassName.simpleClassName() + ComponentUtil.UIROUTERAPI

        val typeSpec = TypeSpec
            .classBuilder(name = targetClassSimpleName)
            .addAnnotation(annotation = mClassNameAndroidKeepAnno)
            .addAnnotation(annotation = ComponentGeneratedAnno::class)
            .addSuperinterface(
                superinterface = routerApiInfo.targetRouterApiInterfaceClassName,
            )
            .also { typeSpec ->
                routerApiInfo
                    .functionInfoList
                    .forEach { functionInfo ->

                        if (logEnable) {
                            /*logger.warn(
                                message = "$TAG $componentModuleName functionName = $functionNameStr",
                            )*/
                        }

                        // 路由的类别
                        val categoryValueList =
                            routerApiInfo.defaultCategoryValueList + functionInfo.categoryValueList

                        // 路由的 flag
                        val flagValueList =
                            routerApiInfo.defaultFlagValueList + (functionInfo.flagValueList)

                        // 是否是返回 Rx
                        val isRxReturnType = functionInfo.isSingleReturnType || functionInfo.isCompletableReturnType

                        val isNeedReturn = (functionInfo.navigateAnno?.let {
                            functionInfo.navigateAnno.forResult || functionInfo.navigateAnno.forIntent || functionInfo.navigateAnno.forResultCode
                        } ?: false) || functionInfo.isComponentNavigationDisposableReturnType
                                || functionInfo.isCompletableReturnType || functionInfo.isComponentNavigatorReturnType || functionInfo.isComponentCallReturnType

                        val navigatePrefixStr = if (functionInfo.isComponentNavigationDisposableReturnType) {
                            "navigate"
                        } else {
                            "forward"
                        }

                        var ksValueParameter_context: KSValueParameter? = null
                        var ksValueParameter_options: KSValueParameter? = null
                        var ksValueParameter_callback: KSValueParameter? = null
                        var ksValueParameter_biCallback: KSValueParameter? = null
                        var ksValueParameter_kt_function0: KSValueParameter? = null
                        var ksValueParameter_kt_function1: KSValueParameter? = null
                        var ksValueParameter_beforeAction: KSValueParameter? = null
                        var ksValueParameter_beforeStartAction: KSValueParameter? = null
                        var ksValueParameter_afterAction: KSValueParameter? = null
                        var ksValueParameter_afterError: KSValueParameter? = null
                        var ksValueParameter_afterEvent: KSValueParameter? = null
                        var ksValueParameter_afterStart: KSValueParameter? = null
                        var ksValueParameter_requestCode: KSValueParameter? = null
                        var ksValueParameter_bundle: KSValueParameter? = null

                        val returnTypePoetTypeName = ksFunctionDeclaration.returnTypeToTypeName()

                        // 几个扩展函数成员
                        val activityResultCallExtendMethodMemberName =
                            MemberName("com.xiaojinzi.component.impl", "activityResultCall")
                        val intentCallExtendMethodMemberName =
                            MemberName("com.xiaojinzi.component.impl", "intentCall")
                        val resultCodeMatchCallExtendMethodMemberName =
                            MemberName("com.xiaojinzi.component.impl", "resultCodeMatchCall")
                        val intentResultCodeMatchCallExtendMethodMemberName =
                            MemberName("com.xiaojinzi.component.impl", "intentResultCodeMatchCall")
                        val resultCodeCallExtendMethodMemberName =
                            MemberName("com.xiaojinzi.component.impl", "resultCodeCall")
                        val callExtendMethodMemberName =
                            MemberName("com.xiaojinzi.component.impl", "call")

                        typeSpec.addFunction(
                            funSpec = FunSpec
                                .builder(name = functionInfo.methodName)
                                .addModifiers(KModifier.OVERRIDE)
                                // 先添加方法的参数, 同时解析出一些特别的参数
                                .apply {
                                    ksFunctionDeclaration
                                        .parameters
                                        .forEach { ksValueParameter ->

                                            when {
                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = OptionsAnno::class
                                                ) -> {
                                                    ksValueParameter_options = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = BeforeRouteSuccessActionAnno::class
                                                ) -> {
                                                    ksValueParameter_beforeAction = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = BeforeStartActivityActionAnno::class
                                                ) -> {
                                                    ksValueParameter_beforeStartAction =
                                                        ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = AfterRouteActionAnno::class
                                                ) -> {
                                                    ksValueParameter_afterAction = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = AfterRouteErrorActionAnno::class
                                                ) -> {
                                                    ksValueParameter_afterError = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = AfterRouteEventActionAnno::class
                                                ) -> {
                                                    ksValueParameter_afterEvent = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = AfterStartActivityActionAnno::class
                                                ) -> {
                                                    ksValueParameter_afterStart = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = RequestCodeAnno::class
                                                ) -> {
                                                    ksValueParameter_requestCode = ksValueParameter
                                                }

                                                ksValueParameter.isAnnotationPresent(
                                                    annotationKClass = ParameterBundleAnno::class
                                                ) -> {
                                                    ksValueParameter_bundle = ksValueParameter
                                                }
                                            }
                                            when (ksValueParameter.type.resolve().declaration.qualifiedName) {
                                                androidContextKSClassDeclaration?.qualifiedName -> {
                                                    ksValueParameter_context = ksValueParameter
                                                }

                                                componentCallbackKSClassDeclaration?.qualifiedName -> {
                                                    ksValueParameter_callback = ksValueParameter
                                                }

                                                componentBiCallbackKSClassDeclaration?.qualifiedName -> {
                                                    ksValueParameter_biCallback = ksValueParameter
                                                }

                                                kotlinFunction0KSClassDeclaration?.qualifiedName -> {
                                                    ksValueParameter_kt_function0 = ksValueParameter
                                                }

                                                kotlinFunction1KSClassDeclaration?.qualifiedName -> {
                                                    ksValueParameter_kt_function1 = ksValueParameter
                                                }
                                            }

                                            this.addParameter(
                                                name = ksValueParameter.name!!.asString(),
                                                type = try {
                                                    ksValueParameter.typeToClassName()
                                                } catch (e: Exception) {
                                                    if (logEnable) {
                                                        logger.warn(
                                                            message = "$TAG $componentModuleName ksValueParameter = $ksValueParameter"
                                                        )
                                                    }
                                                    throw e
                                                },
                                            )
                                        }
                                }
                                .also { funSpecBuilder ->

                                    if (functionInfo.isSuspendMethod) {
                                        funSpecBuilder.addModifiers(
                                            KModifier.SUSPEND
                                        )
                                    }

                                    returnTypePoetTypeName?.let {
                                        if (logEnable) {
                                            logger.warn(message = "$TAG $componentModuleName returnTypePoetTypeName = $returnTypePoetTypeName")
                                        }
                                        funSpecBuilder.returns(
                                            returnType = it
                                        )
                                    }

                                    val functionCodeStringBuffer = StringBuffer()
                                    val functionArgList = mutableListOf<Any>()

                                    if (isNeedReturn) {
                                        functionCodeStringBuffer.append(
                                            "return "
                                        )
                                    }

                                    functionCodeStringBuffer.append(
                                        "%T.with("
                                    )

                                    functionArgList.add(
                                        element = mClassNameRouter,
                                    )

                                    if (ksValueParameter_context == null) {
                                        functionCodeStringBuffer.append(
                                            ")",
                                        )
                                    } else {
                                        functionCodeStringBuffer.append(
                                            "context = %N)",
                                        )
                                        functionArgList.add(
                                            element = ksValueParameter_context.name!!.asString(),
                                        )
                                    }

                                    // scheme userInfo host path 的处理
                                    run {
                                        functionInfo.schemeAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.scheme(scheme = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.userInfoAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.userInfo(userInfo = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.urlAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.url(url = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.hostAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.host(host = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.pathAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.path(path = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.hostAndPathAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.hostAndPath(hostAndPath = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                    }

                                    // 参数的处理
                                    run {
                                        // 普通的参数处理
                                        ksFunctionDeclaration
                                            .parameters
                                            .forEach { ksValueParameter ->

                                                val parameterNameStr =
                                                    ksValueParameter.name!!.asString()

                                                ksValueParameter.getAnnotationsByType(
                                                    annotationKClass = ParameterAnno::class,
                                                ).firstOrNull()?.let { parameterAnno ->
                                                    val methodCallName = getMethodNameFromKsType(
                                                        ksType = ksValueParameter.type.resolve(),
                                                        prefix = "put",
                                                    )
                                                    functionCodeStringBuffer.append(
                                                        "\n.$methodCallName(key = %S, value = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = parameterAnno.value.ifEmpty {
                                                            parameterNameStr
                                                        },
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter.name!!.asString(),
                                                    )
                                                }
                                            }

                                        // Bundle 参数处理
                                        ksValueParameter_bundle?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.putAll(bundle = %N)",
                                            )
                                            functionArgList.add(
                                                element = it.name!!.asString(),
                                            )
                                        }
                                    }

                                    // routeRepeatCheck requestCode category flag options 等处理
                                    run {

                                        functionInfo.checkRepeatAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.useRouteRepeatCheck(useRouteRepeatCheck = %L)",
                                            )

                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }

                                        // 如果没有参数, 就看看有没有标记方法上的注解
                                        if (ksValueParameter_requestCode == null) {
                                            functionInfo.requestCodeAnno?.let {
                                                if (it.value == Int.MIN_VALUE) {
                                                    functionCodeStringBuffer.append(
                                                        "\n // requestCode 框架将会随机生成",
                                                    )
                                                }
                                                functionCodeStringBuffer.append(
                                                    "\n.requestCode(requestCode = %L)",
                                                )

                                                functionArgList.add(
                                                    element = it.value,
                                                )
                                            }
                                        } else {
                                            ksValueParameter_requestCode?.let {
                                                functionCodeStringBuffer.append(
                                                    "\n.requestCode(requestCode = %N)",
                                                )

                                                functionArgList.add(
                                                    element = it.name!!.asString(),
                                                )
                                            }
                                        }

                                        ksValueParameter_options?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.options(options = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString(),
                                            )

                                        }

                                        if (categoryValueList.isNotEmpty()) {

                                            functionCodeStringBuffer.append(
                                                "\n.addIntentCategories("
                                            )

                                            categoryValueList.forEach { categoryValue ->
                                                functionCodeStringBuffer.append(
                                                    "%S,",
                                                )
                                                functionArgList.add(
                                                    element = categoryValue,
                                                )
                                            }

                                            functionCodeStringBuffer.append(
                                                ")"
                                            )

                                        }

                                        if (flagValueList.isNotEmpty()) {

                                            functionCodeStringBuffer.append(
                                                "\n.addIntentFlags("
                                            )

                                            flagValueList.forEach { flagValue ->
                                                functionCodeStringBuffer.append(
                                                    "%L,",
                                                )
                                                functionArgList.add(
                                                    element = flagValue,
                                                )
                                            }

                                            functionCodeStringBuffer.append(
                                                ")"
                                            )

                                        }

                                    }

                                    // 处理拦截器的使用
                                    run {

                                        functionInfo.useInterceptorAnno?.let { useInterceptorAnno ->

                                            run {
                                                val classesClassPathList =
                                                    useInterceptorAnno.classesClassPathList

                                                if (classesClassPathList.isNotEmpty()) {
                                                    functionCodeStringBuffer.append(
                                                        "\n.interceptors(",
                                                    )
                                                    classesClassPathList.forEach {
                                                        functionCodeStringBuffer.append(
                                                            "%T::class,",
                                                        )
                                                        functionArgList.add(
                                                            element = it.toClassName(),
                                                        )
                                                    }
                                                    functionCodeStringBuffer.append(
                                                        ")",
                                                    )
                                                }
                                            }

                                            run {
                                                if (useInterceptorAnno.names.isNotEmpty()) {
                                                    functionCodeStringBuffer.append(
                                                        "\n.interceptorNames(",
                                                    )
                                                    useInterceptorAnno.names.forEach {
                                                        functionCodeStringBuffer.append(
                                                            "%S,",
                                                        )
                                                        functionArgList.add(
                                                            element = it,
                                                        )
                                                    }
                                                    functionCodeStringBuffer.append(
                                                        ")",
                                                    )
                                                }
                                            }

                                        }
                                    }

                                    // 几个回调的处理
                                    run {

                                        ksValueParameter_beforeAction?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.beforeRouteAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString()
                                            )

                                        }

                                        ksValueParameter_beforeStartAction?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.beforeStartActivityAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString()
                                            )

                                        }

                                        ksValueParameter_afterAction?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterRouteSuccessAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString()
                                            )

                                        }

                                        ksValueParameter_afterError?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterRouteErrorAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString()
                                            )

                                        }

                                        ksValueParameter_afterEvent?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterRouteEventAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString()
                                            )

                                        }

                                        ksValueParameter_afterStart?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterStartActivityAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name!!.asString()
                                            )

                                        }

                                    }

                                    // 结尾方法的处理
                                    when {

                                        functionInfo.navigateAnno?.forIntent == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    if (functionInfo.navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.%M(expectedResultCode = %L)",
                                                        )
                                                        functionArgList.add(
                                                            element = intentResultCodeMatchCallExtendMethodMemberName,
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnno.resultCodeMatch,
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.%M()",
                                                        )
                                                        functionArgList.add(
                                                            element = intentCallExtendMethodMemberName,
                                                        )
                                                    }
                                                }

                                                functionInfo.isSuspendMethod -> {
                                                    if (functionInfo.navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.resultCodeMatchAndIntentAwait(expectedResultCode = %L)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnno.resultCodeMatch,
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.intentAwait()",
                                                        )
                                                    }
                                                }

                                                ksValueParameter_biCallback != null -> {
                                                    if (functionInfo.navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntentAndResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnno.resultCodeMatch,
                                                        )
                                                        functionArgList.add(
                                                            element = ksValueParameter_biCallback.name!!.asString(),
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntent(callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = ksValueParameter_biCallback.name!!.asString(),
                                                        )
                                                    }
                                                }

                                                ksValueParameter_kt_function1 != null -> {
                                                    if (functionInfo.navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntentAndResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnno.resultCodeMatch,
                                                        )
                                                        functionArgList.add(
                                                            element = ksValueParameter_kt_function1!!.name!!.asString(),
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntent(callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = ksValueParameter_kt_function1!!.name!!.asString(),
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        functionInfo.navigateAnno?.forResult == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M()",
                                                    )
                                                    functionArgList.add(
                                                        element = activityResultCallExtendMethodMemberName,
                                                    )
                                                }

                                                functionInfo.isSuspendMethod -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.activityResultAwait()",
                                                    )
                                                }

                                                ksValueParameter_biCallback != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResult(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_biCallback!!.name!!.asString(),
                                                    )
                                                }

                                                ksValueParameter_kt_function1 != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResult(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_kt_function1!!.name!!.asString(),
                                                    )
                                                }
                                            }
                                        }

                                        functionInfo.navigateAnno?.forResultCode == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M()",
                                                    )
                                                    functionArgList.add(
                                                        element = resultCodeCallExtendMethodMemberName,
                                                    )
                                                }

                                                functionInfo.isSuspendMethod -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.resultCodeAwait()",
                                                    )
                                                }

                                                ksValueParameter_biCallback != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCode(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_biCallback!!.name!!.asString(),
                                                    )
                                                }

                                                ksValueParameter_kt_function1 != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCode(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_kt_function1!!.name!!.asString(),
                                                    )
                                                }
                                            }
                                        }

                                        functionInfo.navigateAnno?.resultCodeMatchValid == true -> {

                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M(expectedResultCode = %L)",
                                                    )
                                                    functionArgList.add(
                                                        element = resultCodeMatchCallExtendMethodMemberName,
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnno.resultCodeMatch,
                                                    )
                                                }

                                                functionInfo.isSuspendMethod -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.resultCodeMatchAwait(expectedResultCode = %L)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnno.resultCodeMatch,
                                                    )
                                                }

                                                ksValueParameter_callback != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnno.resultCodeMatch,
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_callback.name!!.asString(),
                                                    )
                                                }

                                                ksValueParameter_kt_function0 != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnno.resultCodeMatch,
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_kt_function0.name!!.asString(),
                                                    )
                                                }
                                            }

                                        }

                                        else -> {

                                            if (functionInfo.isComponentNavigatorReturnType || functionInfo.isComponentCallReturnType) {
                                                // 就是空的
                                            } else if (functionInfo.isSuspendMethod) {

                                                functionCodeStringBuffer.append(
                                                    "\n.await()",
                                                )

                                            } else if (functionInfo.isCompletableReturnType) {

                                                functionCodeStringBuffer.append(
                                                    "\n.%M()",
                                                )
                                                functionArgList.add(
                                                    element = callExtendMethodMemberName,
                                                )

                                            } else {

                                                functionCodeStringBuffer.append(
                                                    "\n.$navigatePrefixStr(",
                                                )

                                                ksValueParameter_callback?.let {
                                                    functionCodeStringBuffer.append(
                                                        "callback = %N",
                                                    )
                                                    functionArgList.add(
                                                        element = it.name!!.asString(),
                                                    )
                                                }

                                                functionCodeStringBuffer.append(
                                                    ")",
                                                )

                                            }

                                        }

                                    }

                                    funSpecBuilder.addStatement(
                                        format = functionCodeStringBuffer.toString(),
                                        *functionArgList.toTypedArray(),
                                    )

                                }
                                .build()
                        )

                    }
            }
            .build()

        val fileSpec = FileSpec
            .builder(
                packageName = routerApiInfo.fullClassName.packageName(),
                fileName = targetClassSimpleName,
            )
            .addType(typeSpec = typeSpec)
            .build()

        try {
            routerApiKSClassDeclaration.containingFile
                ?.let { containingFile ->
                    val targetDataArray = fileSpec.toString().toByteArray()
                    codeGenerator.createNewFile(
                        dependencies = Dependencies(
                            aggregating = false,
                            containingFile,
                        ),
                        packageName = fileSpec.packageName,
                        fileName = fileSpec.name,
                    ).use {
                        it.write(
                            targetDataArray
                        )
                    }
                }
        } catch (e: Exception) {
            if (logEnable) {
                logger.warn(
                    "$TAG $componentModuleName 生成文件失败啦~~~"
                )
                logger.exception(e = e)
            }
        }

    }

    @OptIn(KspExperimental::class)
    override fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated> {

        val (validList, inValidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = RouterApiAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        collectList.addAll(
            elements = validList
                .filterIsInstance<KSClassDeclaration>()
                .map { classItem ->
                    val defaultSchemeAnno = classItem
                        .getAnnotationsByType(
                            annotationKClass = SchemeAnno::class
                        ).firstOrNull()
                    val defaultHostAnno = classItem
                        .getAnnotationsByType(
                            annotationKClass = HostAnno::class
                        ).firstOrNull()
                    RouterApiInfo(
                        fullClassName = classItem.asStarProjectedType().declaration.qualifiedName!!.asString(),
                        defaultSchemeAnno = defaultSchemeAnno,
                        defaultHostAnno = defaultHostAnno,
                        defaultCategoryValueList = classItem
                            .getAnnotationsByType(
                                annotationKClass = CategoryAnno::class
                            ).firstOrNull()?.value?.toList() ?: emptyList(),
                        defaultFlagValueList = classItem
                            .getAnnotationsByType(
                                annotationKClass = FlagAnno::class
                            ).firstOrNull()?.value?.toList() ?: emptyList(),
                        functionInfoList = classItem
                            .getDeclaredFunctions()
                            .map { ksFunctionDeclaration ->
                                val navigateAnno = ksFunctionDeclaration
                                    .getAnnotationsByType(
                                        annotationKClass = NavigateAnno::class
                                    ).firstOrNull()
                                val returnTypeKsDeclaration = ksFunctionDeclaration
                                    .returnType
                                    ?.resolve()
                                    ?.declaration
                                RouterApiInfo.FunctionInfo(
                                    navigateAnno = navigateAnno,
                                    schemeAnno = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = SchemeAnno::class,
                                    ).firstOrNull() ?: defaultSchemeAnno,
                                    userInfoAnno = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = UserInfoAnno::class,
                                    ).firstOrNull(),
                                    urlAnno = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = UrlAnno::class,
                                    ).firstOrNull(),
                                    hostAnno = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = HostAnno::class,
                                    ).firstOrNull() ?: defaultHostAnno,
                                    pathAnno = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = PathAnno::class,
                                    ).firstOrNull(),
                                    hostAndPathAnno = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = HostAndPathAnno::class,
                                    ).firstOrNull(),
                                    categoryValueList = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = CategoryAnno::class,
                                        ).firstOrNull()?.value?.toList() ?: emptyList(),
                                    flagValueList = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = FlagAnno::class,
                                        ).firstOrNull()?.value?.toList() ?: emptyList(),
                                    useInterceptorAnno = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = UseInterceptorAnno::class,
                                        ).firstOrNull(),
                                    requestCodeAnno = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = RequestCodeAnno::class,
                                        ).firstOrNull(),
                                    checkRepeatAnno = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = CheckRepeatAnno::class,
                                        ).firstOrNull(),
                                    returnTypeKsDeclaration = returnTypeKsDeclaration,
                                    isSuspendMethod = ksFunctionDeclaration
                                        .modifiers
                                        .contains(element = Modifier.SUSPEND),
                                    methodName = ksFunctionDeclaration
                                        .simpleName
                                        .getShortName(),
                                    isSingleReturnType = returnTypeKsDeclaration
                                        ?.qualifiedName == rxSingleKSClassDeclaration?.qualifiedName,
                                    isCompletableReturnType = returnTypeKsDeclaration
                                        ?.qualifiedName == rxCompletableKSClassDeclaration?.qualifiedName,
                                    isComponentNavigationDisposableReturnType = returnTypeKsDeclaration
                                        ?.qualifiedName == navigationDisposableKSClassDeclaration?.qualifiedName,
                                    isComponentNavigatorReturnType = returnTypeKsDeclaration
                                        ?.qualifiedName == componentNavigatorKSClassDeclaration?.qualifiedName,
                                    isComponentCallReturnType = returnTypeKsDeclaration
                                        ?.qualifiedName == componentCallKSClassDeclaration?.qualifiedName,
                                )
                            }.toList(),
                    )
                }
        )

        return inValidList

    }

    override fun finish() {
        super.finish()
        collectList.forEach { routerApiInfo ->
            createFile(
                routerApiInfo = routerApiInfo,
            )
        }
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
class RouterApiProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouterApiProcessor(
            environment = environment,
        )
    }

}

@OptIn(KspExperimental::class)
val UseInterceptorAnno.classesClassPathList: List<String>
    get() {
        return try {
            this.classes.forEach {
                // 走不到这里的
                println("")
            }
            // 走不到这里的
            emptyList()
        } catch (e: KSTypesNotPresentException) {
            e.ksTypes.map { it.declaration.qualifiedName!!.asString() }
        } catch (e: Exception) {
            emptyList()
        }
    }