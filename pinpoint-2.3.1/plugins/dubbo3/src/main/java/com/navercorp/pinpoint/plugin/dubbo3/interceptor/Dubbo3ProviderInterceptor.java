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
import com.navercorp.pinpoint.plugin.dubbo3.utils.InterceptorUtil;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.RpcInvocation;

/**
 * @author K
 */
public class Dubbo3ProviderInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        if (invocation == null || InterceptorUtil.isGetMetaInfo(invocation)) {
            return;
        }
        System.out.println("provider before interceptor start\n");

        System.out.println("provider before interceptor end\n");
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        if (invocation == null || InterceptorUtil.isGetMetaInfo(invocation)) {
            return;
        }
        System.out.println("provider after interceptor start\n");
        AsyncRpcResult rpcResult = (AsyncRpcResult) result;
        InterceptorUtil.resultHandler(rpcResult);
        System.out.println("provider after interceptor start\n");
    }
}