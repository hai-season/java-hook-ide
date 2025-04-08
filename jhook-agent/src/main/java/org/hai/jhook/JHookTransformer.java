package org.hai.jhook;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JHookTransformer implements ClassFileTransformer {
    private static JHookTransformer instance = new JHookTransformer();
    Map<String, byte[]> map = new ConcurrentHashMap<>();

    public byte[] get(String className) {
        return map.get(className);
    }

    public static JHookTransformer getInstance() {
        return instance;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        className = className.replaceAll("/", ".");
        map.putIfAbsent(className, classFileBuffer);
        return null;
    }
}
