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

public class RedefineClassCommand implements ICommand {
    private static final CommandType type = CommandType.REDEFINE_CLASS;

    @Override
    public void execute(ObjectInputStream input, ObjectOutputStream output) {
        try {
            String className = input.readUTF();
            String methodName = input.readUTF();
            String location = input.readUTF();
            Integer line = input.readInt();
            String snippets = input.readUTF();
            Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
            Optional<Class> clazz = Arrays.stream(classes).filter(c -> c.getName().equals(className)).findFirst();
            Class aClass = clazz.get();
            InstrumentationHolder.getInst().retransformClasses(aClass);
            byte[] code = JHookTransformer.getInstance().get(className);
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
            InstrumentationHolder.getInst().redefineClasses(new ClassDefinition(aClass, code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
