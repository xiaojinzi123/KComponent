package com.xiaojinzi.component.compiler.kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.xiaojinzi.component.ComponentConstants
import com.xiaojinzi.component.anno.AttrValueAutowiredAnno
import com.xiaojinzi.component.anno.ServiceAutowiredAnno
import com.xiaojinzi.component.anno.UriAutowiredAnno
import com.xiaojinzi.component.anno.support.ComponentGeneratedAnno
import com.xiaojinzi.component.support.AttrAutoWireMode

class AutowireProcessor(
    override val environment: SymbolProcessorEnvironment,
    val logger: KSPLogger = environment.logger,
    val codeGenerator: CodeGenerator = environment.codeGenerator,
) : BaseHostProcessor(
    environment = environment,
) {

    val TAG = "AutowireProcessor"

    @OptIn(KspExperimental::class)
    private fun createFile(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        targetAnnotatedList: List<KSPropertyDeclaration>,
    ) {

        val activityKsClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_ACTIVITY)
        val fragmentKsClassDeclaration =
            resolver.getClassDeclarationByName(name = ComponentConstants.ANDROID_FRAGMENT)

        // 目标注入对象的 KsType
        val classDeclarationKsType = classDeclaration.asStarProjectedType()

        val isSubActivity = activityKsClassDeclaration
            ?.asStarProjectedType()
            ?.isAssignableFrom(that = classDeclarationKsType)
            ?: false

        val isSubFragment = fragmentKsClassDeclaration
            ?.asStarProjectedType()
            ?.isAssignableFrom(that = classDeclarationKsType)
            ?: false

        if (isSubActivity.not() && isSubFragment.not()) {
            return
        }

        // 目标注入的 class 对象
        val targetClassClassName = classDeclaration.toClassName()
        // Inject 接口
        val injectClassName = ComponentConstants.INJECT_CLASS_NAME.toClassName()
        // 属性注入的模式
        val attrAutoWireModeClassName = ComponentConstants.ATTRAUTOWIREMODE_CLASS_NAME.toClassName()

        val classSimpleName = targetClassClassName.simpleName + "_Inject"

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
                            isSubActivity -> {
                                it.addStatement("this.injectAttrValue(target = target, bundle = target.intent?.extras?: Bundle())")
                            }
                            isSubFragment -> {
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

                        targetAnnotatedList.forEach { ksPropertyDeclaration ->

                            val isPropertyLateInit =
                                ksPropertyDeclaration.modifiers.contains(element = Modifier.LATEINIT)

                            // 这个属性是否可空的
                            val isPropertyNullable =
                                ksPropertyDeclaration.type.resolve().isMarkedNullable

                            // 属性的名字
                            val propertyName = ksPropertyDeclaration.simpleName.asString()

                            val uriAutoWireAnno = ksPropertyDeclaration.getAnnotationsByType(
                                annotationKClass = UriAutowiredAnno::class
                            ).firstOrNull()

                            if (uriAutoWireAnno != null) {
                                logger.warn(
                                    message = "uriAutoWireAnno = $uriAutoWireAnno"
                                )
                            }

                            val attrAutoWireAnno = ksPropertyDeclaration.getAnnotationsByType(
                                annotationKClass = AttrValueAutowiredAnno::class
                            ).firstOrNull()

                            logger.warn(
                                message = "attrAutoWireAnno = $attrAutoWireAnno"
                            )

                            val serviceAutoWireAnno = ksPropertyDeclaration.getAnnotationsByType(
                                annotationKClass = ServiceAutowiredAnno::class
                            ).firstOrNull()

                            val defaultModel: (funSpec: FunSpec.Builder, attrAutoWireAnnoItemName: String) -> Unit =
                                { funSpec, attrAutoWireAnnoItemName ->
                                    funSpec.addStatement(
                                        format = "target.%N = %T.%N(bundle = bundle, key = %S)?: target.%N",
                                        propertyName,
                                        mClassNameParameterSupport,
                                        getMethodNameFromKsType(
                                            resolver = resolver,
                                            ksType = ksPropertyDeclaration.type.resolve(),
                                            prefix = "get",
                                        ),
                                        attrAutoWireAnnoItemName,
                                        propertyName,
                                    )
                                }

                            val overrideModel: (funSpec: FunSpec.Builder, attrAutoWireAnnoItemName: String, isNullable: Boolean) -> Unit =
                                { funSpec, attrAutoWireAnnoItemName, isNullable ->
                                    funSpec.addStatement(
                                        format = "target.%N = %T.%N(bundle = bundle, key = %S)${if (isNullable) "" else "!!"}",
                                        propertyName,
                                        mClassNameParameterSupport,
                                        getMethodNameFromKsType(
                                            resolver = resolver,
                                            ksType = ksPropertyDeclaration.type.resolve(),
                                            prefix = "get",
                                        ),
                                        attrAutoWireAnnoItemName,
                                    )
                                }

                            uriAutoWireAnno?.let {

                                funSpec.addStatement(
                                    format = "target.%N = %T.getUri(bundle = bundle)${if (isPropertyLateInit || isPropertyNullable) "!!" else ""}",
                                    propertyName,
                                    mClassNameParameterSupport,
                                )

                            }

                            attrAutoWireAnno?.let {

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

                                        if (isPropertyLateInit) {
                                            overrideModel.invoke(
                                                funSpec,
                                                attrAutoWireAnnoItemName,
                                                isPropertyNullable,
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
                                                                isPropertyNullable,
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
                                                        isPropertyNullable,
                                                    )
                                                }
                                            }
                                        }
                                        funSpec.endControlFlow()
                                    }

                                if (attrAutoWireAnno.value.isEmpty()) {
                                    oneNameOfPropertyCall.invoke(0, propertyName)
                                } else {
                                    attrAutoWireAnno.value.forEachIndexed { index, attrAutoWireAnnoItemName ->
                                        oneNameOfPropertyCall.invoke(
                                            index,
                                            attrAutoWireAnnoItemName
                                        )
                                    }
                                }

                            }

                            serviceAutoWireAnno?.let {

                                funSpec.addStatement(
                                    format = "target.%N = %T.%N(tClass = %T::class)",
                                    propertyName,
                                    mClassNameServiceManager,
                                    if(isPropertyNullable) {
                                        "get"
                                    } else {
                                        "requiredGet"
                                    },
                                    ksPropertyDeclaration.type.resolve().toClassName(),
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
            logger.warn(
                message = "classDeclarationKsType1 = $classDeclarationKsType, isSubFragmentActivity = $isSubActivity, isSubFragment = $isSubFragment",
            )
            codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                packageName = fileSpec.packageName,
                fileName = fileSpec.name,
            ).use {
                it.write(
                    fileSpec.toString().toByteArray()
                )
            }
            logger.warn(
                message = "classDeclarationKsType2 = $classDeclarationKsType, isSubFragmentActivity = $isSubActivity, isSubFragment = $isSubFragment",
            )
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun doProcess(resolver: Resolver): List<KSAnnotated> {

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

        (uriAutoWireAnnotatedList + attrAutoWireAnnotatedList + serviceAutoWireAnnotatedList)
            .groupBy {
                it.closestClassDeclaration()
            }
            .forEach { mapItem ->
                // 对 key 为 null 的不予考虑
                val classDeclaration = mapItem.key ?: return@forEach
                createFile(
                    resolver = resolver,
                    classDeclaration = classDeclaration,
                    targetAnnotatedList = mapItem.value
                )
            }

        return uriAutoWireAnnotatedList + attrAutoWireAnnotatedList + serviceAutoWireAnnotatedList
    }

    override fun finish() {
        super.finish()
        logger.warn("$TAG finish")
    }

    override fun onError() {
        super.onError()
        logger.warn("$TAG onError")
    }

}

@AutoService(SymbolProcessorProvider::class)
class AutowireProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn(
            "AutowireProcessorProvider.create called"
        )
        return AutowireProcessor(
            environment = environment,
        )
    }

}