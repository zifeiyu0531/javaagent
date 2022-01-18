package org.apache.dubbo.rpc.cluster.support;

import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.rpc.GetOneInstanceRequest;
import com.tencent.polaris.api.rpc.InstancesResponse;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.ClusterInvoker;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClusterInvoker<T> implements ClusterInvoker<T> {
    private static ConsumerAPI CONSUMER_API;

    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (CONSUMER_API == null) {
            CONSUMER_API = DiscoveryAPIFactory.createConsumerAPI();
        }
        String namespace = "Dubbo";
        String service = invocation.getServiceName();

        GetOneInstanceRequest getOneInstanceRequest = new GetOneInstanceRequest();
        getOneInstanceRequest.setNamespace(namespace);
        getOneInstanceRequest.setService(service);
        InstancesResponse oneInstance = CONSUMER_API.getOneInstance(getOneInstanceRequest);
        Instance[] instances = oneInstance.getInstances();
        Instance targetInstance = instances[0];
        System.out.printf("target instance is %s:%d%n", targetInstance.getHost(), targetInstance.getPort());

        for (Invoker<T> invoker : invokers) {
            URL url = invoker.getUrl();
            if (url.getHost().equals(targetInstance.getHost()) && url.getPort() == targetInstance.getPort()) {
                return invoker;
            }
        }
        return invokers.get(0);
    }
}
