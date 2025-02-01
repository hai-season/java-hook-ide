package org.hai.jhook;

import java.lang.instrument.Instrumentation;

public class JHookAgent {
    public static void premain(String args, Instrumentation inst) {
        startAgent(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        startAgent(args, inst);
    }

    public static void startAgent(String args, Instrumentation inst) {
        // TODO 根据参数启动服务器
        // TODO 避免重复启动
        InstrumentationHolder.setInst(inst);
        Thread thread = new Thread(new JHookServer());
        thread.setDaemon(true);
        thread.start();
    }
}
