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

package com.tencent.polaris.ratelimit.example.utils;

import com.tencent.polaris.api.utils.StringUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class LimitExampleUtils {

    public static class InitResult {

        private final String namespace;
        private final String service;
        private final int concurrency;

        public InitResult(String namespace, String service, int concurrency) {
            this.namespace = namespace;
            this.service = service;
            this.concurrency = concurrency;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getService() {
            return service;
        }

        public int getConcurrency() {
            return concurrency;
        }
    }

    /**
     * 初始化配置对象
     *
     * @param args 命名行参数
     * @return 配置对象
     * @throws ParseException 解析异常
     */
    public static InitResult initRateLimitConfiguration(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("namespace", "service namespace", true, "namespace for service");
        options.addOption("service", "service name", true, "service name");
        options.addOption("concurrency", "concurrency", true, "concurrency");

        CommandLine commandLine = parser.parse(options, args);
        String namespace = commandLine.getOptionValue("namespace");
        String service = commandLine.getOptionValue("service");
        String concurrencyStr = commandLine.getOptionValue("concurrency");
        int concurrency;
        if (StringUtils.isNotBlank(concurrencyStr)) {
            try {
                concurrency = Integer.parseInt(concurrencyStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                concurrency = 1;
            }
        } else {
            concurrency = 1;
        }
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(service)) {
            System.out.println("namespace or service is required");
            System.exit(1);
        }
        return new InitResult(namespace, service, concurrency);
    }
}
