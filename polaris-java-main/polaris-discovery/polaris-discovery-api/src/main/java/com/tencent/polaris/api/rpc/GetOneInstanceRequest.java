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

package com.tencent.polaris.api.rpc;

import com.tencent.polaris.api.pojo.ServiceMetadata;
import java.util.Map;

/**
 * 单个服务实例查询请求
 *
 * @author andrewshan
 * @date 2019/8/21
 */
public class GetOneInstanceRequest extends RequestBaseEntity {

    /**
     * 可选，服务元数据信息，用于服务路由过滤
     */
    private Map<String, String> metadata;

    /**
     * 所属的金丝雀集群
     */
    private String canary;

    /**
     * 可选，负载均衡辅助参数
     */
    private Criteria criteria;

    /**
     * 接口参数
     */
    private String method;

    /**
     * 可选, metadata失败降级策略
     */
    private MetadataFailoverType metadataFailoverType;

    /**
     * 主调方服务信息
     */
    private ServiceMetadata serviceInfo;

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getCanary() {
        return canary;
    }

    public void setCanary(String canary) {
        this.canary = canary;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ServiceMetadata getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceMetadata serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public MetadataFailoverType getMetadataFailoverType() {
        return metadataFailoverType;
    }

    public void setMetadataFailoverType(MetadataFailoverType metadataFailoverType) {
        this.metadataFailoverType = metadataFailoverType;
    }

    @Override
    @SuppressWarnings("checkstyle:all")
    public String toString() {
        return "GetOneInstanceRequest{" +
                "metadata=" + metadata +
                ", canary='" + canary + '\'' +
                ", criteria=" + criteria +
                ", method='" + method + '\'' +
                ", metadataFailoverType=" + metadataFailoverType +
                ", serviceInfo=" + serviceInfo +
                "} " + super.toString();
    }
}
