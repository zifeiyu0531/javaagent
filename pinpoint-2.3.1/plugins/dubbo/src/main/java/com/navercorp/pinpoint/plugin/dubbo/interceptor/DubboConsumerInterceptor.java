package com.navercorp.pinpoint.plugin.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.dubbo.entity.InvokerMap;
import com.navercorp.pinpoint.plugin.dubbo.utils.PolarisUtil;
import com.navercorp.pinpoint.plugin.dubbo.utils.StringUtil;
import com.tencent.polaris.api.pojo.Instance;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.integration.RegistryDirectory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.cluster.RouterChain;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;

import java.util.ArrayList;
import java.util.List;

public class DubboConsumerInterceptor implements AroundInterceptor {
    private long startTimeMilli;

    @Override
    public void before(Object target, Object[] args) {
        AbstractClusterInvoker clusterInvoker = (AbstractClusterInvoker) target;
        Invocation invocation = (Invocation)args[0];

        String namespace = "Dubbo";
        String service = invocation.getServiceName();
        Instance targetInstance = PolarisUtil.getTargetInstance(namespace, service);

        String address = StringUtil.buildAdress(targetInstance.getHost(), targetInstance.getPort());
        Invoker targetInvoker = InvokerMap.get(address);
        if (targetInvoker == null) {
            System.out.println("polaris target invoker not found, use default invoker selected by dubbo");
            return;
        }
        List<Invoker> newInvokers = new ArrayList<>();
        newInvokers.add(targetInvoker);

        RegistryDirectory directory = (RegistryDirectory) clusterInvoker.getDirectory();
        RouterChain routerChain = directory.getRouterChain();
        routerChain.setInvokers(newInvokers);
        this.startTimeMilli = System.currentTimeMillis();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        long delay = System.currentTimeMillis() - this.startTimeMilli;
        System.out.println("delay: " + delay);
        Invocation invocation = (Invocation) args[0];
        URL url = invocation.getInvoker().getUrl();
        String namespace = "Dubbo";
        String service = invocation.getServiceName();
        PolarisUtil.reportInvokeResult(namespace, service, url, delay, (Result) result, throwable);
    }
}
