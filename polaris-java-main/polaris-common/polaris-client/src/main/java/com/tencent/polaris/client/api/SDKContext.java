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

package com.tencent.polaris.client.api;

import com.tencent.polaris.api.config.Configuration;
import com.tencent.polaris.api.config.global.ClusterConfig;
import com.tencent.polaris.api.config.global.ClusterType;
import com.tencent.polaris.api.config.global.SystemConfig;
import com.tencent.polaris.api.control.Destroyable;
import com.tencent.polaris.api.exception.ErrorCode;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.plugin.Manager;
import com.tencent.polaris.api.plugin.PluginType;
import com.tencent.polaris.api.plugin.Supplier;
import com.tencent.polaris.api.plugin.TypeProvider;
import com.tencent.polaris.api.plugin.common.InitContext;
import com.tencent.polaris.api.plugin.common.ValueContext;
import com.tencent.polaris.api.plugin.compose.Extensions;
import com.tencent.polaris.api.plugin.compose.ServerServiceInfo;
import com.tencent.polaris.api.plugin.impl.PluginManager;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import java.io.Closeable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SDK初始化相关的上下文信息
 *
 * @author andrewshan
 * @date 2019/8/21
 */
public class SDKContext extends Destroyable implements InitContext, AutoCloseable, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(SDKContext.class);

    /**
     * 配置对象
     */
    private final Configuration configuration;

    /**
     * 初始化标识
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 插件管理器
     */
    private final Manager plugins;

    private final ValueContext valueContext;

    private final Extensions extensions = new Extensions();

    private final Object lock = new Object();

    private List<Destroyable> destroyHooks = new ArrayList<>();

    private final Collection<ServerServiceInfo> serverServices;

    public ValueContext getValueContext() {
        return valueContext;
    }

    /**
     * 构造器
     *
     * @param configuration 配置
     * @param plugins 插件工厂
     * @param valueContext 上下文
     */
    public SDKContext(Configuration configuration, Manager plugins, ValueContext valueContext) {
        this.configuration = configuration;
        this.plugins = plugins;
        this.valueContext = valueContext;
        this.valueContext.setClientId(UUID.randomUUID().toString());
        List<ServerServiceInfo> services = new ArrayList<>();
        //加载系统服务配置
        SystemConfig system = configuration.getGlobal().getSystem();
        ClusterConfig discoverCluster = system.getDiscoverCluster();
        if (clusterAvailable(discoverCluster)) {
            services.add(new ServerServiceInfo(ClusterType.SERVICE_DISCOVER_CLUSTER, discoverCluster));
        }
        ClusterConfig healthCheckCluster = system.getHealthCheckCluster();
        if (clusterAvailable(healthCheckCluster)) {
            services.add(new ServerServiceInfo(ClusterType.HEALTH_CHECK_CLUSTER, healthCheckCluster));
        }
        ClusterConfig monitorCluster = system.getMonitorCluster();
        if (clusterAvailable(monitorCluster)) {
            services.add(new ServerServiceInfo(ClusterType.MONITOR_CLUSTER, monitorCluster));
        }
        this.serverServices = Collections.unmodifiableCollection(services);
    }

    public synchronized void init() throws PolarisException {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        extensions.init(configuration, plugins, valueContext);
        plugins.postContextInitPlugins(extensions);
    }

    private boolean clusterAvailable(ClusterConfig clusterConfig) {
        if (null == clusterConfig) {
            return false;
        }
        if (StringUtils.isBlank(clusterConfig.getNamespace()) || StringUtils.isBlank(clusterConfig.getService())) {
            return false;
        }
        if (!clusterConfig.isSameAsBuiltin()) {
            return false;
        }
        return true;
    }

    public Extensions getExtensions() {
        return extensions;
    }

    public Configuration getConfig() {
        return configuration;
    }

    public Supplier getPlugins() {
        return plugins;
    }

    @Override
    protected void doDestroy() {
        synchronized (lock) {
            for (Destroyable destroyable : destroyHooks) {
                destroyable.destroy();
            }
        }
        plugins.destroyPlugins();
    }

    /**
     * 通过默认配置初始化SDKContext
     *
     * @return SDKContext对象
     * @throws PolarisException 初始化异常
     */
    public static SDKContext initContext() throws PolarisException {
        Configuration configuration = ConfigAPIFactory.defaultConfig();
        return initContextByConfig(configuration);
    }

    /**
     * 通过配置对象初始化SDK上下文
     *
     * @param config 配置对象
     * @return SDK上下文
     * @throws PolarisException 初始化过程的异常
     */
    public static SDKContext initContextByConfig(Configuration config) throws PolarisException {
        try {
            ((ConfigurationImpl) config).setDefault();
            config.verify();
        } catch (IllegalArgumentException e) {
            throw new PolarisException(ErrorCode.INVALID_CONFIG, "fail to verify configuration", e);
        }
        ServiceLoader<TypeProvider> providers = ServiceLoader.load(TypeProvider.class);
        List<PluginType> types = new ArrayList<>();
        for (TypeProvider provider : providers) {
            types.addAll(provider.getTypes());
        }
        PluginManager manager = new PluginManager(types);
        ValueContext valueContext = new ValueContext();
        valueContext.setHost(parseHost(config));
        SDKContext initContext = new SDKContext(config, manager, valueContext);

        try {
            manager.initPlugins(initContext);
        } catch (Throwable e) {
            manager.destroyPlugins();
            if (e instanceof PolarisException) {
                throw e;
            }
            throw new PolarisException(ErrorCode.PLUGIN_ERROR, "plugin error", e);
        }
        return initContext;
    }

    @Override
    public Collection<ServerServiceInfo> getServerServices() {
        return serverServices;
    }

    public static String parseHost(Configuration configuration) {
        String hostAddress = configuration.getGlobal().getAPI().getBindIP();
        if (!StringUtils.isBlank(hostAddress)) {
            return hostAddress;
        }
        String nic = configuration.getGlobal().getAPI().getBindIf();
        return resolveAddress(nic);
    }

    private static NetworkInterface resolveNetworkInterface(String nic) {
        NetworkInterface ni = null;
        try {
            if (StringUtils.isNotBlank(nic)) {
                ni = NetworkInterface.getByName(nic);
            }
        } catch (SocketException e) {
            LOG.error("[ReportClient]get nic failed, nic:{}", nic, e);
        }
        if (null != nic) {
            return ni;
        }
        //获取第一张网卡
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.getInetAddresses().hasMoreElements()) {
                    return networkInterface;
                }
            }
        } catch (SocketException e) {
            LOG.error("[ReportClient]get all network interfaces failed", e);
        }
        return null;
    }

    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    /**
     * 解析网卡IP
     *
     * @param nic 网卡标识，如eth1
     * @return 地址信息
     */
    private static String resolveAddress(String nic) {
        NetworkInterface ni = resolveNetworkInterface(nic);
        if (null == ni) {
            return DEFAULT_ADDRESS;
        }
        Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
        if (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            return inetAddress.getCanonicalHostName();
        }
        return DEFAULT_ADDRESS;
    }

    public void registerDestroyHook(Destroyable destroyable) {
        synchronized (lock) {
            destroyHooks.add(destroyable);
        }
    }

    @Override
    public void close() {
        destroy();
    }
}
