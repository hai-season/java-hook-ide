package org.hai.jhook;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JHookServer implements Runnable {
    @Override
    public void run() {
        // TODO 同名的类 各自重定义
        // TODO 如何获取类的字节码
        System.out.println("start agent...");
        try {
            ServerSocket server = new ServerSocket(9090);
            while (true) {
                Socket client = server.accept();
                new Thread(() -> {
                    try {
                        System.out.println(client);
                        System.err.println("readStreamHeader");
                        ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                        System.out.println("writeStreamHeader");
                        ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                        JHookTransformer trans = new JHookTransformer();
                        InstrumentationHolder.getInst().addTransformer(trans, true);
                        while (!client.isClosed()) {
                            String cmdTypeStr = "";
                            try {
                                cmdTypeStr = input.readUTF();
                            } catch (Exception e) {
                                break;
                            }
                            System.out.println(cmdTypeStr);
                            CommandType cmdType = CommandType.valueOf(cmdTypeStr);
                            if (cmdType == CommandType.GET_STATUS) {

                            } else if (cmdType == CommandType.GET_CLASS) {
                                String className = input.readUTF();
                                Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
                                Optional<Class> clazz = Arrays.stream(classes).filter(c -> c.getName().equals(className)).findFirst();
                                Class aClass = clazz.get();
                                InstrumentationHolder.getInst().retransformClasses(aClass);
                                output.writeInt(trans.get(className).length);
                                output.write(trans.get(className));
                                output.flush();
                            } else if (cmdType == CommandType.REDEFINE_CLASS) {
                                String className = input.readUTF();
                                String methodName = input.readUTF();
                                String location = input.readUTF();
                                String snippets = input.readUTF();
                                Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
                                Optional<Class> clazz = Arrays.stream(classes).filter(c -> c.getName().equals(className)).findFirst();
                                Class aClass = clazz.get();
                                InstrumentationHolder.getInst().retransformClasses(aClass);
                                byte[] code = trans.get(className);
                                ClassPool pool = ClassPool.getDefault();
                                pool.appendClassPath(new ClassClassPath(aClass));
                                pool.getCtClass(className).defrost();
                                CtClass ctClass = pool.makeClass(new ByteArrayInputStream(code));
                                CtMethod method = ctClass.getDeclaredMethod(methodName); // TODO 重名方法
                                if (location.equals("before")) {
                                    method.insertBefore(snippets);
                                } else if (location.equals("after")) {
                                    method.insertAfter(snippets);
                                }
                                code = ctClass.toBytecode();
                                InstrumentationHolder.getInst().redefineClasses(new ClassDefinition(aClass, code));
                            } else if (cmdType == CommandType.GET_ALL_CLASSES) {
                                Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
                                output.writeInt(classes.length);
                                Arrays.stream(classes).forEach(c -> {
                                    try {
                                        String className = c.getName();
                                        if (className.contains("/")) {
                                            className = className.substring(0, className.indexOf("/"));
                                        }
                                        if (!className.startsWith("java.")
                                                && !className.startsWith("sun.")
                                                && !className.startsWith("com.sun.")
                                                && !className.startsWith("javafx.")
                                                && !className.startsWith("javax.")
                                                && !className.startsWith("jdk.")) {
                                            if (InstrumentationHolder.getInst().isModifiableClass(c)) {
                                                InstrumentationHolder.getInst().retransformClasses(c);
                                                output.writeUTF(className);
                                                byte[] code = trans.get(className);
                                                if (code == null) {
                                                    code = new byte[1];
                                                    System.out.println("error: " + className);
                                                }
                                                output.writeInt(code.length);
                                                output.write(code);
                                            } else {
                                                System.out.println("unmodifiable " + c);
                                            }
                                        }
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                output.flush();
                            } else if (cmdType == CommandType.LIST_CLASS) {
                                Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
                                List<String> result = new ArrayList<>();
                                for (Class<?> clazz : classes) {
                                    String classLoader = clazz.getClassLoader() != null ? clazz.getClassLoader().getClass().toString() : "BootstrapClassLoader";
                                    result.add(clazz.getName() + "[" + classLoader + "]");
                                }
                                output.writeObject(result);
                                output.flush();
                            } else if (cmdType == CommandType.LIST_METHOD) {
                                String className = input.readUTF();
                                // TODO 同名的不同类
                                Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
                                List<String> methodList = new ArrayList<>();
                                for (Class<?> clazz : classes) {
                                    if (clazz.getName().equals(className)) {
                                        Method[] methods = clazz.getDeclaredMethods(); // TODO 其他方法
                                        for (Method method : methods) {
                                            methodList.add(method.getName());
                                        }
                                        break;
                                    }
                                }
                                output.writeObject(methodList);
                                output.flush();
                            }
                            output.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
