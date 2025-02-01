package org.hai.jhook;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AppTest {
    public void test1() throws Exception {
//        String file = "E:\\DemoProject\\java-hook-ide\\BIConfigService.class";
//        byte[] bytecode = Files.readAllBytes(new File(file).toPath());
//        ClassPool.getDefault().appendSystemPath().openClassfile("");
//        CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytecode));
//        CtMethod method = ctClass.getDeclaredMethod("checkLoginInfo");
//        method.insertBefore("smartbi.util.StrUtil.replace();");
//        bytecode = ctClass.toBytecode();
//        System.out.println(bytecode.length);
    }

    public void test2() throws Exception {
//        ClassPool pool = ClassPool.getDefault();
//        CtClass ctClass = pool.get("javax.servlet.http.HttpSession");
//        System.out.println(ctClass);
    }
}
