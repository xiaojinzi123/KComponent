package com.xiaojinzi.component.compiler.kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.anno.AttrValueAutowiredAnno
import com.xiaojinzi.component.anno.ServiceAutowiredAnno
import com.xiaojinzi.component.anno.UriAutowiredAnno
import com.xiaojinzi.component.anno.support.ComponentGeneratedAnno
import com.xiaojinzi.component.support.AttrAutoWireMode

private data object UriAutowiredAnnoInfo

/**
 * see [AttrValueAutowiredAnno]
 */
private data class AttrValueAutowiredAnnoInfo(
    val value: List<String>,
    val mode: AttrAutoWireMode,
)

/**
 * see [ServiceAutowiredAnno]
 */
private data class ServiceAutowiredAnnoInfo(
    val name: String,
)

/**
 * 一个 [InjectFileInfo] 对象表示一个要生成注入类
 */
private data class InjectFileInfo(
    val targetClassContainingFile: KSFile,
    val targetClassClassName: ClassName,
    val isSubActivity: Boolean,
    val isSubFragment: Boolean,
) //
{

    data class PropertyInfo(
        val isPropertyLateInit: Boolean,
        val isPropertyNullable: Boolean,
        val propertyName: String,
        val propertyClassName: ClassName,
        val propertyGetMethodName: String?,
        val uriAutowiredAnnoInfo: UriAutowiredAnnoInfo?,
        val attrValueAutowiredAnnoInfo: AttrValueAutowiredAnnoInfo?,
        val serviceAutowiredAnnoInfo: ServiceAutowiredAnnoInfo?,
    ) {

        override fun hashCode(): Int {
            var result = isPropertyLateInit.hashCode()
            result = 31 * result + isPropertyNullable.hashCode()
            result = 31 * result + propertyName.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PropertyInfo) return false

            if (isPropertyLateInit != other.isPropertyLateInit) return false
            if (isPropertyNullable != other.isPropertyNullable) return false
            if (propertyName != other.propertyName) return false
            return true
        }

    }

}

private class AutowireProcessor(
    override val environment: SymbolProcessorEnvironment,
) : BaseProcessor(
    environment = environment,
) {

    companion object {
        const val TAG = "AutowireProcessor"
    }

    private val collectInfoMap = mutableMapOf<InjectFileInfo, Set<InjectFileInfo.PropertyInfo>>()

    @OptIn(KspExperimental::class)
    private fun createFile(
        injectFileInfo: InjectFileInfo,
        propertyInfoSet: Set<InjectFileInfo.PropertyInfo>,
    ) {

        if (injectFileInfo.isSubActivity.not() && injectFileInfo.isSubFragment.not()) {
            return
        }

        // 目标注入的 class 对象
        val targetClassClassName = injectFileInfo.targetClassClassName
        // Inject 接口
        val injectClassName = ComponentConstants.INJECT_CLASS_NAME.toClassName()
        // 属性注入的模式
        val attrAutoWireModeClassName = ComponentConstants.ATTRAUTOWIREMODE_CLASS_NAME.toClassName()

        val classSimpleName = targetClassClassName.simpleName + ComponentConstants.INJECT_SUFFIX

        val typeSpec = TypeSpec
            .classBuilder(classSimpleName)
            .addAnnotation(annotation = mClassNameAndroidKeepAnno)
            .addAnnotation(annotation = ComponentGeneratedAnno::class)
            .addSuperinterface(
                superinterface = injectClassName.parameterizedBy(
                    targetClassClassName,
                )
            )
            .addFunction(
                funSpec = FunSpec
                    .builder("injectAttrValue")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        name = "target",
                        type = targetClassClassName,
                    )
                    .also {
                        when {
                            injectFileInfo.isSubActivity -> {
                                it.addStatement("this.injectAttrValue(target = target, bundle = target.intent?.extras?: Bundle())")
                            }

                            injectFileInfo.isSubFragment -> {
                                it.addStatement("this.injectAttrValue(target = target, bundle = target.arguments?: Bundle())")
                            }
                        }
                    }
                    .build()
            )
            .addFunction(
                funSpec = FunSpec
                    .builder("injectAttrValue")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        name = "target",
                        type = targetClassClassName,
                    )
                    .addParameter(
                        name = "bundle",
                        type = mClassNameAndroidBundle,
                    )
                    .addComment("App 默认的模式")
                    .addStatement(
                        format = "val defaultMode = %T.requiredConfig().attrAutoWireMode",
                        mClassNameComponent,
                    )
                    .also { funSpec ->

                        propertyInfoSet.forEach { propertyInfo ->

                            if (propertyInfo.uriAutowiredAnnoInfo != null) {
                                if (logEnable) {
                                    logger.warn(
                                        message = "$TAG $componentModuleName uriAutowiredAnnoInfo = ${propertyInfo.uriAutowiredAnnoInfo}"
                                    )
                                }
                            }

                            if (propertyInfo.attrValueAutowiredAnnoInfo != null) {
                                if (logEnable) {
                                    logger.warn(
                                        message = "$TAG $componentModuleName attrValueAutowiredAnnoInfo = ${propertyInfo.attrValueAutowiredAnnoInfo}"
                                    )
                                }
                            }

                            val defaultModel: (funSpec: FunSpec.Builder, attrAutoWireAnnoItemName: String) -> Unit =
                                { funSpec, attrAutoWireAnnoItemName ->
                                    funSpec.addStatement(
                                        format = "target.%N = %T.%N(bundle = bundle, key = %S)?: target.%N",
                                        propertyInfo.propertyName,
                                        mClassNameParameterSupport,
                                        checkNotNull(
                                            value = propertyInfo.propertyGetMethodName,
                                        ),
                                        attrAutoWireAnnoItemName,
                                        propertyInfo.propertyName,
                                    )
                                }

                            val overrideModel: (funSpec: FunSpec.Builder, attrAutoWireAnnoItemName: String, isNullable: Boolean) -> Unit =
                                { funSpec, attrAutoWireAnnoItemName, isNullable ->
                                    funSpec.addStatement(
                                        format = "target.%N = %T.%N(bundle = bundle, key = %S)${if (isNullable) "" else "!!"}",
                                        propertyInfo.propertyName,
                                        mClassNameParameterSupport,
                                        checkNotNull(
                                            value = propertyInfo.propertyGetMethodName,
                                        ),
                                        attrAutoWireAnnoItemName,
                                    )
                                }

                            propertyInfo.uriAutowiredAnnoInfo?.let {

                                funSpec.addStatement(
                                    format = "target.%N = %T.getUri(bundle = bundle)${if (propertyInfo.isPropertyLateInit || propertyInfo.isPropertyNullable) "!!" else ""}",
                                    propertyInfo.propertyName,
                                    mClassNameParameterSupport,
                                )

                            }

                            propertyInfo.attrValueAutowiredAnnoInfo?.let { attrAutoWireAnno ->

                                val oneNameOfPropertyCall: (Int, String) -> Unit =
                                    { index, attrAutoWireAnnoItemName ->

                                        if (index == 0) {
                                            funSpec.beginControlFlow(
                                                controlFlow = "if(%T.containsKey(bundle = bundle, key = %S))",
                                                mClassNameParameterSupport,
                                                attrAutoWireAnnoItemName,
                                            )
                                        } else {
                                            funSpec.beginControlFlow(
                                                controlFlow = "else if(%T.containsKey(bundle = bundle, key = %S))",
                                                mClassNameParameterSupport,
                                                attrAutoWireAnnoItemName,
                                            )
                                        }

                                        if (propertyInfo.isPropertyLateInit) {
                                            overrideModel.invoke(
                                                funSpec,
                                                attrAutoWireAnnoItemName,
                                                propertyInfo.isPropertyNullable,
                                            )
                                        } else {
                                            when (attrAutoWireAnno.mode) {
                                                AttrAutoWireMode.Unspecified -> {
                                                    funSpec.beginControlFlow(
                                                        controlFlow = "when(defaultMode)",
                                                    )
                                                    run {
                                                        run {
                                                            funSpec.addStatement(
                                                                format = "%T.Default ->",
                                                                attrAutoWireModeClassName,
                                                            )
                                                            funSpec.beginControlFlow(
                                                                controlFlow = "",
                                                            )

                                                            defaultModel.invoke(
                                                                funSpec, attrAutoWireAnnoItemName,
                                                            )

                                                            funSpec.endControlFlow()
                                                        }

                                                        run {
                                                            funSpec.addStatement(
                                                                format = "%T.Override ->",
                                                                attrAutoWireModeClassName,
                                                            )
                                                            funSpec.beginControlFlow(
                                                                controlFlow = "",
                                                            )
                                                            overrideModel.invoke(
                                                                funSpec,
                                                                attrAutoWireAnnoItemName,
                                                                propertyInfo.isPropertyNullable,
                                                            )
                                                            funSpec.endControlFlow()
                                                        }

                                                        funSpec.addStatement(
                                                            format = "else -> {}",
                                                        )

                                                    }
                                                    funSpec.endControlFlow()
                                                }

                                                AttrAutoWireMode.Default -> {
                                                    defaultModel.invoke(
                                                        funSpec, attrAutoWireAnnoItemName,
                                                    )
                                                }

                                                AttrAutoWireMode.Override -> {
                                                    overrideModel.invoke(
                                                        funSpec,
                                                        attrAutoWireAnnoItemName,
                                                        propertyInfo.isPropertyNullable,
                                                    )
                                                }
                                            }
                                        }
                                        funSpec.endControlFlow()
                                    }

                                if (
                                    runCatching {
                                        attrAutoWireAnno.value
                                    }.getOrNull().isNullOrEmpty()
                                ) {
                                    oneNameOfPropertyCall.invoke(0, propertyInfo.propertyName)
                                } else {
                                    attrAutoWireAnno.value.forEachIndexed { index, attrAutoWireAnnoItemName ->
                                        oneNameOfPropertyCall.invoke(
                                            index,
                                            attrAutoWireAnnoItemName
                                        )
                                    }
                                }

                            }

                            propertyInfo.serviceAutowiredAnnoInfo?.let {

                                funSpec.addStatement(
                                    format = "target.%N = %T.%N(tClass = %T::class)",
                                    propertyInfo.propertyName,
                                    mClassNameServiceManager,
                                    if (propertyInfo.isPropertyNullable) {
                                        "get"
                                    } else {
                                        "requiredGet"
                                    },
                                    propertyInfo.propertyClassName,
                                )

                            }
                        }

                    }
                    .build()
            )
            .addFunction(
                funSpec = FunSpec
                    .builder("injectService")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        name = "target",
                        type = targetClassClassName,
                    )
                    .build()
            )
            .build()

        val fileSpec = FileSpec
            .builder(
                packageName = targetClassClassName.packageName,
                fileName = classSimpleName,
            )
            .addType(typeSpec = typeSpec)
            .build()

        try {
            if (logEnable) {
                logger.warn(
                    message = "$TAG $componentModuleName $componentModuleName, isSubFragmentActivity = ${injectFileInfo.isSubActivity}, isSubFragment = ${injectFileInfo.isSubFragment}",
                )
            }
            val targetDataArray = fileSpec.toString().toByteArray()
            codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = false,
                    injectFileInfo.targetClassContainingFile,
                ),
                packageName = fileSpec.packageName,
                fileName = fileSpec.name,
            ).use {
                it.write(
                    targetDataArray
                )
            }
            if (logEnable) {
                logger.warn(
                    message = "$TAG $componentModuleName, isSubFragmentActivity = ${injectFileInfo.isSubActivity}, isSubFragment = ${injectFileInfo.isSubFragment}",
                )
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    @OptIn(KspExperimental::class)
    private fun KSPropertyDeclaration.convertToPropertyInfo(
    ): InjectFileInfo.PropertyInfo? {
        val ksPropertyDeclaration = this
        val propertyType = ksPropertyDeclaration.type.resolve()
        return InjectFileInfo.PropertyInfo(
            isPropertyLateInit = ksPropertyDeclaration.modifiers.contains(element = Modifier.LATEINIT),
            isPropertyNullable =
                ksPropertyDeclaration.type.resolve().isMarkedNullable,
            propertyName = ksPropertyDeclaration.simpleName.asString(),
            propertyClassName = propertyType.toClassName(),
            propertyGetMethodName = runCatching {
                getMethodNameFromKsType(
                    ksType = propertyType,
                    prefix = "get",
                )
            }.getOrNull(),
            uriAutowiredAnnoInfo = ksPropertyDeclaration.getAnnotationsByType(
                annotationKClass = UriAutowiredAnno::class
            ).firstOrNull()?.let {
                UriAutowiredAnnoInfo
            },
            attrValueAutowiredAnnoInfo = ksPropertyDeclaration.getAnnotationsByType(
                annotationKClass = AttrValueAutowiredAnno::class
            ).firstOrNull()?.let { anno ->
                AttrValueAutowiredAnnoInfo(
                    value = anno.value.toList(),
                    mode = anno.mode,
                )
            },
            serviceAutowiredAnnoInfo = ksPropertyDeclaration.getAnnotationsByType(
                annotationKClass = ServiceAutowiredAnno::class
            ).firstOrNull()?.let { anno ->
                ServiceAutowiredAnnoInfo(
                    name = anno.name,
                )
            },
        )
    }

    private fun KSClassDeclaration.convertToInjectFileInfo(
    ): InjectFileInfo? {
        this.qualifiedName?.getQualifier()
        val containingFile = this.containingFile ?: return null
        val classDeclarationKsType = this.asStarProjectedType()
        return InjectFileInfo(
            targetClassContainingFile = containingFile,
            targetClassClassName = classDeclarationKsType.toClassName(),
            isSubActivity = activityKsClassDeclaration
                .asStarProjectedType()
                .isAssignableFrom(that = classDeclarationKsType),
            isSubFragment = fragmentKsClassDeclaration
                .asStarProjectedType()
                .isAssignableFrom(that = classDeclarationKsType),
        )
    }

    private fun createAllFile() {
        collectInfoMap
            .forEach { (injectFileInfo, propertyInfoSet) ->
                createFile(
                    injectFileInfo = injectFileInfo,
                    propertyInfoSet = propertyInfoSet,
                )
            }
    }

    override fun roundProcess(
        resolver: Resolver,
        round: Int,
    ): List<KSAnnotated> {

        val (uriAutoWireValidList, uriAutoWireInValidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = UriAutowiredAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        val (attrValueAutowiredValidList, attrValueAutowiredInValidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = AttrValueAutowiredAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        val (serviceAutowiredValidList, serviceAutowiredInValidList) = resolver
            .getSymbolsWithAnnotation(
                annotationName = ServiceAutowiredAnno::class.qualifiedName!!
            )
            .partition { !validateEnable || it.validate() }

        val uriAutoWireAnnotatedList = resolver
            .getSymbolsWithAnnotation(
                annotationName = UriAutowiredAnno::class.qualifiedName!!
            )
            .mapNotNull { it as? KSPropertyDeclaration }
            .toList()

        val attrAutoWireAnnotatedList = resolver
            .getSymbolsWithAnnotation(
                annotationName = AttrValueAutowiredAnno::class.qualifiedName!!
            )
            .mapNotNull { it as? KSPropertyDeclaration }
            .toList()

        val serviceAutoWireAnnotatedList = resolver
            .getSymbolsWithAnnotation(
                annotationName = ServiceAutowiredAnno::class.qualifiedName!!
            )
            .mapNotNull { it as? KSPropertyDeclaration }
            .toList()

        val eachCollectList =
            (uriAutoWireValidList + attrValueAutowiredValidList + serviceAutowiredValidList)
                .filterIsInstance<KSPropertyDeclaration>()

        eachCollectList.groupBy {
            it.closestClassDeclaration()?.convertToInjectFileInfo()
        }.mapNotNull { (key, value) ->
            key?.let {
                key to value.mapNotNull {
                    it.convertToPropertyInfo()
                }
            }
        }.forEach { (key, value) ->
            val newValue = (collectInfoMap[key] ?: emptySet()) + value.toSet()
            collectInfoMap[key] = newValue
        }

        return uriAutoWireInValidList + attrValueAutowiredInValidList + serviceAutowiredInValidList

    }

    override fun finish() {
        super.finish()
        createAllFile()
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
class AutowireProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn(
            "$AutowireProcessor.TAG, AutowireProcessorProvider.create called"
        )
        return AutowireProcessor(
            environment = environment,
        )
    }

}