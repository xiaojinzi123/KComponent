package com.xiaojinzi.component.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.utils.FileUtils;
import com.xiaojinzi.component.plugin.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

public class ModifyASMUtilTransform extends BaseTransform {

    public static final String IMPL_OUTPUT_PKG_PATH = "com/xiaojinzi/component/impl";

    /**
     * value 是 class 的名称
     * "user" -> "UserModuleGenerated"
     */
    private Map<String, String> applicationMap = new HashMap<>();

    private String mAsmUtilOutputPathStr;

    public ModifyASMUtilTransform(String asmUtilOutputPathStr) {
        super();
        mAsmUtilOutputPathStr = asmUtilOutputPathStr;
    }

    @Override
    public String getName() {
        return "ComponentPlugin";
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }

        filterAllNames(transformInvocation);

        // 消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        // OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        try {
            for (TransformInput input : inputs) {
                for (JarInput jarInput : input.getJarInputs()) {
                    File dest = outputProvider.getContentLocation(
                            jarInput.getFile().getAbsolutePath(),
                            jarInput.getContentTypes(),
                            jarInput.getScopes(),
                            Format.JAR);
                    File destJarFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + "_" + jarInput.getFile().getName());
                    if (destJarFile.exists()) {
                        destJarFile.delete();
                    }

                    JarFile jarFile = new JarFile(jarInput.getFile());
                    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();

                    JarOutputStream jarOutputStream = new JarOutputStream(
                            new FileOutputStream(destJarFile)
                    );
                    while (jarEntryEnumeration.hasMoreElements()) {
                        JarEntry jarEntry = jarEntryEnumeration.nextElement();
                        String entryName = jarEntry.getName();
                        // 如果是目标工具类, 就换成手动生成的
                        if ("com/xiaojinzi/component/support/ASMUtil.class".equals(entryName)) {
                            byte[] bytes = ASMUtilClassGen.getBytes(
                                    applicationMap
                            );
                            try {
                                // 生成到桌面用来测试
                                if (mAsmUtilOutputPathStr != null && !"".equals(mAsmUtilOutputPathStr)) {
                                    File file = new File(mAsmUtilOutputPathStr);
                                    file.delete();
                                    if (!file.getParentFile().mkdirs()) {
                                        throw new Exception();
                                    }
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(bytes);
                                    fileOutputStream.close();
                                }
                            } catch (Exception ignore) {
                                // ignore
                            }
                            ZipEntry asmUtiZipEntry = new ZipEntry(jarEntry.getName());
                            asmUtiZipEntry.setSize(bytes.length);
                            CRC32 crc = new CRC32();
                            crc.update(bytes);
                            asmUtiZipEntry.setCrc(crc.getValue());
                            jarOutputStream.putNextEntry(asmUtiZipEntry);
                            jarOutputStream.write(bytes);
                        } else {
                            ZipEntry zipEntry = new ZipEntry(jarEntry);
                            zipEntry.setCompressedSize(-1);
                            jarOutputStream.putNextEntry(zipEntry);
                            InputStream inputStream = jarFile.getInputStream(zipEntry);
                            IOUtil.readAndWrite(inputStream, jarOutputStream);
                            inputStream.close();
                        }
                        jarOutputStream.closeEntry();
                    }
                    jarOutputStream.close();
                    FileUtils.copyFile(destJarFile, dest);
                    // 删除文件
                    destJarFile.delete();
                }
                for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                    File dest = outputProvider.getContentLocation(directoryInput.getName(),
                            directoryInput.getContentTypes(), directoryInput.getScopes(),
                            Format.DIRECTORY);
                    File directoryInputFile = directoryInput.getFile();
                    //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                    FileUtils.copyDirectory(directoryInputFile, dest);
                }


            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw e;
        }

    }

    /**
     * 找到所有 Application 的生成类
     *
     * @param transformInvocation
     * @throws IOException
     */
    private void filterAllNames(TransformInvocation transformInvocation) throws IOException {
        applicationMap.clear();
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                JarFile jarFile = new JarFile(jarInput.getFile());
                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry jarEntry = jarEntryEnumeration.nextElement();
                    String entryName = jarEntry.getName();
                    // 统一使用 / 分隔符处理. 全类名
                    String className = entryName.replace('\\', '/');
                    doFilterApplicationName(className);
                }
            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                recursiveFile(directoryInput.getFile(), new IFileRecursive() {
                    @Override
                    public void accept(File file) {
                        // xxx.class 统一使用 / 分隔符处理
                        String filePath = file.getPath().replace('\\', '/');
                        // 去掉文件前面本地的路径
                        if (filePath.contains(IMPL_OUTPUT_PKG_PATH)) {
                            // xxx/xxx/xxx/xxxxxxxx
                            String className = filePath.substring(
                                    filePath.indexOf(IMPL_OUTPUT_PKG_PATH)
                            );
                            doFilterApplicationName(className);
                        }

                    }
                });
            }
        }
    }

    private void doFilterApplicationName(String className) {
        if (className.startsWith(IMPL_OUTPUT_PKG_PATH)
                && (className.endsWith("ModuleGenerated.class"))
        ) {
            String simpleName = className.substring(className.lastIndexOf("/") + 1);
            // 拿到 host 的名称
            String hostName = simpleName.substring(0, simpleName.indexOf("ModuleGenerated"));
            // 如果不存在才能进去
            if (!applicationMap.containsKey(hostName)) {
                applicationMap.put(hostName, simpleName);
            }
        }
    }

    private void recursiveFile(File file, IFileRecursive fileRecursive) {
        if (file.isFile()) {
            fileRecursive.accept(file);
        } else {
            File[] files = file.listFiles();
            for (File subFile : files) {
                if (subFile.isFile()) {
                    fileRecursive.accept(subFile);
                } else {
                    recursiveFile(subFile, fileRecursive);
                }
            }
        }
    }

}
