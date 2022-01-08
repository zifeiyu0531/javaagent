package com.navercorp.pinpoint.plugin.dubbo.utils;

public class StringUtil {
    public static String buildAdress(String host, int port) {
        if (port <= 0) {
            return host;
        }
        return host + ":" + port;
    }
}
