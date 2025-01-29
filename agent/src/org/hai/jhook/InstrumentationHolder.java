package org.hai.jhook;

import java.lang.instrument.Instrumentation;

public class InstrumentationHolder {
    private static Instrumentation inst;

    public static Instrumentation getInst() {
        return inst;
    }

    public static void setInst(Instrumentation inst) {
        InstrumentationHolder.inst = inst;
    }
}
