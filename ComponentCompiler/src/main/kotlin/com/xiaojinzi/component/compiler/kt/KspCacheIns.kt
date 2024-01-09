package com.xiaojinzi.component.compiler.kt

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import java.io.File

object KspCacheIns {

    val tempKspFolder = File(
        System.getProperty("java.io.tmpdir"),
        "kcomponentKspCacheFolder",
    )

    fun save(
        logEnable: Boolean,
        logger: KSPLogger,
        processorTag: String,
        moduleName: String,
        kspOptimizeUniqueName: String,
        packageName: String,
        fileName: String,
        data: ByteArray,
    ) {
        runCatching {
            // 从系统变量中获取临时目录的
            val tempCacheFolderForModule = File(
                tempKspFolder,
                moduleName.replace(oldChar = '-', newChar = '_'),
            )
            val relativePathName = packageName.replace(
                oldChar = '.', newChar = File.separatorChar
            ) + File.separatorChar + fileName
            val tempCacheFolderInModule = File(tempCacheFolderForModule, kspOptimizeUniqueName)
            File(
                tempCacheFolderInModule,
                relativePathName,
            ).apply {
                if (logEnable) {
                    logger.warn("$processorTag $moduleName saveMethod tempCacheFolder = ${tempCacheFolderForModule.path}")
                    logger.warn("$processorTag $moduleName saveMethod targetFileInCache = ${this.path}")
                }
                this.parentFile?.mkdirs()
                this.outputStream().use {
                    it.write(data)
                }
            }
        }
    }

    @Synchronized
    fun readCacheToKspFolder(
        logEnable: Boolean,
        logger: KSPLogger,
        processorTag: String,
        moduleName: String,
        kspOptimizeUniqueName: String?,
        simpleNameSuffix: String,
        codeGenerator: CodeGenerator,
    ): Int {
        // 从系统变量中获取临时目录的
        val tempCacheFolder = File(
            tempKspFolder,
            "${
                moduleName.replace(
                    oldChar = '-',
                    newChar = '_'
                )
            }/$kspOptimizeUniqueName",
        )

        val targetFileList = tempCacheFolder
            .walk()
            .filter {
                it.isFile && it.nameWithoutExtension.endsWith(
                    suffix = simpleNameSuffix,
                )
            }
            .toList()

        // 深度遍历 tempCacheFolder
        targetFileList.forEach { file ->
            if (file.isFile) {
                if (file.nameWithoutExtension.endsWith(
                        suffix = simpleNameSuffix,
                    )
                ) {
                    val packageName = file.parentFile
                        .relativeTo(base = tempCacheFolder)
                        .path
                        .replace(oldChar = File.separatorChar, newChar = '.')
                    codeGenerator.createNewFile(
                        dependencies = Dependencies.ALL_FILES,
                        packageName = packageName,
                        fileName = file.name,
                        extensionName = file.extension,
                    )
                }
            }

        }

        logger.warn("$processorTag $moduleName 总共为您恢复了 ${targetFileList.size} 个文件")

        return targetFileList.size

    }

}