package com.navercorp.pinpoint.plugin.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.dubbo.utils.PolarisUtil;
import com.navercorp.pinpoint.plugin.dubbo.utils.StringUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;

public class DubboProviderInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {
        Invoker invoker = (Invoker) args[0];
        URL url = invoker.getUrl();
        PolarisUtil.register("Dubbo", url);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}