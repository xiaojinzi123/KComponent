package com.xiaojinzi.component.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.joom.grip.Grip
import com.joom.grip.GripFactory
import com.joom.grip.annotatedWith
import com.joom.grip.classes
import com.joom.grip.mirrors.getType
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class KComponentPlugin2 : Plugin<Project> {

    abstract class RouterClassesTask : DefaultTask() {

        @get:InputFiles
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val jars: ListProperty<RegularFile>

        @get:InputFiles
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val dirs: ListProperty<Directory>

        @get:OutputFile
        abstract val output: RegularFileProperty

        @get:Classpath
        abstract val bootClasspath: ListProperty<RegularFile>

        @get:CompileClasspath
        abstract var classpath: FileCollection

        @TaskAction
        fun taskAction() {

            // 输入的 jar、aar、源码
            val inputs = (jars.get() + dirs.get()).map { it.asFile.toPath() }

            // 系统依赖
            val classPaths = bootClasspath.get().map { it.asFile.toPath() }
                .toSet() + classpath.files.map { it.toPath() }

            val grip: Grip = GripFactory.newInstance(Opcodes.ASM9).create(classPaths + inputs)
            val query = grip
                .select(classes)
                .from(inputs)
                .where(
                    annotatedWith(
                        annotationType = getType(
                            descriptor = "Lcom/xiaojinzi/component/anno/support/ModuleApplicationAnno;",
                        )
                    )
                )

            // 找到所有满足条件的 class
            val moduleNameMap = query
                .execute()
                .classes
                .associate {
                    it.name
                        .removePrefix(prefix = "com.xiaojinzi.component.impl.")
                        .removeSuffix(suffix = "ModuleGenerated") to "${it.name}.class"
                }

            val targetTestFolder = File("/Users/hhkj/Desktop/测试")

            println("moduleNameMap = $moduleNameMap")

            File(targetTestFolder, "jars.txt")
                .outputStream()
                .use { output ->
                    output.write(
                        jars.get().joinToString(separator = "\n") { it.asFile.absolutePath }
                            .toByteArray()
                    )
                }

            File(targetTestFolder, "dirs.txt")
                .outputStream()
                .use { output ->
                    output.write(
                        dirs.get().joinToString(separator = "\n") { it.asFile.absolutePath }
                            .toByteArray()
                    )
                }

            val jarList = jars
                .get()
                .map { it.asFile.absolutePath }

            val dirList = dirs
                .get()
                .map { it.asFile.absolutePath }

            val outputJarFile = output.get().asFile

            JarOutputStream(
                BufferedOutputStream(
                    FileOutputStream(outputJarFile)
                )
            ).use { jarOutputStream ->

                val jarToEntityList = jarList
                    .map { File(it) }
                    .map { JarFile(it) }
                    .flatMap { jarFile -> jarFile.entries().toList().map { jarFile to it } }

                // 处理 jars 的文件夹
                jarToEntityList
                    .filter { it.second.isDirectory }
                    .distinctBy { it.second.name }
                    .forEach { item ->

                        val jarFile = item.first
                        val jarEntity = item.second

                        jarOutputStream.putNextEntry(
                            JarEntry(jarEntity.name)
                        )

                        jarFile.getInputStream(jarEntity).use {
                            it.copyTo(out = jarOutputStream)
                        }

                        jarOutputStream.closeEntry()
                    }

                // 处理 jars 的文件
                jarToEntityList
                    .filter { !it.second.isDirectory }
                    .forEach { item ->

                        val jarFile = item.first
                        val jarEntity = item.second

                        try {
                            jarOutputStream.putNextEntry(
                                JarEntry(jarEntity.name).apply {
                                    this.time = 0
                                }
                            )
                            if ("com/xiaojinzi/component/support/ASMUtil.class" == jarEntity.name) {
                                val asmUtlClassBytes = ASMUtilClassGen.getBytes(moduleNameMap)
                                jarOutputStream.write(asmUtlClassBytes)
                            } else {
                                jarFile.getInputStream(jarEntity).use {
                                    it.copyTo(out = jarOutputStream)
                                }
                            }
                            jarOutputStream.closeEntry()
                        } catch (e: Exception) {
                            println("jarEntity = ${jarEntity.name}, jarFile = ${jarFile.name}")
                        }
                    }

                // 最后处理 dirs 目录
                dirList
                    .map { File(it) }
                    .flatMap { dir -> dir.walk().toList().map { dir to it } }
                    .filter { it.second.isFile }
                    .forEach { disToFileItem ->

                        val dir = disToFileItem.first
                        val file = disToFileItem.second

                        val relativePath = dir
                            .toURI()
                            .relativize(file.toURI())
                            .path

                        jarOutputStream.putNextEntry(
                            JarEntry(
                                relativePath.replace(
                                    File.separatorChar,
                                    '/',
                                )
                            ).apply {
                                this.time = 0
                            }
                        )

                        file.inputStream().use {
                            it.copyTo(out = jarOutputStream)
                        }

                        jarOutputStream.closeEntry()

                    }

            }

        }

    }

    override fun apply(project: Project) {

        with(project) {

            plugins.withType(AppPlugin::class.java) {

                val androidComponents =
                    extensions.findByType(AndroidComponentsExtension::class.java)
                androidComponents?.onVariants { variant ->
                    val name = "gather${variant.name.capitalize(Locale.ROOT)}RouteTables"
                    val taskProvider = tasks.register<RouterClassesTask>(name) {
                        group = "component"
                        description = "gather ${variant.name} route tables"
                        bootClasspath.set(androidComponents.sdkComponents.bootClasspath)
                        classpath = variant.compileClasspath
                    }

                    variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                        .use(taskProvider)
                        .toTransform(
                            ScopedArtifact.CLASSES,
                            RouterClassesTask::jars,
                            RouterClassesTask::dirs,
                            RouterClassesTask::output,
                        )
                }

            }

        }

    }

}