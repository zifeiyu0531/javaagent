package com.navercorp.pinpoint.plugin.dubbo.utils;

import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.rpc.GetOneInstanceRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.rpc.ServiceCallResult;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;

@SuppressWarnings("Duplicates")
public class PolarisUtil {
    private static final ConsumerAPI consumerAPI = DiscoveryAPIFactory.createConsumerAPI();

    public static Instance getTargetInstance(String namespace, String serviceName) {
        System.out.println("namespace " + namespace + ", service " + serviceName);
        GetOneInstanceRequest getOneInstanceRequest = new GetOneInstanceRequest();
        getOneInstanceRequest.setNamespace(namespace);
        getOneInstanceRequest.setService(serviceName);
        System.out.println("request set complete");
        InstancesResponse oneInstance = consumerAPI.getOneInstance(getOneInstanceRequest);
        Instance[] instances = oneInstance.getInstances();
        System.out.println("instances count is " + instances.length);
        Instance targetInstance = instances[0];
        System.out.printf("target instance is %s:%d%n", targetInstance.getHost(), targetInstance.getPort());
        return targetInstance;
    }

    public static void polarisAfterHandler(String namespace, String serviceName, String host, int port) {
        RetStatus status = RetStatus.RetSuccess;
        ServiceCallResult result = new ServiceCallResult();
        result.setNamespace(namespace);
        result.setService(serviceName);
        result.setHost(host);
        result.setPort(port);
        //result.setRetCode(httpResult.code);
        //result.setDelay(delay);
        result.setRetStatus(status);
        try {
            consumerAPI.updateServiceCallResult(result);
            System.out.println("success to call updateServiceCallResult");
        } catch (PolarisException e) {
            System.out.println(e.getMessage());
        }
    }
}
