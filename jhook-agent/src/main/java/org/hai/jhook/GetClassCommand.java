package org.hai.jhook;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Optional;

public class GetClassCommand implements ICommand {
    @Override
    public void execute(ObjectInputStream input, ObjectOutputStream output) {
        try {
            String className = input.readUTF();
            Class[] classes = InstrumentationHolder.getInst().getAllLoadedClasses();
            Optional<Class> clazz = Arrays.stream(classes).filter(c -> c.getName().equals(className)).findFirst();
            Class aClass = clazz.get();
            InstrumentationHolder.getInst().retransformClasses(aClass);
            output.writeInt(JHookTransformer.getInstance().get(className).length);
            output.write(JHookTransformer.getInstance().get(className));
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
