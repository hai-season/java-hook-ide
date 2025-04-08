package org.hai.jhook;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ListClassCommand implements ICommand {
    @Override
    public void execute(ObjectInputStream input, ObjectOutputStream output) {
        try {
            Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
            List<String> result = new ArrayList<>();
            for (Class<?> clazz : classes) {
                String classLoader = clazz.getClassLoader() != null ? clazz.getClassLoader().getClass().toString() : "BootstrapClassLoader";
                result.add(clazz.getName() + "[" + classLoader + "]");
            }
            output.writeObject(result);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
