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

package com.tencent.polaris.api.config.provider;

import com.tencent.polaris.api.config.plugin.PluginConfig;
import com.tencent.polaris.api.config.verify.Verifier;

public interface RateLimitConfig extends PluginConfig, Verifier {

    enum Fallback {
        pass, reject,
    }

    /**
     * 是否开启限流功能
     *
     * @return boolean
     */
    boolean isEnable();

    /**
     * 最大限流窗口数量
     *
     * @return int
     */
    int getMaxWindowCount();

    /**
     * 限流窗口超标后的降级策略
     *
     * @return fallback
     */
    Fallback getFallbackOnExceedWindowCount();
}
