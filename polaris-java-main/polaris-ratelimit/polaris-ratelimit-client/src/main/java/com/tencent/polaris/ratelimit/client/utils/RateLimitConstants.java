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

package com.tencent.polaris.ratelimit.client.utils;

public interface RateLimitConstants {

    /**
     * 默认Map组装str key value分割符
     */
    String DEFAULT_KV_SEPARATOR = ":";

    /**
     * 默认Map组装str (key:value) 二元组分割符
     */
    String DEFAULT_ENTRY_SEPARATOR = "|";

    /**
     * 规则不存在的报错
     */
    String RULE_NOT_EXISTS = "quota rule not exists";

    /**
     * 默认的名字分隔符
     */
    String DEFAULT_NAMES_SEPARATOR = "#";

    /**
     * 淘汰因子，过期时间=MaxDuration + ExpireFactor
     */
    long EXPIRE_FACTOR_MS = 1000;

    /**
     * 本地模式
     */
    int CONFIG_QUOTA_LOCAL_MODE = 0;

    /**
     * 远程分布式模式
     */
    int CONFIG_QUOTA_GLOBAL_MODE = 1;

    long STARTUP_DELAY_MS = 30 * 1000;

    int RANGE_DELAY_MS = 20 * 1000;

    /**
     * 服务端的返回code
     */
    int SUCCESS = 200;
}
