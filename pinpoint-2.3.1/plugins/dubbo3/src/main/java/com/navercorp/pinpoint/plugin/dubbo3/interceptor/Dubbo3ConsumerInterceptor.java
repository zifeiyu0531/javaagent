/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.dubbo3.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.dubbo3.entity.Service;
import com.navercorp.pinpoint.plugin.dubbo3.entity.TokenMap;
import com.navercorp.pinpoint.plugin.dubbo3.utils.InterceptorUtil;
import com.navercorp.pinpoint.plugin.dubbo3.utils.ReflectUtil;
import com.tencent.polaris.api.pojo.Instance;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.exchange.ExchangeClient;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.protocol.AbstractInvoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;

import java.util.HashMap;
import java.util.Map;

import static org.apache.dubbo.rpc.Constants.TOKEN_KEY;

/**
 * @author K
 */
public class Dubbo3ConsumerInterceptor implements AroundInterceptor {
    private Instance targetInstance = null;

    @Override
    public void before(Object target, Object[] args) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        if (invocation == null || InterceptorUtil.isGetMetaInfo(invocation)) {
            return;
        }
        System.out.println("\nconsumer before interceptor start");

        Service service = new Service("Dubbo", InterceptorUtil.serviceNameTransform(invocation.getServiceName()));
        if (service == null) {
            System.out.println("service is null");
            return;
        }

        this.targetInstance = InterceptorUtil.polarisBeforeHandler(service.getNamespace(), service.getServiceName());
        if (this.targetInstance == null) {
            System.out.println("get targetInstance fail");
            return;
        }

        AbstractInvoker invoker = (AbstractInvoker) target;
        // 如果dubbo的目的url与polaris的目的url相同，则不做处理
        if (invoker.getUrl().getHost().equals(targetInstance.getHost()) &&
                invoker.getUrl().getPort() == targetInstance.getPort()) {
            System.out.println("same address, no need change");
            checkInvocationToken(args[0], invoker.getUrl());
            return;
        }

        URL url = makeNewURL(invoker, targetInstance.getHost(), targetInstance.getPort());
        if (url == null) {
            System.out.println("make new url fail");
            return;
        }

        checkInvocationToken(args[0], url);
        changeTargetURL(target, url);
        changeTargetClientByURL(target, url);

        // 修改context内容
        RpcContext context = RpcContext.getContext();
        context.setRemoteAddress(targetInstance.getHost(), targetInstance.getPort());
        context.setInvoker((AbstractInvoker) target);
        context.setUrl(url);

        System.out.println("consumer before interceptor end\n");
    }

    private URL makeNewURL(AbstractInvoker invoker, String newHost, int newPort) {
        // 根据polaris提供的host port生成新URL
        URL url = invoker.getUrl().setHost(newHost).setPort(newPort);
        Map<String, String> params = (Map<String, String>) ReflectUtil.getObjectByFieldName(url, "parameters");
        if (params == null) {
            return null;
        }
        String address = url.getAddress();
        String token = TokenMap.getToken(address);
        // 修改url中的token防止鉴权错误
        Map<String, String> parameters = new HashMap<>(params);
        parameters.put(TOKEN_KEY, token);
        ReflectUtil.setFinalValueByFieldName(url, "parameters", parameters);
        return url;
    }

    private void checkInvocationToken(Object inv, URL url) {
        // 检查invocation的token和url的token是否一致
        if (!(((Invocation) inv).getObjectAttachment(TOKEN_KEY) == url.getParameter(TOKEN_KEY))) {
            changeInvocationToken(inv, url);
        }
    }

    private void changeInvocationToken(Object inv, URL url) {
        // 修改invocation的token
        String token = url.getParameter(TOKEN_KEY);
        ((Invocation) inv).setObjectAttachment(TOKEN_KEY, token);
    }

    private void changeTargetURL(Object target, URL url) {
        // 将DubboInvoker对象的url改为polaris指定的url
        ReflectUtil.setSuperFinalValueByFieldName(target, "url", url);
    }

    private void changeTargetClientByURL(Object target, URL url) {
        // 将DubboInvoker对象的client改为新url对应的client
        DubboProtocol protocol = DubboProtocol.getDubboProtocol();
        try {
            Object clients = ReflectUtil.invokeMethodByName(protocol, "getClients", url);
            if (!(clients instanceof ExchangeClient[])) {
                System.out.println("method invoke fail");
                return;
            }
            ReflectUtil.setFinalValueByFieldName(target, "clients", clients);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        if (invocation == null || InterceptorUtil.isGetMetaInfo(invocation)) {
            return;
        }
        System.out.println("\nconsumer after interceptor start");

        Service service = new Service("Dubbo", InterceptorUtil.serviceNameTransform(invocation.getServiceName()));
        AsyncRpcResult rpcResult = (AsyncRpcResult) result;
        if (this.targetInstance != null) {
            InterceptorUtil.polarisAfterHandler(service.getNamespace(), service.getServiceName(), rpcResult, this.targetInstance);
        } else {
            InterceptorUtil.resultHandler(rpcResult);
        }

        System.out.println("consumer after interceptor endd\n");
    }
}
