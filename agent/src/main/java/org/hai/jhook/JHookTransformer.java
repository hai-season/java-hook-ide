package org.hai.jhook;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JHookTransformer implements ClassFileTransformer {
    Map<String, byte[]> map = new ConcurrentHashMap<>();
    public byte[] get(String className) {
        return map.get(className);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        synchronized (this) {
            className = className.replaceAll("/", ".");
            map.putIfAbsent(className, classfileBuffer);
        }
        return null;
    }
}
