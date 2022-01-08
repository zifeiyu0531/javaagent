package com.navercorp.pinpoint.plugin.dubbo.utils;

import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.rpc.*;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Result;

@SuppressWarnings("Duplicates")
public class PolarisUtil {
    private static final ConsumerAPI consumerAPI = DiscoveryAPIFactory.createConsumerAPI();
    private static final ProviderAPI providerAPI = DiscoveryAPIFactory.createProviderAPI();

    public static void register(String namespace, URL url) {
        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace(namespace);
        instanceRegisterRequest.setService(url.getServiceInterface());
        instanceRegisterRequest.setHost(url.getHost());
        instanceRegisterRequest.setPort(url.getPort());
        instanceRegisterRequest.setMetadata(url.getParameters());
        InstanceRegisterResponse instanceRegisterResponse = providerAPI.register(instanceRegisterRequest);
        System.out.println("response after register is " + instanceRegisterResponse);
    }

    public static Instance getTargetInstance(String namespace, String service) {
        System.out.println("namespace " + namespace + ", service " + service);
        GetOneInstanceRequest getOneInstanceRequest = new GetOneInstanceRequest();
        getOneInstanceRequest.setNamespace(namespace);
        getOneInstanceRequest.setService(service);
        System.out.println("request set complete");
        InstancesResponse oneInstance = consumerAPI.getOneInstance(getOneInstanceRequest);
        Instance[] instances = oneInstance.getInstances();
        System.out.println("instances count is " + instances.length);
        Instance targetInstance = instances[0];
        System.out.printf("target instance is %s:%d%n", targetInstance.getHost(), targetInstance.getPort());
        return targetInstance;
    }

    public static void reportInvokeResult(String namespace, String service, URL url, long delay, Result result, Throwable throwable) {
        String host = url.getHost();
        int port = url.getPort();
        ServiceCallResult serviceCallResult = new ServiceCallResult();
        serviceCallResult.setNamespace(namespace);
        serviceCallResult.setService(service);
        serviceCallResult.setHost(host);
        serviceCallResult.setPort(port);
        serviceCallResult.setDelay(delay);
        serviceCallResult.setRetStatus(null == throwable ? RetStatus.RetSuccess : RetStatus.RetFail);
        serviceCallResult.setRetCode(null != result ? 200 : -1);
        try {
            consumerAPI.updateServiceCallResult(serviceCallResult);
            System.out.println("success to call updateServiceCallResult");
        } catch (PolarisException e) {
            System.out.println(e.getMessage());
        }
    }
}
