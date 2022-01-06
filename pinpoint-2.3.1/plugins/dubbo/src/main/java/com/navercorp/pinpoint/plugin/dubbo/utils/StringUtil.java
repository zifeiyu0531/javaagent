package com.navercorp.pinpoint.plugin.dubbo.utils;

public class StringUtil {
    public static String serviceNameTransform(String serviceName) {
        if (serviceName == null) {
            return "";
        }
        String[] splitList = serviceName.split("\\.");
        if (splitList.length == 0) {
            return "";
        }
        return splitList[splitList.length - 1];
    }

    public static String buildAdress(String host, int port) {
        if (port <= 0) {
            return host;
        }
        return host + ":" + port;
    }
}
