/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.polaris.factory.config.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.polaris.api.config.consumer.ConsumerConfig;
import com.tencent.polaris.factory.util.ConfigUtils;

/**
 * 调用者配置对象
 *
 * @author andrewshan
 * @date 2019/8/20
 */
public class ConsumerConfigImpl implements ConsumerConfig {

    @JsonProperty
    private LocalCacheConfigImpl localCache;

    @JsonProperty
    private ServiceRouterConfigImpl serviceRouter;

    @JsonProperty
    private LoadBalanceConfigImpl loadbalancer;

    @JsonProperty
    private CircuitBreakerConfigImpl circuitBreaker;

    @JsonProperty
    private OutlierDetectionConfigImpl outlierDetection;

    @Override
    public LocalCacheConfigImpl getLocalCache() {
        return localCache;
    }

    @Override
    public ServiceRouterConfigImpl getServiceRouter() {
        return serviceRouter;
    }

    @Override
    public LoadBalanceConfigImpl getLoadbalancer() {
        return loadbalancer;
    }

    public CircuitBreakerConfigImpl getCircuitBreaker() {
        return circuitBreaker;
    }

    @Override
    public OutlierDetectionConfigImpl getOutlierDetection() {
        return outlierDetection;
    }

    @Override
    public void verify() {
        ConfigUtils.validateNull(localCache, "localCache");
        ConfigUtils.validateNull(serviceRouter, "serviceRouter");
        ConfigUtils.validateNull(loadbalancer, "loadbalancer");
        ConfigUtils.validateNull(circuitBreaker, "circuitBreaker");
        ConfigUtils.validateNull(outlierDetection, "outlierDetection");
        localCache.verify();
        serviceRouter.verify();
        loadbalancer.verify();
        circuitBreaker.verify();
        outlierDetection.verify();
    }

    @Override
    public void setDefault(Object defaultObject) {
        if (null == localCache) {
            localCache = new LocalCacheConfigImpl();
        }
        if (null == serviceRouter) {
            serviceRouter = new ServiceRouterConfigImpl();
        }
        if (null == loadbalancer) {
            loadbalancer = new LoadBalanceConfigImpl();
        }
        if (null == circuitBreaker) {
            circuitBreaker = new CircuitBreakerConfigImpl();
        }
        if (null == outlierDetection) {
            outlierDetection = new OutlierDetectionConfigImpl();
        }
        if (null != defaultObject) {
            ConsumerConfig consumerConfig = (ConsumerConfig) defaultObject;
            localCache.setDefault(consumerConfig.getLocalCache());
            serviceRouter.setDefault(consumerConfig.getServiceRouter());
            loadbalancer.setDefault(consumerConfig.getLoadbalancer());
            circuitBreaker.setDefault(consumerConfig.getCircuitBreaker());
            outlierDetection.setDefault(consumerConfig.getOutlierDetection());
        }
    }

    @Override
    @SuppressWarnings("checkstyle:all")
    public String toString() {
        return "ConsumerConfigImpl{" +
                "localCache=" + localCache +
                ", serviceRouter=" + serviceRouter +
                ", loadbalancer=" + loadbalancer +
                ", circuitBreaker=" + circuitBreaker +
                ", outlierDetection=" + outlierDetection +
                '}';
    }
}
