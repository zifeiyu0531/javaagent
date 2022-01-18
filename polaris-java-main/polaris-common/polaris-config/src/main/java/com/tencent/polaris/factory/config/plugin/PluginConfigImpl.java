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

package com.tencent.polaris.factory.config.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tencent.polaris.api.config.plugin.PluginConfig;
import com.tencent.polaris.api.config.plugin.PluginConfigProvider;
import com.tencent.polaris.api.config.verify.Verifier;
import com.tencent.polaris.api.exception.ErrorCode;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.utils.MapUtils;
import com.tencent.polaris.factory.util.ConfigUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 插件配置对象解析
 *
 * @author andrewshan
 * @date 2019/8/20
 */
public class PluginConfigImpl implements PluginConfig {

    @JsonProperty
    private final Map<String, Map<?, ?>> plugin = new HashMap<>();

    private final Object lock = new Object();

    private static final Map<String, Class<? extends Verifier>> pluginConfigClazz = new HashMap<>();

    static {
        ServiceLoader<PluginConfigProvider> providers = ServiceLoader.load(PluginConfigProvider.class);
        for (PluginConfigProvider provider : providers) {
            pluginConfigClazz.put(provider.getName(), provider.getPluginConfigClazz());
        }
    }

    /**
     * 用于转换插件配置逻辑
     */
    private final ObjectMapper mapper = new ObjectMapper();

    public PluginConfigImpl() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public <T extends Verifier> T getPluginConfig(String pluginName, Class<T> clazz) throws PolarisException {
        synchronized (lock) {
            Map<?, ?> properties = plugin.get(pluginName);
            if (null == properties) {
                Verifier config = getConfigByName(clazz);
                properties = mutableSetPluginConfig(pluginName, config);
            }
            T result;
            try {
                result = mapper.convertValue(properties, clazz);
            } catch (IllegalArgumentException e) {
                throw new PolarisException(ErrorCode.INVALID_CONFIG,
                        String.format("fail to deserialize properties %s to clazz %s for plugin %s", properties,
                                clazz.getCanonicalName(), pluginName), e);
            }
            return result;
        }
    }

    public void setDefaultPluginConfig(PluginConfig pluginConfig) {
        if (null == pluginConfig) {
            return;
        }
        Map<String, Verifier> pluginConfigs = pluginConfig.getPluginConfigs();
        if (MapUtils.isEmpty(pluginConfigs)) {
            return;
        }
        for (Map.Entry<String, Verifier> entry : pluginConfigs.entrySet()) {
            String pluginName = entry.getKey();
            Verifier defaultObject = entry.getValue();
            Class<? extends Verifier> pluginConfigClazz = defaultObject.getClass();
            Verifier existConfig = getPluginConfig(pluginName, pluginConfigClazz);
            existConfig.setDefault(defaultObject);
            setPluginConfig(pluginName, existConfig);
        }
    }

    public void verifyPluginConfig() {
        Map<String, Verifier> pluginConfigs = getPluginConfigs();
        if (MapUtils.isEmpty(pluginConfigs)) {
            return;
        }
        for (Verifier verifier : pluginConfigs.values()) {
            verifier.verify();
        }
    }

    @Override
    public Map<String, Verifier> getPluginConfigs() throws PolarisException {
        synchronized (lock) {
            Map<String, Verifier> values = new HashMap<>();
            if (plugin.size() == 0) {
                return values;
            }
            for (Map.Entry<String, Map<?, ?>> entry : plugin.entrySet()) {
                Map<?, ?> properties = entry.getValue();
                if (MapUtils.isEmpty(properties)) {
                    continue;
                }
                String pluginName = entry.getKey();
                Class<? extends Verifier> clazz = PluginConfigImpl.pluginConfigClazz.get(pluginName);
                if (null == clazz) {
//                    throw new PolarisException(ErrorCode.INVALID_CONFIG,
//                            String.format("unknown plugin config type for plugin %s", pluginName));
                    continue;
                }
                try {
                    Verifier result = mapper.convertValue(properties, clazz);
                    values.put(pluginName, result);
                } catch (IllegalArgumentException e) {
                    throw new PolarisException(ErrorCode.INVALID_CONFIG,
                            String.format("fail to deserialize properties %s to clazz %s for plugin %s", properties,
                                    clazz.getCanonicalName(), pluginName), e);
                }
            }
            return values;
        }
    }

    private Map<?, ?> mutableSetPluginConfig(String pluginName, Verifier config) throws PolarisException {
        Map<?, ?> configMap;
        try {
            configMap = ConfigUtils.objectToMap(config);
        } catch (Exception e) {
            throw new PolarisException(ErrorCode.INVALID_CONFIG,
                    String.format("fail to marshal plugin config for %s", pluginName), e);
        }
        plugin.put(pluginName, configMap);
        return configMap;
    }

    /**
     * 设置特定插件配置
     *
     * @param pluginName 插件名
     * @param config 插件配置对象
     * @throws PolarisException 设置过程出现的异常
     */
    public void setPluginConfig(String pluginName, Verifier config) throws PolarisException {
        synchronized (lock) {
            if (null == config) {
                throw new PolarisException(ErrorCode.INVALID_CONFIG,
                        String.format("config is null, plugin name %s", pluginName));
            }
            mutableSetPluginConfig(pluginName, config);
        }
    }

    /**
     * 通过插件名，初始化配置
     *
     * @return 配置对象
     */
    private static Verifier getConfigByName(Class<? extends Verifier> pluginConfigClazz) {
//        Class<? extends Verifier> pluginConfigClazz = PluginConfigImpl.pluginConfigClazz.get(pluginName);
//        if (null == pluginConfigClazz) {
//            return null;
//        }
        try {
            return pluginConfigClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(pluginConfigClazz.getCanonicalName() + " create config failed.", e);
        }
    }

//    protected void setDefault(String routeName) {
//        Verifier defaultConfig = getConfigByName(routeName);
//        if (null != defaultConfig) {
//            overwriteDefaultConfigWithUserConfig(routeName, defaultConfig);
//        }
//    }
//
//    private <T> void overwriteDefaultConfigWithUserConfig(String routerName,
//            T defaultRouterConfig) {
//        synchronized (lock) {
//            Map<String, Object> userConfig = plugin.get(routerName);
//            if (userConfig == null) {
//                userConfig = new HashMap<>();
//            }
//            //使用默认的转成map是为了方便和用户配置的merge
//            Map<String, Object> defaultConfigMap = mapper.convertValue(defaultRouterConfig, Map.class);
//            //使用用户的配置覆盖默认
//            defaultConfigMap.putAll(userConfig);
//            plugin.put(routerName, defaultConfigMap);
//        }
//    }
}
