package com.xiaojinzi.component.compiler.kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.anno.router.*
import com.xiaojinzi.component.anno.support.ComponentGeneratedAnno
import com.xiaojinzi.component.packageName
import com.xiaojinzi.component.simpleClassName

class RouterApiProcessor(
    override val environment: SymbolProcessorEnvironment,
) : BaseHostProcessor(
    environment = environment,
) {

    private val TAG = "RouterApiProcessor"

    @OptIn(KspExperimental::class)
    private fun createFile(
        resolver: Resolver,
        routerApiKSClassDeclaration: KSClassDeclaration,
    ) {

        // 默认的 SchemeAnno
        val defaultSchemeAnno: SchemeAnno? = routerApiKSClassDeclaration.getAnnotationsByType(
            annotationKClass = SchemeAnno::class
        ).firstOrNull()

        // 默认的 HostAnno
        val defaultHostAnno: HostAnno? = routerApiKSClassDeclaration.getAnnotationsByType(
            annotationKClass = HostAnno::class
        ).firstOrNull()

        // 默认的 CategoryAnno 的值
        val defaultCategoryValueList = routerApiKSClassDeclaration.getAnnotationsByType(
            annotationKClass = CategoryAnno::class
        ).firstOrNull()?.value?.toList() ?: emptyList()

        // 默认的 FlagAnno 的值
        val defaultFlagValueList = routerApiKSClassDeclaration.getAnnotationsByType(
            annotationKClass = FlagAnno::class
        ).firstOrNull()?.value?.toList() ?: emptyList()

        val componentCallbackKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.CALLBACK_CLASS_NAME)

        val componentActivityResultKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.COMPONENT_ACTIVITY_RESULT_CLASS_NAME)

        val componentNavigatorKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.NAVIGATOR_CLASS_NAME)

        val componentCallKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.CALL_CLASS_NAME)

        val componentBiCallbackKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.BICALLBACK_CLASS_NAME)

        val androidIntentKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_INTENT)

        val androidContextKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_CONTEXT)

        val rxSingleKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.RXJAVA_SINGLE)

        val navigationDisposableKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.NAVIGATIONDISPOSABLE_CLASS_NAME)

        val rxCompletableKSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.RXJAVA_COMPLETABLE)

        val kotlinFunction0KSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.KOTLIN_FUNCTION0)

        val kotlinFunction1KSClassDeclaration: KSClassDeclaration? =
            resolver.getClassDeclarationByName(name = ComponentConstants.KOTLIN_FUNCTION1)

        // 生成的类要实现的接口的 String 全类名
        val fullClassName =
            routerApiKSClassDeclaration.asStarProjectedType().declaration.qualifiedName!!.asString()

        // 生成的类要实现的接口的 ClassName
        val targetRouterApiInterfaceClassName = fullClassName.toClassName()

        // 生成的目标类的 String className
        val targetClassSimpleName = fullClassName.simpleClassName() + ComponentUtil.UIROUTERAPI

        val typeSpec = TypeSpec
            .classBuilder(name = targetClassSimpleName)
            .addAnnotation(annotation = mClassNameAndroidKeepAnno)
            .addAnnotation(annotation = ComponentGeneratedAnno::class)
            .addSuperinterface(
                superinterface = targetRouterApiInterfaceClassName
            )
            .also { typeSpec ->
                routerApiKSClassDeclaration
                    .getDeclaredFunctions()
                    .forEach { ksFunctionDeclaration ->

                        // 导航的注解, 默认可以省略
                        val navigateAnno: NavigateAnno? =
                            ksFunctionDeclaration.getAnnotationsByType(
                                annotationKClass = NavigateAnno::class,
                            ).firstOrNull()

                        // SchemeAnno 信息
                        val schemeAnno = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = SchemeAnno::class,
                        ).firstOrNull() ?: defaultSchemeAnno

                        // userInfo 信息
                        val userInfoAnno = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = UserInfoAnno::class,
                        ).firstOrNull()

                        // 标记路由的 url
                        val urlAnno: UrlAnno? = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = UrlAnno::class,
                        ).firstOrNull()

                        // 标记路由的 host
                        val hostAnno: HostAnno? = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = HostAnno::class,
                        ).firstOrNull() ?: defaultHostAnno

                        // 标记路由的 path
                        val pathAnno: PathAnno? = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = PathAnno::class,
                        ).firstOrNull()

                        // 标记路由的地址
                        val hostAndPathAnno: HostAndPathAnno? =
                            ksFunctionDeclaration.getAnnotationsByType(
                                annotationKClass = HostAndPathAnno::class,
                            ).firstOrNull()

                        // 路由的类别
                        val categoryValueList =
                            defaultCategoryValueList + (ksFunctionDeclaration
                                .getAnnotationsByType(
                                    annotationKClass = CategoryAnno::class,
                                ).firstOrNull()?.value?.toList() ?: emptyList())

                        // 路由的 flag
                        val flagValueList =
                            defaultFlagValueList + (ksFunctionDeclaration
                                .getAnnotationsByType(
                                    annotationKClass = FlagAnno::class,
                                ).firstOrNull()?.value?.toList() ?: emptyList())

                        // 使用的拦截器
                        val useInterceptorAnno = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = UseInterceptorAnno::class,
                        ).firstOrNull()

                        // RequestCodeAnno 信息
                        val requestCodeAnno = ksFunctionDeclaration.getAnnotationsByType(
                            annotationKClass = RequestCodeAnno::class,
                        ).firstOrNull()

                        // 方法返回值类型的声明
                        val returnTypeKsDeclaration =
                            ksFunctionDeclaration.returnType?.resolve()?.declaration

                        // 是否是 suspend 函数
                        val isSuspendMethod =
                            ksFunctionDeclaration.modifiers.contains(element = Modifier.SUSPEND)

                        // 方法名字
                        val methodName: String = ksFunctionDeclaration.simpleName.getShortName()

                        // 是否返回 Rx 的 Single
                        val isSingleReturnType =
                            returnTypeKsDeclaration == rxSingleKSClassDeclaration

                        // 是否返回 Rx 的 Completable
                        val isCompletableReturnType =
                            returnTypeKsDeclaration == rxCompletableKSClassDeclaration

                        // 是否是返回 Rx
                        val isRxReturnType = isSingleReturnType || isCompletableReturnType

                        // 是否需要返回 ActivityResult
                        val isAndroidActivityResultReturnType =
                            returnTypeKsDeclaration == componentActivityResultKSClassDeclaration

                        // 是否需要返回 Intent
                        val isAndroidIntentReturnType =
                            returnTypeKsDeclaration == androidIntentKSClassDeclaration

                        // 是否返回 NavigationDisposable
                        val isComponentNavigationDisposableReturnType =
                            returnTypeKsDeclaration == navigationDisposableKSClassDeclaration

                        // 是否返回 Navigator
                        val isComponentNavigatorReturnType =
                            returnTypeKsDeclaration == componentNavigatorKSClassDeclaration

                        // 是否返回 Component 的 Call
                        val isComponentCallReturnType =
                            returnTypeKsDeclaration == componentCallKSClassDeclaration

                        // 是否需要返回值
                        /*val isNeedReturn =
                            isNavigationDisposableReturnType || isRxReturnType || isActivityResultReturnType || isIntentReturnType*/

                        val isNeedReturn = (navigateAnno?.let {
                            navigateAnno.forResult || navigateAnno.forIntent || navigateAnno.forResultCode
                        } ?: false) || isComponentNavigationDisposableReturnType
                                || isCompletableReturnType || isComponentNavigatorReturnType || isComponentCallReturnType

                        val navigatePrefixStr = if (isComponentNavigationDisposableReturnType) {
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
                                .builder(name = methodName)
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
                                            when (ksValueParameter.type.resolve().declaration) {
                                                androidContextKSClassDeclaration -> {
                                                    ksValueParameter_context = ksValueParameter
                                                }

                                                componentCallbackKSClassDeclaration -> {
                                                    ksValueParameter_callback = ksValueParameter
                                                }

                                                componentBiCallbackKSClassDeclaration -> {
                                                    ksValueParameter_biCallback = ksValueParameter
                                                }

                                                kotlinFunction0KSClassDeclaration -> {
                                                    ksValueParameter_kt_function0 = ksValueParameter
                                                }

                                                kotlinFunction1KSClassDeclaration -> {
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
                                                            message = "$TAG ksValueParameter = $ksValueParameter"
                                                        )
                                                    }
                                                    throw e
                                                },
                                            )
                                        }
                                }
                                .also { funSpecBuilder ->

                                    if (isSuspendMethod) {
                                        funSpecBuilder.addModifiers(
                                            KModifier.SUSPEND
                                        )
                                    }

                                    returnTypePoetTypeName?.let {
                                        if (logEnable) {
                                            logger.warn(message = "$TAG returnTypePoetTypeName = $returnTypePoetTypeName")
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
                                            element = ksValueParameter_context!!.name!!.asString(),
                                        )
                                    }

                                    // scheme userInfo host path 的处理
                                    run {
                                        schemeAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.scheme(scheme = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        userInfoAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.userInfo(userInfo = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        urlAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.url(url = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        hostAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.host(host = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        pathAnno?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.path(path = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        hostAndPathAnno?.let {
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
                                                        resolver = resolver,
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

                                    // requestCode category flag options 等处理
                                    run {

                                        // 如果没有参数, 就看看有没有标记方法上的注解
                                        if (ksValueParameter_requestCode == null) {
                                            requestCodeAnno?.let {
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

                                        useInterceptorAnno?.let { useInterceptorAnno ->

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

                                        navigateAnno?.forIntent == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    if (navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.%M(expectedResultCode = %L)",
                                                        )
                                                        functionArgList.add(
                                                            element = intentResultCodeMatchCallExtendMethodMemberName,
                                                        )
                                                        functionArgList.add(
                                                            element = navigateAnno.resultCodeMatch,
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

                                                isSuspendMethod -> {
                                                    if (navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.resultCodeMatchAndIntentAwait(expectedResultCode = %L)",
                                                        )
                                                        functionArgList.add(
                                                            element = navigateAnno.resultCodeMatch,
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.intentAwait()",
                                                        )
                                                    }
                                                }

                                                ksValueParameter_biCallback != null -> {
                                                    if (navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntentAndResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = navigateAnno.resultCodeMatch,
                                                        )
                                                        functionArgList.add(
                                                            element = ksValueParameter_biCallback!!.name!!.asString(),
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntent(callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = ksValueParameter_biCallback!!.name!!.asString(),
                                                        )
                                                    }
                                                }

                                                ksValueParameter_kt_function1 != null -> {
                                                    if (navigateAnno.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntentAndResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = navigateAnno.resultCodeMatch,
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

                                        navigateAnno?.forResult == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M()",
                                                    )
                                                    functionArgList.add(
                                                        element = activityResultCallExtendMethodMemberName,
                                                    )
                                                }

                                                isSuspendMethod -> {
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

                                        navigateAnno?.forResultCode == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M()",
                                                    )
                                                    functionArgList.add(
                                                        element = resultCodeCallExtendMethodMemberName,
                                                    )
                                                }

                                                isSuspendMethod -> {
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

                                        navigateAnno?.resultCodeMatchValid == true -> {

                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M(expectedResultCode = %L)",
                                                    )
                                                    functionArgList.add(
                                                        element = resultCodeMatchCallExtendMethodMemberName,
                                                    )
                                                    functionArgList.add(
                                                        element = navigateAnno.resultCodeMatch,
                                                    )
                                                }

                                                isSuspendMethod -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.resultCodeMatchAwait(expectedResultCode = %L)",
                                                    )
                                                    functionArgList.add(
                                                        element = navigateAnno.resultCodeMatch,
                                                    )
                                                }

                                                ksValueParameter_callback != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = navigateAnno.resultCodeMatch,
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_callback!!.name!!.asString(),
                                                    )
                                                }

                                                ksValueParameter_kt_function0 != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = navigateAnno.resultCodeMatch,
                                                    )
                                                    functionArgList.add(
                                                        element = ksValueParameter_kt_function0!!.name!!.asString(),
                                                    )
                                                }
                                            }

                                        }

                                        else -> {

                                            if (isComponentNavigatorReturnType || isComponentCallReturnType) {
                                                // 就是空的
                                            } else if (isSuspendMethod) {

                                                functionCodeStringBuffer.append(
                                                    "\n.await()",
                                                )

                                            } else if (isCompletableReturnType) {

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
                packageName = fullClassName.packageName(),
                fileName = targetClassSimpleName,
            )
            .addType(typeSpec = typeSpec)
            .build()

        try {
            routerApiKSClassDeclaration.containingFile
                ?.let { containingFile ->
                    codeGenerator.createNewFile(
                        // dependencies = Dependencies.ALL_FILES,
                        dependencies = Dependencies(
                            aggregating = true,
                            containingFile,
                        ),
                        packageName = fileSpec.packageName,
                        fileName = fileSpec.name,
                    ).use {
                        it.write(
                            fileSpec.toString().toByteArray()
                        )
                        it.flush()
                    }
                }
        } catch (e: Exception) {
            if (logEnable) {
                logger.warn(
                    "$TAG 生成文件失败啦~~~"
                )
                logger.exception(e = e)
            }
        }

    }

    override fun doProcess(resolver: Resolver): List<KSAnnotated> {

        val targetRouterApiAnnotatedList = resolver
            .getSymbolsWithAnnotation(
                annotationName = RouterApiAnno::class.qualifiedName!!
            )
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (logEnable) {
            logger.warn(
                "$TAG targetList = $targetRouterApiAnnotatedList"
            )
        }

        targetRouterApiAnnotatedList.forEach { item ->
            createFile(
                resolver = resolver,
                routerApiKSClassDeclaration = item,
            )
        }

        return emptyList()

    }

    override fun finish() {
        super.finish()
        if (logEnable) {
            logger.warn("$TAG finish")
        }
    }

    override fun onError() {
        super.onError()
        if (logEnable) {
            logger.warn("$TAG onError")
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