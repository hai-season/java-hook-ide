package org.hai.jhook;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

public class JHookServer implements Runnable {
    @Override
    public void run() {
        // TODO 同名的类 各自重定义
        // TODO 如何获取类的字节码
        System.out.println("start agent...");
        try {
            ServerSocket server = new ServerSocket(9090);
            Socket client = server.accept();
            System.out.println(client);
            ObjectInputStream input = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
            MyTransformer trans = new MyTransformer();
            InstrumentationHolder.getInst().addTransformer(trans, true);
            while (!client.isClosed()) {
                String cmdTypeStr = input.readUTF();
                System.out.println(cmdTypeStr);
                System.out.println(CommandType.GET_ALL_CLASSES);
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
                    Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
                    Optional<Class> clazz = Arrays.stream(classes).filter(c -> c.getName().equals(className)).findFirst();
                    Class aClass = clazz.get();
                    int len = input.readInt();
                    byte[] code = new byte[len];
                    input.readFully(code);
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
                }
                output.writeUTF("ok");
                output.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
