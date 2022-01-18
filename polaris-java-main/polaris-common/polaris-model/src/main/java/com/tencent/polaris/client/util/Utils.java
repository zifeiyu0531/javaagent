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

package com.tencent.polaris.client.util;

import com.tencent.polaris.api.pojo.CircuitBreakerStatus;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.StatusDimension;
import com.tencent.polaris.api.pojo.StatusDimension.Level;
import com.tencent.polaris.api.utils.StringUtils;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common util class.
 *
 * @author andrewshan
 * @date 2019/8/24
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static long sleepUninterrupted(long millis) {
        long currentTime = System.currentTimeMillis();
        long deadline = currentTime + millis;
        while (currentTime < deadline) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                LOG.debug(String.format("interrupted while sleeping %d", millis), e);
            }
            currentTime = System.currentTimeMillis();
        }
        return currentTime;
    }

    public static String translatePath(String path) {
        if (path.startsWith("$HOME")) {
            String userHome = System.getProperty("user.home");
            return StringUtils.replace(path, "$HOME", userHome);
        }
        return path;
    }


    /**
     * 用正则表达式来判断
     * 1.compile(String regex)    将给定的正则表达式编译到模式中。
     * 2.matcher(CharSequence input)    创建匹配给定输入与此模式的匹配器。
     * 3.matches()    尝试将整个区域与模式匹配。
     *
     * @param regex 正则表达式
     * @param input 输入文本
     * @return 是否匹配
     */
    public static boolean regMatch(String regex, String input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.matches();
    }


    public static boolean isHealthyInstance(Instance instance, Map<Level, StatusDimension> dimensions) {
        if (!instance.isHealthy()) {
            return false;
        }
        for (StatusDimension statusDimension : dimensions.values()) {
            CircuitBreakerStatus circuitBreakerStatus = instance.getCircuitBreakerStatus(statusDimension);
            if (null != circuitBreakerStatus && circuitBreakerStatus.getStatus() == CircuitBreakerStatus.Status.OPEN) {
                return false;
            }
        }
        return true;
    }
}
