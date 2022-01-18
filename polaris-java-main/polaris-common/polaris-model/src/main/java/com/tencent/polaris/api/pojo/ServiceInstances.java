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

package com.tencent.polaris.api.pojo;

import java.util.List;

/**
 * 服务实例列表信息
 *
 * @author andrewshan
 * @date 2019/8/21
 */
public interface ServiceInstances extends ServiceMetadata {

    /**
     * 获取服务标识
     *
     * @return 服务标识
     */
    ServiceKey getServiceKey();

    /**
     * 获取服务实例总权重值
     *
     * @return totalWeight
     */
    int getTotalWeight();

    /**
     * 获取服务实例列表
     *
     * @return 服务列表
     */
    List<Instance> getInstances();

    /**
     * 服务实例列表是否已经加载
     *
     * @return 加载标识
     */
    boolean isInitialized();

    /**
     * 获取唯一标识信息
     *
     * @return revision
     */
    String getRevision();

}
