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

package com.tencent.polaris.ratelimit.test.core;

import java.util.HashMap;
import java.util.Map;

public interface Consts {

    String NAMESPACE_TEST = "Test";

    String LOCAL_LIMIT_SERVICE = "java_local_limit";

    String LABEL_METHOD = "method";

    String METHOD_PAY = "pay";

    String METHOD_CASH = "cash";

    int MAX_PAY_COUNT = 2000;

    int MAX_CASH_COUNT = 4000;

    int MAX_SERVICE_COUNT = 15000;

    /**
     * 通过数组创建Map
     *
     * @param keys key数组
     * @param values value数组
     * @return map对象
     */
    static Map<String, String> createSingleValueMap(String[] keys, String[] values) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            result.put(keys[i], values[i]);
        }
        return result;
    }
}
