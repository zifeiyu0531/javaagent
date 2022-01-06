package com.navercorp.pinpoint.plugin.dubbo3.entity;

public class Service {
    private String namespace;
    private String serviceName;

    public Service() {
        this("", "");
    }

    public Service(String namespace, String serviceName) {
        this.namespace = namespace;
        this.serviceName = serviceName;
    }

    public String getNamespace() {
        return this.namespace == null ? "" : this.namespace;
    }

    public String getServiceName() {
        return this.serviceName == null ? "" : this.serviceName;
    }
}
