package com.zifeiyu;

import java.lang.instrument.Instrumentation;

public class MyAgent {

    /**
     * 参数args是启动参数
     * 参数inst是JVM启动时传入的Instrumentation实现
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("premain方法会在main方法之前执行......");
        inst.addTransformer(new MyTransformClass());
    }
}
