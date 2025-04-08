package org.hai.jhook;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ListMethodCommand implements ICommand {
    @Override
    public void execute(ObjectInputStream input, ObjectOutputStream output) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
