package org.hai.jhook;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class App {
    public static void main(String[] args) throws Exception {
        test1();
    }

    public static void test2() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get("org.hai.jhook.Demo");
        pool.importPackage("java.util.ArrayList");
        CtMethod method = cc.getDeclaredMethod("say");
        method.insertAfter("System.out.println(\"code enhan\");");
        method.addCatch("System.out.println(new ArrayList()); return;", pool.get("java.lang.Exception"));
        cc.writeFile("a");
    }

    public static void test1() throws Exception {
        loadAgent();
        redefine();
    }

    public static void loadAgent() throws Exception {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        Optional<VirtualMachineDescriptor> currentVmd =
                list.stream().filter(v -> v.displayName().contains("org.apache.catalina.startup.Bootstrap")).findFirst();
        if (!currentVmd.isPresent()) {
            throw new RuntimeException("unknown error");
        }
        VirtualMachineDescriptor desc = currentVmd.get();
        System.out.println("current desc: " + desc.displayName());
        VirtualMachine machine = VirtualMachine.attach(desc);
        machine.loadAgent("E:\\DemoProject\\java-hook-ide\\agent\\target\\agent-1.0-jar-with-dependencies.jar");
    }

    public static void redefine() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9090);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        output.writeUTF(CommandType.REDEFINE_CLASS.name());
        output.writeUTF("class");
        output.writeUTF("method");
        output.writeUTF("before");
        output.writeUTF("if(true) return true;");
        output.flush();
        System.out.println(input.readUTF());
//        output.writeUTF(CommandType.GET_ALL_CLASSES.name());
//        output.flush();
//        int len = input.readInt();
//        long start = System.currentTimeMillis();
//        String base = "E:\\DemoProject\\java-hook-ide\\core\\result\\";
//        for(int i=0;i<len;i++){
//            String className = input.readUTF() + ".class";
//            int length = input.readInt();
//            byte[] code = new byte[length];
//            input.readFully(code);
//            Files.write(Paths.get(base, className), code);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("cost time: " + (end - start) + " ms");


        // checkLoginInfo
//        output.writeUTF(CommandType.GET_CLASS.name());
//        output.writeUTF("smartbi.config.BIConfigService");
//        output.flush();
//        int length = input.readInt();
//        byte[] bytecode = new byte[length];
//        input.readFully(bytecode);
//        Files.write(Paths.get("iv.class"), bytecode);
//        CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytecode));
//        CtMethod method = ctClass.getDeclaredMethod("checkLoginInfo");
//        method.insertBefore("if(true)return true;");
//        bytecode = ctClass.toBytecode();
//        output.writeUTF("smartbi.config.BIConfigService");
//        output.writeInt(bytecode.length);
//        output.write(bytecode);
//        output.close();
//        ClassPool pool = ClassPool.getDefault();
//        CtClass ctClass = pool.makeClass(new ByteArrayInputStream(bytecode));
//        CtMethod method = ctClass.getDeclaredMethod("loopCalc");
//        method.insertBefore("System.out.println(\"insert before\");");
//        method.insertAfter("System.out.println(\"insert after\");");
//        method.insertAt(11, "System.out.println(\"line 11 hhh\");");
//        byte[] bytecode1 = ctClass.toBytecode();
//        output.writeUTF(CommandType.REDEFINE_CLASS.name());
//        output.writeUTF("org.hai.jhook.Demo");
//        output.writeInt(bytecode1.length);
//        output.write(bytecode1);
//        output.flush();
    }
}
