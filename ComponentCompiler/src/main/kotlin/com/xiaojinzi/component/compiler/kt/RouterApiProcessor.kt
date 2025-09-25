package com.xiaojinzi.component.compiler.kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KSTypesNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.ComponentUtil
import com.xiaojinzi.component.anno.router.AfterRouteActionAnno
import com.xiaojinzi.component.anno.router.AfterRouteErrorActionAnno
import com.xiaojinzi.component.anno.router.AfterRouteEventActionAnno
import com.xiaojinzi.component.anno.router.AfterStartActivityActionAnno
import com.xiaojinzi.component.anno.router.BeforeRouteSuccessActionAnno
import com.xiaojinzi.component.anno.router.BeforeStartActivityActionAnno
import com.xiaojinzi.component.anno.router.CategoryAnno
import com.xiaojinzi.component.anno.router.CheckRepeatAnno
import com.xiaojinzi.component.anno.router.FlagAnno
import com.xiaojinzi.component.anno.router.HostAndPathAnno
import com.xiaojinzi.component.anno.router.HostAnno
import com.xiaojinzi.component.anno.router.NavigateAnno
import com.xiaojinzi.component.anno.router.OptionsAnno
import com.xiaojinzi.component.anno.router.ParameterAnno
import com.xiaojinzi.component.anno.router.ParameterBundleAnno
import com.xiaojinzi.component.anno.router.PathAnno
import com.xiaojinzi.component.anno.router.RequestCodeAnno
import com.xiaojinzi.component.anno.router.RouterApiAnno
import com.xiaojinzi.component.anno.router.SchemeAnno
import com.xiaojinzi.component.anno.router.UrlAnno
import com.xiaojinzi.component.anno.router.UseInterceptorAnno
import com.xiaojinzi.component.anno.router.UserInfoAnno
import com.xiaojinzi.component.anno.support.ComponentGeneratedAnno
import com.xiaojinzi.component.packageName
import com.xiaojinzi.component.simpleClassName

private data class UserInfoAnnoInfo(
    val value: String,
)

private data class UrlAnnoInfo(
    val value: String,
)

private data class SchemeAnnoInfo(
    val value: String,
)

private data class HostAnnoInfo(
    val value: String,
)

private data class PathAnnoInfo(
    val value: String,
)

private data class HostAndPathAnnoInfo(
    val value: String,
)

private data class UseInterceptorAnnoInfo(
    val classesClassPathList: List<String>,
    val names: List<String>,
)

private data class RequestCodeAnnoInfo(
    val value: Int,
)

private data class CheckRepeatAnnoInfo(
    val value: Boolean,
)

private data class NavigateAnnoInfo(
    val forResult: Boolean,
    val forResultCode: Boolean,
    val forIntent: Boolean,
    val resultCodeMatch: Int,
) {
    val resultCodeMatchValid: Boolean
        get() = resultCodeMatch != Int.MIN_VALUE
}

private fun UserInfoAnno.toUserInfoAnnoInfo() = UserInfoAnnoInfo(value = this.value)

private fun UrlAnno.toUrlAnnoInfo() = UrlAnnoInfo(value = this.value)

private fun SchemeAnno.toSchemeAnnoInfo() = SchemeAnnoInfo(value = this.value)

private fun HostAnno.toHostAnnoInfo() = HostAnnoInfo(value = this.value)

private fun PathAnno.toPathAnnoInfo() = PathAnnoInfo(value = this.value)

private fun HostAndPathAnno.toHostAndPathAnnoInfo() = HostAndPathAnnoInfo(value = this.value)

private fun UseInterceptorAnno.toUseInterceptorAnnoInfo() = UseInterceptorAnnoInfo(
    classesClassPathList = this.classesClassPathList,
    names = this.names.toList(),
)

private fun RequestCodeAnno.toRequestCodeAnnoInfo() = RequestCodeAnnoInfo(
    value = this.value,
)

private fun CheckRepeatAnno.toCheckRepeatAnnoInfo() = CheckRepeatAnnoInfo(
    value = this.value,
)

private data class RouterApiInfo(
    val containingFile: KSFile?,
    // 生成的类要实现的接口的 String 全类名
    val fullClassName: String,
    // 生成的类要实现的接口的 ClassName
    val targetRouterApiInterfaceClassName: ClassName = fullClassName.toClassName(),
    // 默认的 SchemeAnno
    val defaultSchemeAnnoInfo: SchemeAnnoInfo?,
    // 默认的 HostAnno
    val defaultHostAnnoInfo: HostAnnoInfo?,
    // 默认的 CategoryAnno 的值
    val defaultCategoryValueList: List<String>,
    // 默认的 FlagAnno 的值
    val defaultFlagValueList: List<Int>,
    val functionInfoList: List<FunctionInfo>,
) {

    data class FunctionInfo(
        // 导航的注解, 默认可以省略
        val navigateAnnoInfo: NavigateAnnoInfo?,
        // SchemeAnno 信息
        val schemeAnnoInfo: SchemeAnnoInfo?,
        // userInfo 信息
        val userInfoAnnoInfo: UserInfoAnnoInfo?,
        // 标记路由的 url
        val urlAnnoInfo: UrlAnnoInfo?,
        // 标记路由的 host
        val hostAnnoInfo: HostAnnoInfo?,
        // 标记路由的 path
        val pathAnnoInfo: PathAnnoInfo?,
        // 标记路由的地址
        val hostAndPathAnnoInfo: HostAndPathAnnoInfo?,
        val categoryValueList: List<String>,
        val flagValueList: List<Int>,
        // 使用的拦截器
        val useInterceptorAnnoInfo: UseInterceptorAnnoInfo?,
        // RequestCodeAnno 信息
        val requestCodeAnnoInfo: RequestCodeAnnoInfo?,
        val checkRepeatAnnoInfo: CheckRepeatAnnoInfo?,
        // 方法返回值类型的声明
        val returnTypePoetTypeName: TypeName?,
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
        // 方法所有参数的信息
        val parameterInfoList: List<ParameterInfo>,
        val optionsParameterInfo: ParameterInfo?,
        val beforeActionParameterInfo: ParameterInfo?,
        val beforeStartActionParameterInfo: ParameterInfo?,
        val afterActionParameterInfo: ParameterInfo?,
        val afterErrorParameterInfo: ParameterInfo?,
        val afterEventParameterInfo: ParameterInfo?,
        val afterStartParameterInfo: ParameterInfo?,
        val requestCodeParameterInfo: ParameterInfo?,
        val bundleParameterInfo: ParameterInfo?,
        val contextParameterInfo: ParameterInfo?,
        val callbackParameterInfo: ParameterInfo?,
        val biCallbackParameterInfo: ParameterInfo?,
        val ktFunction0ParameterInfo: ParameterInfo?,
        val ktFunction1ParameterInfo: ParameterInfo?,
    ) {

        data class ParameterInfo(
            val name: String,
            val typeName: TypeName,
            val parameterAnno: ParameterAnno?,
            val methodCallName: String?,
        )

    }

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
        val targetClassSimpleName =
            routerApiInfo.fullClassName.simpleClassName() + ComponentUtil.UIROUTERAPI

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
                        val isRxReturnType =
                            functionInfo.isSingleReturnType || functionInfo.isCompletableReturnType

                        val isNeedReturn = (functionInfo.navigateAnnoInfo?.let {
                            functionInfo.navigateAnnoInfo.forResult || functionInfo.navigateAnnoInfo.forIntent || functionInfo.navigateAnnoInfo.forResultCode
                        } ?: false) || functionInfo.isComponentNavigationDisposableReturnType
                                || functionInfo.isCompletableReturnType || functionInfo.isComponentNavigatorReturnType || functionInfo.isComponentCallReturnType

                        val navigatePrefixStr =
                            if (functionInfo.isComponentNavigationDisposableReturnType) {
                                "navigate"
                            } else {
                                "forward"
                            }

                        val ksValueParameter_bundle = functionInfo.bundleParameterInfo

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
                                    functionInfo.parameterInfoList.forEach { parameterInfo ->

                                        /*when {
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
                                        }*/

                                        this.addParameter(
                                            name = parameterInfo.name,
                                            type = try {
                                                parameterInfo.typeName
                                            } catch (e: Exception) {
                                                if (logEnable) {
                                                    logger.warn(
                                                        message = "$TAG $componentModuleName parameterInfo = $parameterInfo"
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

                                    functionInfo.returnTypePoetTypeName?.let {
                                        if (logEnable) {
                                            logger.warn(message = "$TAG $componentModuleName returnTypePoetTypeName = ${functionInfo.returnTypePoetTypeName}")
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

                                    if (functionInfo.contextParameterInfo == null) {
                                        functionCodeStringBuffer.append(
                                            ")",
                                        )
                                    } else {
                                        functionCodeStringBuffer.append(
                                            "context = %N)",
                                        )
                                        functionArgList.add(
                                            element = functionInfo.contextParameterInfo.name,
                                        )
                                    }

                                    // scheme userInfo host path 的处理
                                    run {
                                        functionInfo.schemeAnnoInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.scheme(scheme = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.userInfoAnnoInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.userInfo(userInfo = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.urlAnnoInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.url(url = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.hostAnnoInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.host(host = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.pathAnnoInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.path(path = %S)",
                                            )
                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }
                                        functionInfo.hostAndPathAnnoInfo?.let {
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
                                        functionInfo.parameterInfoList.forEach { parameterInfo ->
                                            parameterInfo.parameterAnno?.let { parameterAnno ->
                                                functionCodeStringBuffer.append(
                                                    "\n.${parameterInfo.methodCallName}(key = %S, value = %N)",
                                                )
                                                functionArgList.add(
                                                    element = parameterAnno.value.ifEmpty {
                                                        parameterInfo.name
                                                    },
                                                )
                                                functionArgList.add(
                                                    element = parameterInfo.name,
                                                )
                                            }
                                        }

                                        // Bundle 参数处理
                                        functionInfo.bundleParameterInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.putAll(bundle = %N)",
                                            )
                                            functionArgList.add(
                                                element = it.name,
                                            )
                                        }

                                    }

                                    // routeRepeatCheck requestCode category flag options 等处理
                                    run {

                                        functionInfo.checkRepeatAnnoInfo?.let {
                                            functionCodeStringBuffer.append(
                                                "\n.useRouteRepeatCheck(useRouteRepeatCheck = %L)",
                                            )

                                            functionArgList.add(
                                                element = it.value,
                                            )
                                        }

                                        // 如果没有参数, 就看看有没有标记方法上的注解
                                        if (functionInfo.requestCodeParameterInfo == null) {
                                            functionInfo.requestCodeAnnoInfo?.let {
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
                                            functionInfo.requestCodeParameterInfo.let {
                                                functionCodeStringBuffer.append(
                                                    "\n.requestCode(requestCode = %N)",
                                                )

                                                functionArgList.add(
                                                    element = it.name,
                                                )
                                            }
                                        }

                                        functionInfo.optionsParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.options(options = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name,
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

                                        functionInfo.useInterceptorAnnoInfo?.let { useInterceptorAnno ->

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

                                        functionInfo.beforeActionParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.beforeRouteAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name
                                            )

                                        }

                                        functionInfo.beforeStartActionParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.beforeStartActivityAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name
                                            )

                                        }

                                        functionInfo.afterActionParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterRouteSuccessAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name
                                            )

                                        }

                                        functionInfo.afterErrorParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterRouteErrorAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name
                                            )

                                        }

                                        functionInfo.afterEventParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterRouteEventAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name
                                            )

                                        }

                                        functionInfo.afterStartParameterInfo?.let {

                                            functionCodeStringBuffer.append(
                                                "\n.afterStartActivityAction(action = %N)",
                                            )

                                            functionArgList.add(
                                                element = it.name
                                            )

                                        }

                                    }

                                    // 结尾方法的处理
                                    when {

                                        functionInfo.navigateAnnoInfo?.forIntent == true -> {
                                            when {
                                                isRxReturnType -> {
                                                    if (functionInfo.navigateAnnoInfo.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.%M(expectedResultCode = %L)",
                                                        )
                                                        functionArgList.add(
                                                            element = intentResultCodeMatchCallExtendMethodMemberName,
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnnoInfo.resultCodeMatch,
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
                                                    if (functionInfo.navigateAnnoInfo.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.resultCodeMatchAndIntentAwait(expectedResultCode = %L)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.intentAwait()",
                                                        )
                                                    }
                                                }

                                                functionInfo.biCallbackParameterInfo != null -> {
                                                    if (functionInfo.navigateAnnoInfo.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntentAndResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.biCallbackParameterInfo.name,
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntent(callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.biCallbackParameterInfo.name,
                                                        )
                                                    }
                                                }

                                                functionInfo.ktFunction1ParameterInfo != null -> {
                                                    if (functionInfo.navigateAnnoInfo.resultCodeMatchValid) {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntentAndResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.ktFunction1ParameterInfo.name,
                                                        )
                                                    } else {
                                                        functionCodeStringBuffer.append(
                                                            "\n.${navigatePrefixStr}ForIntent(callback = %N)",
                                                        )
                                                        functionArgList.add(
                                                            element = functionInfo.ktFunction1ParameterInfo.name,
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        functionInfo.navigateAnnoInfo?.forResult == true -> {
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

                                                functionInfo.biCallbackParameterInfo != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResult(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.biCallbackParameterInfo.name,
                                                    )
                                                }

                                                functionInfo.ktFunction1ParameterInfo != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResult(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.ktFunction1ParameterInfo.name,
                                                    )
                                                }
                                            }
                                        }

                                        functionInfo.navigateAnnoInfo?.forResultCode == true -> {
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

                                                functionInfo.biCallbackParameterInfo != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCode(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.biCallbackParameterInfo.name,
                                                    )
                                                }

                                                functionInfo.ktFunction1ParameterInfo != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCode(callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.ktFunction1ParameterInfo.name,
                                                    )
                                                }
                                            }
                                        }

                                        functionInfo.navigateAnnoInfo?.resultCodeMatchValid == true -> {

                                            when {
                                                isRxReturnType -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.%M(expectedResultCode = %L)",
                                                    )
                                                    functionArgList.add(
                                                        element = resultCodeMatchCallExtendMethodMemberName,
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                    )
                                                }

                                                functionInfo.isSuspendMethod -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.resultCodeMatchAwait(expectedResultCode = %L)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                    )
                                                }

                                                functionInfo.callbackParameterInfo != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.callbackParameterInfo.name,
                                                    )
                                                }

                                                functionInfo.ktFunction0ParameterInfo != null -> {
                                                    functionCodeStringBuffer.append(
                                                        "\n.${navigatePrefixStr}ForResultCodeMatch(expectedResultCode = %L, callback = %N)",
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.navigateAnnoInfo.resultCodeMatch,
                                                    )
                                                    functionArgList.add(
                                                        element = functionInfo.ktFunction0ParameterInfo.name,
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

                                                functionInfo.callbackParameterInfo?.let {
                                                    functionCodeStringBuffer.append(
                                                        "callback = %N",
                                                    )
                                                    functionArgList.add(
                                                        element = it.name,
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
            routerApiInfo
                .containingFile
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
    private fun KSValueParameter.toParameterInfo() = RouterApiInfo.FunctionInfo.ParameterInfo(
        name = this.name!!.asString(),
        typeName = this.typeToClassName(),
        parameterAnno = this.getAnnotationsByType(
            annotationKClass = ParameterAnno::class,
        ).firstOrNull(),
        methodCallName = runCatching {
            getMethodNameFromKsType(
                ksType = this.type.resolve(),
                prefix = "put",
            )
        }.getOrNull(),
    )

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
                        containingFile = classItem.containingFile,
                        fullClassName = classItem.asStarProjectedType().declaration.qualifiedName!!.asString(),
                        defaultSchemeAnnoInfo = defaultSchemeAnno?.toSchemeAnnoInfo(),
                        defaultHostAnnoInfo = defaultHostAnno?.toHostAnnoInfo(),
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
                                val navigateAnnoInfo = navigateAnno?.let { anno ->
                                    NavigateAnnoInfo(
                                        forResult = anno.forResult,
                                        forResultCode = anno.forResultCode,
                                        forIntent = anno.forIntent,
                                        resultCodeMatch = anno.resultCodeMatch,
                                    )
                                }
                                val returnTypeKsDeclaration = ksFunctionDeclaration
                                    .returnType
                                    ?.resolve()
                                    ?.declaration

                                var ksValueParameter_options: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_beforeAction: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_beforeStartAction: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_afterAction: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_afterError: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_afterEvent: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_afterStart: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_requestCode: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_bundle: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_context: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_callback: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_biCallback: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_kt_function0: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null
                                var ksValueParameter_kt_function1: RouterApiInfo.FunctionInfo.ParameterInfo? =
                                    null

                                val parameterInfoList = ksFunctionDeclaration
                                    .parameters
                                    .onEach { ksValueParameter ->
                                        when {
                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = OptionsAnno::class
                                            ) -> {
                                                ksValueParameter_options =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = BeforeRouteSuccessActionAnno::class
                                            ) -> {
                                                ksValueParameter_beforeAction =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = BeforeStartActivityActionAnno::class
                                            ) -> {
                                                ksValueParameter_beforeStartAction =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = AfterRouteActionAnno::class
                                            ) -> {
                                                ksValueParameter_afterAction =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = AfterRouteErrorActionAnno::class
                                            ) -> {
                                                ksValueParameter_afterError =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = AfterRouteEventActionAnno::class
                                            ) -> {
                                                ksValueParameter_afterEvent =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = AfterStartActivityActionAnno::class
                                            ) -> {
                                                ksValueParameter_afterStart =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = RequestCodeAnno::class
                                            ) -> {
                                                ksValueParameter_requestCode =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            ksValueParameter.isAnnotationPresent(
                                                annotationKClass = ParameterBundleAnno::class
                                            ) -> {
                                                ksValueParameter_bundle =
                                                    ksValueParameter.toParameterInfo()
                                            }
                                        }
                                        when (ksValueParameter.type.resolve().declaration.qualifiedName) {
                                            androidContextKSClassDeclaration?.qualifiedName -> {
                                                ksValueParameter_context =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            componentCallbackKSClassDeclaration?.qualifiedName -> {
                                                ksValueParameter_callback =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            componentBiCallbackKSClassDeclaration?.qualifiedName -> {
                                                ksValueParameter_biCallback =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            kotlinFunction0KSClassDeclaration?.qualifiedName -> {
                                                ksValueParameter_kt_function0 =
                                                    ksValueParameter.toParameterInfo()
                                            }

                                            kotlinFunction1KSClassDeclaration?.qualifiedName -> {
                                                ksValueParameter_kt_function1 =
                                                    ksValueParameter.toParameterInfo()
                                            }
                                        }
                                    }
                                    .map { ksValueParameter ->
                                        ksValueParameter.toParameterInfo()
                                    }
                                RouterApiInfo.FunctionInfo(
                                    navigateAnnoInfo = navigateAnnoInfo,
                                    schemeAnnoInfo = (ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = SchemeAnno::class,
                                    ).firstOrNull() ?: defaultSchemeAnno)?.toSchemeAnnoInfo(),
                                    userInfoAnnoInfo = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = UserInfoAnno::class,
                                    ).firstOrNull()?.toUserInfoAnnoInfo(),
                                    urlAnnoInfo = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = UrlAnno::class,
                                    ).firstOrNull()?.toUrlAnnoInfo(),
                                    hostAnnoInfo = (ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = HostAnno::class,
                                    ).firstOrNull() ?: defaultHostAnno)?.toHostAnnoInfo(),
                                    pathAnnoInfo = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = PathAnno::class,
                                    ).firstOrNull()?.toPathAnnoInfo(),
                                    hostAndPathAnnoInfo = ksFunctionDeclaration.getAnnotationsByType(
                                        annotationKClass = HostAndPathAnno::class,
                                    ).firstOrNull()?.toHostAndPathAnnoInfo(),
                                    categoryValueList = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = CategoryAnno::class,
                                        ).firstOrNull()?.value?.toList() ?: emptyList(),
                                    flagValueList = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = FlagAnno::class,
                                        ).firstOrNull()?.value?.toList() ?: emptyList(),
                                    useInterceptorAnnoInfo = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = UseInterceptorAnno::class,
                                        ).firstOrNull()?.toUseInterceptorAnnoInfo(),
                                    requestCodeAnnoInfo = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = RequestCodeAnno::class,
                                        ).firstOrNull()?.toRequestCodeAnnoInfo(),
                                    checkRepeatAnnoInfo = ksFunctionDeclaration
                                        .getAnnotationsByType(
                                            annotationKClass = CheckRepeatAnno::class,
                                        ).firstOrNull()?.toCheckRepeatAnnoInfo(),
                                    returnTypePoetTypeName = ksFunctionDeclaration.returnTypeToTypeName(),
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
                                    parameterInfoList = parameterInfoList,
                                    optionsParameterInfo = ksValueParameter_options,
                                    beforeActionParameterInfo = ksValueParameter_beforeAction,
                                    beforeStartActionParameterInfo = ksValueParameter_beforeStartAction,
                                    afterActionParameterInfo = ksValueParameter_afterAction,
                                    afterErrorParameterInfo = ksValueParameter_afterError,
                                    afterEventParameterInfo = ksValueParameter_afterEvent,
                                    afterStartParameterInfo = ksValueParameter_afterStart,
                                    requestCodeParameterInfo = ksValueParameter_requestCode,
                                    bundleParameterInfo = ksValueParameter_bundle,
                                    contextParameterInfo = ksValueParameter_context,
                                    callbackParameterInfo = ksValueParameter_callback,
                                    biCallbackParameterInfo = ksValueParameter_biCallback,
                                    ktFunction0ParameterInfo = ksValueParameter_kt_function0,
                                    ktFunction1ParameterInfo = ksValueParameter_kt_function1,
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