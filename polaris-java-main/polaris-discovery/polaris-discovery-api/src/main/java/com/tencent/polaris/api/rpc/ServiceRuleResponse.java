/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 - 2020. THL A29 Limited, a Tencent company. All rights reserved.
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

import com.tencent.polaris.api.pojo.ServiceRule;

/**
 * Response for instances query request.
 *
 * @author andrewshan
 * @date 2019/8/21
 */
public class ServiceRuleResponse extends BaseEntity {

    private final ServiceRule serviceRule;

    public ServiceRuleResponse(ServiceRule serviceRule) {
        this.serviceRule = serviceRule;
    }

    @Override
    public String toString() {
        return "ServiceRuleResponse{ service_rule=" + serviceRule + " }" + super.toString();
    }

    public ServiceRule getServiceRule() {
        return serviceRule;
    }
}
