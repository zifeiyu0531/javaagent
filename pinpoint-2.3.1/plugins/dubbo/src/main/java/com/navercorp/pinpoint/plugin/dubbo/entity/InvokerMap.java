package com.navercorp.pinpoint.plugin.dubbo.entity;

import org.apache.dubbo.rpc.Invoker;

import java.util.concurrent.ConcurrentHashMap;

public class InvokerMap {
    private static ConcurrentHashMap<String, Invoker> map = new ConcurrentHashMap<>();

    public static void put(String key, Invoker invoker) {
        map.put(key, invoker);
    }

    public static Invoker get(String key) {
        return map.get(key);
    }
}
