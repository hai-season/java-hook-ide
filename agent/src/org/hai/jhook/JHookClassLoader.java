package org.hai.jhook;

public class JHookClassLoader extends ClassLoader {
    public Class<?> loadClass(String name, byte[] code) throws ClassNotFoundException {
        return defineClass(name, code, 0, code.length);
    }
}
