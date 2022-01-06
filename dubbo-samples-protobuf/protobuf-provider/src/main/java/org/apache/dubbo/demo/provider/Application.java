/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.demo.provider;

import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

public class Application {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/dubbo-provider.xml");
        context.start();

        ProviderAPI providerAPI = DiscoveryAPIFactory.createProviderAPI();
        regist(providerAPI);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            deregist(providerAPI);
            providerAPI.close();
        }));
        System.out.println("dubbo service started");
        new CountDownLatch(1).await();
    }

    private static void regist(ProviderAPI providerAPI) {
        InstanceRegisterRequest instanceRegisterRequest = getRegisterRequest();
        InstanceRegisterResponse instanceRegisterResponse = providerAPI.register(instanceRegisterRequest);
        System.out.println("response after register is " + instanceRegisterResponse);
    }


    private static InstanceRegisterRequest getRegisterRequest() {
        InstanceRegisterRequest instanceRegisterRequest = new InstanceRegisterRequest();
        instanceRegisterRequest.setNamespace("Dubbo");
        instanceRegisterRequest.setService("DemoService");
        instanceRegisterRequest.setHost("192.168.180.1");
        instanceRegisterRequest.setPort(20880);
        instanceRegisterRequest.setToken("token");
        instanceRegisterRequest.setTtl(5);
        return instanceRegisterRequest;
    }

    private static void deregist(ProviderAPI providerAPI) {
        InstanceDeregisterRequest instanceDeregisterRequest = getDeregisterRequest();
        providerAPI.deRegister(instanceDeregisterRequest);
        System.out.println("deregister for service successfully: " + instanceDeregisterRequest.getService());
    }

    private static InstanceDeregisterRequest getDeregisterRequest() {
        InstanceDeregisterRequest instanceDeregisterRequest = new InstanceDeregisterRequest();
        instanceDeregisterRequest.setNamespace("Dubbo");
        instanceDeregisterRequest.setService("DemoService");
        instanceDeregisterRequest.setHost("127.0.0.1");
        instanceDeregisterRequest.setPort(20880);
        instanceDeregisterRequest.setToken("token");
        return instanceDeregisterRequest;
    }
}
