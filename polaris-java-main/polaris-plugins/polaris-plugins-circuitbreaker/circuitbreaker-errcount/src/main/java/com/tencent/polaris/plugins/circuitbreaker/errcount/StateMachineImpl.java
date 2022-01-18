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

package com.tencent.polaris.plugins.circuitbreaker.errcount;

import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.StatusDimension;
import com.tencent.polaris.plugins.circuitbreaker.common.AbstractStateMachine;
import com.tencent.polaris.plugins.circuitbreaker.common.ConfigGroup;
import com.tencent.polaris.plugins.circuitbreaker.common.ConfigSet;
import com.tencent.polaris.plugins.circuitbreaker.common.ConfigSetLocator;
import com.tencent.polaris.plugins.circuitbreaker.common.HalfOpenCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于连续错误熔断的状态机切换逻辑
 *
 * @author andrewshan
 * @date 2019/8/27
 */
public class StateMachineImpl extends AbstractStateMachine<Config> {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineImpl.class);

    public StateMachineImpl(ConfigGroup<Config> configGroup, int pluginId, ConfigSetLocator<Config> configSetLocator) {
        super(configGroup, pluginId, configSetLocator);
    }

    @Override
    public boolean closeToOpen(Instance instance, StatusDimension statusDimension, Parameter parameter) {
        HalfOpenCounter halfOpenCounter = getHalfOpenCounterOnClose(
                instance, statusDimension);
        if (halfOpenCounter == null) {
            return false;
        }
        ConfigSet<Config> configSet = getConfigSetByLocator(instance, statusDimension, configSetLocator);
        return ((ConsecutiveCounter) halfOpenCounter).getConsecutiveErrorCount(statusDimension) >= configSet
                .getPlugConfig().getContinuousErrorThreshold();
    }
}
