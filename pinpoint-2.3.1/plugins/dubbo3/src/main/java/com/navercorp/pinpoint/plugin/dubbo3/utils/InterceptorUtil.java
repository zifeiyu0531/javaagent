package com.navercorp.pinpoint.plugin.dubbo3.utils;

import com.navercorp.pinpoint.plugin.dubbo3.entity.Service;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.rpc.GetOneInstanceRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.api.rpc.ServiceCallResult;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;

import java.util.concurrent.TimeUnit;

public class InterceptorUtil {
    private static final ConsumerAPI consumerAPI = DiscoveryAPIFactory.createConsumerAPI();

    public static Service invocationHandler(RpcInvocation invocation) {
        String serviceName = serviceNameTransform(invocation.getServiceName());
        return new Service("Dubbo", serviceName);
    }

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

    public static void resultHandler(AsyncRpcResult rpcResult) {
        try {
            System.out.println("result.get():" + rpcResult.get(2147483647L, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Instance polarisBeforeHandler(String namespace, String serviceName) {
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

    public static void polarisAfterHandler(String namespace, String serviceName, AsyncRpcResult rpcResult,Instance targetInstance){
        try {
            System.out.println("result.get():" + rpcResult.get(2147483647L, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        RetStatus status = RetStatus.RetSuccess;
        ServiceCallResult result = new ServiceCallResult();
        result.setNamespace(namespace);
        result.setService(serviceName);
        result.setHost(targetInstance.getHost());
        result.setPort(targetInstance.getPort());
        //result.setRetCode(httpResult.code);
        //result.setDelay(delay);
        result.setRetStatus(status);
        consumerAPI.updateServiceCallResult(result);
        System.out.println("success to call updateServiceCallResult");
    }

    public static boolean isGetMetaInfo(RpcInvocation invocation) {
        return invocation.getMethodName().equals("getMetadataInfo");
    }
}
