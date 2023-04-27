package com.xiaojinzi.component.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File

val moduleNameSet = mutableSetOf<String>()

var buildFolder: File? = null

private fun filterClassFilesRecursion(file: File, jarList: MutableList<File>) {
    file.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            println("filterClassFilesRecursion.folder = ${file.name}")
            filterClassFilesRecursion(file = file, jarList = jarList)
        } else {
            println("filterClassFilesRecursion.file = ${file.name}")
            if(file.name.endsWith(suffix = ".class")) {
                jarList.add(file)
            }
        }
    }
}

private fun filterClassFiles(): List<File> {
    return buildFolder?.let { buildFolder ->
        val jarList = mutableListOf<File>()
        filterClassFilesRecursion(jarList = jarList, file = buildFolder)
        jarList
    }?: emptyList()
}

class KComponentPlugin2 : Plugin<Project> {

    class AsmUtilModifyClassVisitor(
        nextClassVisitor: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM5, nextClassVisitor) {

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            filterClassFiles().forEach {
                println("classes ----= ${it.path}")
            }
            println("AsmUtilModifyClassVisitor.visit: name = $name, moduleNameSet = $moduleNameSet")
            super.visit(version, access, name, signature, superName, interfaces)
        }

    }

    class CollectModuleNameClassVisitor(
        nextClassVisitor: ClassVisitor
    ) : ClassVisitor(
        Opcodes.ASM5, nextClassVisitor,
    ) {
        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            if (name?.endsWith(suffix = "ModuleGenerated") == true) {
                moduleNameSet.add(name.substring(startIndex = "com/xiaojinzi/component/impl/".length))
                println("CollectModuleNameClassVisitor.visit: name = $name, moduleNameSet = $moduleNameSet")
            }
            super.visit(version, access, name, signature, superName, interfaces)
        }

    }

    abstract class AsmUtilAsmClassVisitorFactory :
        AsmClassVisitorFactory<InstrumentationParameters.None> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor { //ClassNode和ClassVisito
            return AsmUtilModifyClassVisitor(
                nextClassVisitor = nextClassVisitor,
            )
        }

        //判断当前类是否要进行扫描，ClassData则包含了类的一些信息
        override fun isInstrumentable(classData: ClassData): Boolean {
            return "com.xiaojinzi.component.support.ASMUtil" == classData.className
        }

    }

    abstract class CollectModuleNameAsmClassVisitorFactory :
        AsmClassVisitorFactory<InstrumentationParameters.None> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor { //ClassNode和ClassVisito
            return CollectModuleNameClassVisitor(
                nextClassVisitor = nextClassVisitor,
            )
        }

        //判断当前类是否要进行扫描，ClassData则包含了类的一些信息
        override fun isInstrumentable(classData: ClassData): Boolean {
            buildFolder?.let {  buildFolder ->
                buildFolder.listFiles()?.forEach {
                    println("file = ${it.name}")
                }
            }
            return classData.className.startsWith("com.xiaojinzi.component")
        }

    }

    override fun apply(project: Project) {

        buildFolder = project.buildDir

        moduleNameSet.clear()
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->

            println("variant1 = ${variant.name}")

            variant
                .instrumentation
                .transformClassesWith(
                    classVisitorFactoryImplClass = AsmUtilAsmClassVisitorFactory::class.java,
                    scope = InstrumentationScope.ALL,
                ) {
                }

            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)

        }

    }

}