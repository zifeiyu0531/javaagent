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
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.cluster.RouterChain;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;

import java.util.ArrayList;
import java.util.List;

public class DubboConsumerInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {
        AbstractClusterInvoker clusterInvoker = (AbstractClusterInvoker) target;

        String namespace = "Dubbo";
        String service = StringUtil.serviceNameTransform(((Invocation) args[0]).getServiceName());
        Instance targetInstance = PolarisUtil.getTargetInstance(namespace, service);

        String address = StringUtil.buildAdress(targetInstance.getHost(), targetInstance.getPort());
        Invoker targetInvoker = InvokerMap.get(address);
        if (targetInvoker == null) {
            return;
        }
        List<Invoker> newInvokers = new ArrayList<>();
        newInvokers.add(targetInvoker);

        RegistryDirectory directory = (RegistryDirectory) clusterInvoker.getDirectory();
        RouterChain routerChain = directory.getRouterChain();
        routerChain.setInvokers(newInvokers);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        if (invocation == null) {
            return;
        }
        String namespace = "Dubbo";
        String service = StringUtil.serviceNameTransform(((Invocation) args[0]).getServiceName());
        URL url = invocation.getInvoker().getUrl();
        PolarisUtil.polarisAfterHandler(namespace, service, url.getHost(), url.getPort());
    }
}
