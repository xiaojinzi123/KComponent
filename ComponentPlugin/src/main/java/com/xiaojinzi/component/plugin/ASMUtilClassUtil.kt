package com.xiaojinzi.component.plugin

import javassist.ClassPool
import javassist.CtConstructor
import javassist.CtMethod

object ASMUtilClassUtil {

    fun getClassBytes(moduleNameMap: Map<String, String>): ByteArray {

        val moduleNames = moduleNameMap.keys
        val classPool = ClassPool.getDefault()
        val listClass = classPool.get("java.util.List")
        val asmUtilClass = classPool.makeClass("com.xiaojinzi.component.support.ASMUtil")

        val getModuleNamesMethod = CtMethod(listClass, "getModuleNames", emptyArray(), asmUtilClass)
        getModuleNamesMethod.modifiers = javassist.Modifier.PUBLIC or javassist.Modifier.STATIC
        // getModuleNamesMethod.genericSignature = "(Ljava/util/List<Ljava/lang/String;>;)V"

        val getModuleNamesMethodBodySb = StringBuilder()
        getModuleNamesMethodBodySb.append("{")
        getModuleNamesMethodBodySb.append("java.util.List list = new java.util.ArrayList();")
        moduleNames.forEach {
            getModuleNamesMethodBodySb.append("list.add(\"$it\");")
        }
        getModuleNamesMethodBodySb.append("return list;")
        getModuleNamesMethodBodySb.append("}")
        getModuleNamesMethod.setBody(
            getModuleNamesMethodBodySb.toString()
        )
        asmUtilClass.addMethod(getModuleNamesMethod)

        val interfaceIModuleLifecycle =
            classPool.makeInterface("com.xiaojinzi.component.impl.IModuleLifecycle")
        val findModuleApplicationAsmImplMethod = CtMethod(
            interfaceIModuleLifecycle,
            "findModuleApplicationAsmImpl",
            arrayOf(classPool.get("java.lang.String")),
            asmUtilClass,
        )
        findModuleApplicationAsmImplMethod.modifiers =
            javassist.Modifier.PUBLIC or javassist.Modifier.STATIC
        val findModuleApplicationAsmImplMethodBodySb = StringBuilder()
        findModuleApplicationAsmImplMethodBodySb.append("{")
        if (moduleNames.isEmpty()) {
            findModuleApplicationAsmImplMethodBodySb.append("return null;")
            val tempClass = classPool.makeClass("com.xiaojinzi.component.impl.AppModuleGenerated")
            tempClass.addConstructor(
                CtConstructor(
                    arrayOf(),
                    tempClass
                ).apply {
                    modifiers = javassist.Modifier.PUBLIC
                }
            )
            findModuleApplicationAsmImplMethodBodySb.append("return new com.xiaojinzi.component.impl.AppModuleGenerated();")
        } else {
            moduleNames.forEachIndexed { index, item ->
                val targetClassFullName = moduleNameMap[item]!!.removeSuffix(
                    suffix = ".class"
                )
                classPool.makeClass(targetClassFullName).apply {
                    this.addConstructor(
                        CtConstructor(
                            arrayOf(),
                            this
                        ).apply {
                            modifiers = javassist.Modifier.PUBLIC
                        }
                    )
                }
                if (index != 0) {
                    findModuleApplicationAsmImplMethodBodySb.append(" else ")
                }
                findModuleApplicationAsmImplMethodBodySb.append("if (\"$item\".equals($1)) {")
                findModuleApplicationAsmImplMethodBodySb.append(
                    "return new ${targetClassFullName}();"
                )
                findModuleApplicationAsmImplMethodBodySb.append("}")
            }
            findModuleApplicationAsmImplMethodBodySb.append(" else {return null;}")
        }
        findModuleApplicationAsmImplMethodBodySb.append("}")
        findModuleApplicationAsmImplMethod.setBody(
            findModuleApplicationAsmImplMethodBodySb.toString(),
        )
        asmUtilClass.addMethod(findModuleApplicationAsmImplMethod)

        return asmUtilClass.toBytecode()
    }

}