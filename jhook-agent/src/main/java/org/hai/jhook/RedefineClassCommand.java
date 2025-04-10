package org.hai.jhook;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassDefinition;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RedefineClassCommand implements ICommand {
    private static final CommandType type = CommandType.REDEFINE_CLASS;
    private static final ConcurrentHashMap<String, byte[]> classCodeMap = new ConcurrentHashMap<>(); // TODO 并发处理

    @Override
    public void execute(ObjectInputStream input, ObjectOutputStream output) {
        try {
            String className = input.readUTF();
            String methodName = input.readUTF();
            String location = input.readUTF();
            Integer line = input.readInt();
            String snippets = input.readUTF();
            if (location.equals("reset")) {
                classCodeMap.remove(className);
            }
            Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
            Optional<Class> clazz = Arrays.stream(classes).filter(c -> c.getName().equals(className)).findFirst();
            Class aClass = clazz.get();
            byte[] code = null;
            if (classCodeMap.containsKey(className)) {
                code = classCodeMap.get(className);
            } else {
                InstrumentationHolder.getInst().retransformClasses(aClass);
                code = JHookTransformer.getInstance().get(className);
            }
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new ClassClassPath(aClass));
            pool.getCtClass(className).defrost();
            CtClass ctClass = pool.makeClass(new ByteArrayInputStream(code));
            CtMethod method = ctClass.getDeclaredMethod(methodName); // TODO 重名方法
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
            classCodeMap.put(className, code);
            InstrumentationHolder.getInst().redefineClasses(new ClassDefinition(aClass, code));
            output.writeUTF("ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
