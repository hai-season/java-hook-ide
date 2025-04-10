package org.hai.jhook;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TestCatchExp {
    private static final ConcurrentHashMap<String, byte[]> classCodeMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        redefine("before", 0, "{ System.out.println(\"insert before\"); }");
        redefine("after", 0, "{ System.out.println(\"insert after\"); }");
//        redefine("total", 0, "{ System.out.println(\"insert total\"); }");
        redefine("line", 3, "{ System.out.println(\"insert line 33\"); }");
        redefine("reset", 3, "{ System.out.println(\"insert line 33\"); }");
        redefine("before", 3, "{ System.out.println(\"insert before after reset\"); }");
    }

    public static void redefine(String location, Integer line, String snippets) {
        try {
            String className = "org.hai.jhook.AppDemo";
            String methodName = "loopCalc";
            if (location.equals("reset")) {
                classCodeMap.remove(className);
                return;
            }
            Class aClass = Class.forName(className);
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new ClassClassPath(aClass));
            pool.getCtClass(className).defrost();
            byte[] code = null;
            CtClass ctClass = null;
            if (classCodeMap.containsKey(className)) {
                code = classCodeMap.get(className);
                ctClass = pool.makeClass(new ByteArrayInputStream(code));
            } else {
                ctClass = pool.getCtClass(aClass.getName());
            }
            CtMethod method = ctClass.getDeclaredMethod(methodName);
            if (location.equals("before")) {
                method.insertBefore(snippets);
            } else if (location.equals("after")) {
                method.insertAfter(snippets);
            } else if (location.equals("total")) {
                method.setBody(snippets);
            } else if (location.equals("exception")) {
                method.addCatch(snippets, pool.get("java.lang.Exception"));
            } else if (location.equals("line")) {
                method.insertAt(line, snippets);
            }
            code = ctClass.toBytecode();
            Files.write(Paths.get("./", new Date().getTime() + ".class"), code);
            classCodeMap.put(className, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test() throws Exception {
        Class aClass = AppDemo.class;
        byte[] code = null;
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new ClassClassPath(aClass));
        CtClass ctClass = pool.getCtClass(aClass.getName());
        CtMethod method = ctClass.getDeclaredMethod("loopCalc");
        String location = "exception";
        String snippets = "{ System.out.println(\"hello\"); }";
        int line = 0;
        if (location.equals("before")) {
            method.insertBefore(snippets);
        } else if (location.equals("after")) {
            method.insertAfter(snippets);
        } else if (location.equals("total")) {
            method.setBody(snippets);
        } else if (location.equals("exception")) {
            method.addCatch(snippets, pool.get("java.lang.Exception"));
        } else if (location.equals("line")) {
            method.insertAt(line, snippets);
        }
        code = ctClass.toBytecode();
        Files.write(Paths.get("./", "a.class"), code);
    }
}
