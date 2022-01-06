package com.navercorp.pinpoint.plugin.dubbo3.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenMap {
    // key: address     value: token
    private static Map<String, String> tokenMap = new ConcurrentHashMap<>();

    public static String getToken(String address) {
        return tokenMap.get(address);
    }

    public static void setToken(String address, String token) {
        tokenMap.put(address, token);
    }

    public static Map<String, String> getTokenMap() {
        return tokenMap;
    }
}
