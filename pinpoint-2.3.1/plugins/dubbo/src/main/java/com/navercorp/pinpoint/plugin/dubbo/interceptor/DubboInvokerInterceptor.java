package com.navercorp.pinpoint.plugin.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.dubbo.entity.InvokerMap;
import org.apache.dubbo.rpc.Invoker;

public class DubboInvokerInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // 记录host:port和注册的invoker的映射关系
        Invoker invoker = (Invoker) result;
        String address = invoker.getUrl().getAddress(); // host:port
        InvokerMap.put(address, invoker);
    }
}
