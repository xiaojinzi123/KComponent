package com.xiaojinzi.component.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Set;

/**
 * 用户帮助生成 ASMUtil 这个类的
 */
public class ASMUtilClassGen implements Opcodes {

    /**
     * 获取字节码的字节数组
     *
     * @param applicationMap 模块相关的参数
     * @return 字节码的字节数组
     */
    public static byte[] getBytes(
            Map<String, String> applicationMap
    ) {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                V1_7, ACC_PUBLIC | ACC_SUPER,
                "com/xiaojinzi/component/support/ASMUtil", null,
                "java/lang/Object", null
        );

        writeStructureMethod(cw);
        writeModuleNameMethod(cw, applicationMap);
        writeApplicationMethod(cw, applicationMap);
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static void writeStructureMethod(ClassVisitor cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "Lcom/xiaojinzi/component/support/ASMUtil;", null, label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private static void writeModuleNameMethod(ClassVisitor cw, Map<String, String> applicationMap) {

        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,
                "getModuleNames", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/String;>;", null);
        methodVisitor.visitCode();
        // 开始的标记
        Label labelStart = new Label();
        // 标记开始
        methodVisitor.visitLabel(labelStart);
        // 创建 对象 : List<String> result = new ArrayList<>();
        methodVisitor.visitTypeInsn(NEW, "java/util/ArrayList");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        methodVisitor.visitVarInsn(ASTORE, 0);

        // 开始的标记
        Label label1 = new Label();

        Set<Map.Entry<String, String>> entries = applicationMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            // 拿到名称
            String name = entry.getKey();

            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitLdcInsn(name);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
            methodVisitor.visitInsn(POP);

        }

        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ARETURN);

        // 结束的标记
        Label labelEnd = new Label();
        methodVisitor.visitLabel(labelEnd);
        methodVisitor.visitLocalVariable("result", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", label1, labelEnd, 0);
        methodVisitor.visitEnd();

    }

    private static void writeApplicationMethod(ClassVisitor cw, Map<String, String> applicationMap) {

        // com.xiaojinzi.component.impl.IModuleLifecycle

        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "findModuleApplicationAsmImpl",
                "(Ljava/lang/String;)Lcom/xiaojinzi/component/impl/IModuleLifecycle;", null, null);
        methodVisitor.visitCode();
        // 开始的标记
        Label labelStart = new Label();
        // 标记开始
        methodVisitor.visitLabel(labelStart);

        Set<Map.Entry<String, String>> entries = applicationMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            // 除了第一个后面的都有前一个 if 跳转过来的 label
            Label ifJumpLabel = new Label();
            // 拿到名称
            String name = entry.getKey();
            String classFullName = entry.getValue();
            String className = classFullName.substring(0, classFullName.lastIndexOf(".class"));
            String classPathName = "com/xiaojinzi/component/impl/" + className;

            methodVisitor.visitLdcInsn(name);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z", false);
            methodVisitor.visitJumpInsn(IFEQ, ifJumpLabel);
            methodVisitor.visitTypeInsn(NEW, classPathName);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, classPathName, "<init>", "()V", false);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(ifJumpLabel);
        }

        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ARETURN);

        // 结束的标记
        Label labelEnd = new Label();
        methodVisitor.visitLabel(labelEnd);
        methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, labelStart, labelEnd, 0);
        methodVisitor.visitEnd();

    }

}