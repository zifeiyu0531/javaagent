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

package com.tencent.polaris.api.plugin.server;

import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.plugin.Plugin;
import com.tencent.polaris.api.pojo.ServiceEventKey;

/**
 * 【扩展点接口】services server代理，封装了server对接的逻辑
 *
 * @author andrewshan
 * @date 2019/8/21
 */
public interface ServerConnector extends Plugin {

    /**
     * 注册服务监听器
     *
     * @param handler 服务事件处理器
     * @throws PolarisException SDK被释放后，再进行注册会抛异常
     */
    void registerServiceHandler(ServiceEventHandler handler) throws PolarisException;

    /**
     * 反注册服务监听器
     *
     * @param eventKey 服务标识
     * @throws PolarisException SDK被释放后，再进行注册会抛异常
     */
    void deRegisterServiceHandler(ServiceEventKey eventKey) throws PolarisException;

    /**
     * 同步注册服务
     *
     * @param req 注册请求
     * @return 注册应答
     * @throws PolarisException 注册过程出现的错误
     */
    CommonProviderResponse registerInstance(CommonProviderRequest req) throws PolarisException;

    /**
     * 同步反注册服务
     *
     * @param req 反注册请求
     * @throws PolarisException 反注册过程出现的错误
     */
    void deregisterInstance(CommonProviderRequest req) throws PolarisException;

    /**
     * 同步心跳请求
     *
     * @param req 心跳请求
     * @throws PolarisException 心跳上报过程出现的错误
     */
    void heartbeat(CommonProviderRequest req) throws PolarisException;

    /**
     * 上报客户端信息
     *
     * @param req 上报客户端信息
     * @return 上报应答
     * @throws PolarisException PolarisException
     */
    ReportClientResponse reportClient(ReportClientRequest req) throws PolarisException;

    /**
     * 更新服务端地址
     *
     * @param svcEventKey 新的资源key
     * @throws PolarisException 异常场景：当地址列表为空，或者地址全部连接失败，则返回error，调用者需进行重试
     */
    void updateServers(ServiceEventKey svcEventKey);
}
