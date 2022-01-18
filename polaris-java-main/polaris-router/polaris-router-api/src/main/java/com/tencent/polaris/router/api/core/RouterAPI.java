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

package com.tencent.polaris.router.api.core;

import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceRequest;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceResponse;
import com.tencent.polaris.router.api.rpc.ProcessRoutersRequest;
import com.tencent.polaris.router.api.rpc.ProcessRoutersResponse;

/**
 * 单独操作插件方法的路由相关API，供其他框架集成使用
 */
public interface RouterAPI {

    /**
     * 执行路由链
     *
     * @param request 路由链列表，包含前置路由链，主链，以及后置路由链
     * @return 服务实例列表
     * @throws PolarisException 错误码及错误信息
     */
    ProcessRoutersResponse processRouters(ProcessRoutersRequest request) throws PolarisException;

    /**
     * 执行负载均衡器
     *
     * @param request 负载均衡策略，以及服务列表
     * @return 负载均衡后的实例信息
     * @throws PolarisException 错误码及错误信息
     */
    ProcessLoadBalanceResponse processLoadBalance(ProcessLoadBalanceRequest request) throws PolarisException;

}
