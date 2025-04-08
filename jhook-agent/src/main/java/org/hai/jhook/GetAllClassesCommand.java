package org.hai.jhook;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class GetAllClassesCommand implements ICommand {
    @Override
    public void execute(ObjectInputStream input, ObjectOutputStream output) {
        try {
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
                            byte[] code = JHookTransformer.getInstance().get(className);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
