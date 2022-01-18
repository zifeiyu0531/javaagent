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

package com.tencent.polaris.api.core;

import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceHeartbeatRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import java.io.Closeable;

/**
 * 被调端相关的接口API
 *
 * @author andrewshan
 * @date 2019/8/21
 */
public interface ProviderAPI extends AutoCloseable, Closeable {

    /**
     * 同步注册服务实例
     *
     * @param req 注册请求
     * @return 服务实例ID
     * @throws PolarisException 错误码及异常信息
     */
    InstanceRegisterResponse register(InstanceRegisterRequest req) throws PolarisException;

    /**
     * 同步反注册服务实例
     *
     * @param req 服务实例ID
     * @throws PolarisException 错误码及异常信息
     */
    void deRegister(InstanceDeregisterRequest req) throws PolarisException;

    /**
     * 同步进行心跳上报
     *
     * @param req 服务实例ID
     * @throws PolarisException 错误码及异常信息
     */
    void heartbeat(InstanceHeartbeatRequest req) throws PolarisException;

    /**
     * 清理并释放资源
     */
    void destroy();

    @Override
    default void close() {
        destroy();
    }
}
