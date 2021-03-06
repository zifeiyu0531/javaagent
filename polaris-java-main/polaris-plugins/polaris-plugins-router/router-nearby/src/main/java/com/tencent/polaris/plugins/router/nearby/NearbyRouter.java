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

package com.tencent.polaris.plugins.router.nearby;

import static com.tencent.polaris.client.util.Utils.isHealthyInstance;

import com.tencent.polaris.api.config.consumer.ServiceRouterConfig;
import com.tencent.polaris.api.config.plugin.PluginConfigProvider;
import com.tencent.polaris.api.config.verify.Verifier;
import com.tencent.polaris.api.exception.ErrorCode;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.plugin.PluginType;
import com.tencent.polaris.api.plugin.common.InitContext;
import com.tencent.polaris.api.plugin.common.PluginTypes;
import com.tencent.polaris.api.plugin.common.ValueContext;
import com.tencent.polaris.api.plugin.compose.Extensions;
import com.tencent.polaris.api.plugin.route.LocationLevel;
import com.tencent.polaris.api.plugin.route.RouteInfo;
import com.tencent.polaris.api.plugin.route.RouteResult;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.pojo.ServiceMetadata;
import com.tencent.polaris.api.pojo.StatusDimension;
import com.tencent.polaris.api.pojo.StatusDimension.Level;
import com.tencent.polaris.api.utils.CollectionUtils;
import com.tencent.polaris.api.utils.MapUtils;
import com.tencent.polaris.api.utils.ThreadPoolUtils;
import com.tencent.polaris.client.util.NamedThreadFactory;
import com.tencent.polaris.plugins.router.common.AbstractServiceRouter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ??????????????????
 *
 * @author vickliu
 * @date 2019/11/10
 */
public class NearbyRouter extends AbstractServiceRouter implements PluginConfigProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NearbyRouter.class);

    private static final LocationLevel defaultMinLevel = LocationLevel.zone;

    private ValueContext valueContext;

    private ScheduledExecutorService reportClientExecutor;

    /**
     * # ????????????????????????????????? matchLevel: zone # ???????????????????????????????????????????????? maxMatchLevel: all #
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????server??????????????????IP?????????????????????????????????????????????????????????????????? strictNearby: false #
     * ?????????????????????????????????????????????????????? enableDegradeByUnhealthyPercent: true????????????????????????????????????#
     * ?????????????????????????????????????????????????????????????????????????????????????????????(0, 100]??? # ??????100???????????????????????????????????????
     */
    private NearbyRouterConfig config;

    /**
     * ???????????????????????????
     */
    private double healthyPercentToDegrade;

    /**
     * ???????????????????????????????????????
     */
    long locationReadyTimeout;
    /**
     * ?????????????????????
     */
    private final AtomicReference<Map<LocationLevel, String>> locationInfo = new AtomicReference<>();

    @Override
    public RouteResult router(RouteInfo routeInfo, ServiceInstances serviceInstances)
            throws PolarisException {
        //?????????????????????????????????
        LocationLevel minAvailableLevel = config.getMatchLevel();
        if (null == minAvailableLevel) {
            minAvailableLevel = defaultMinLevel;
        }
        LocationLevel minLevel = minAvailableLevel;
        if (null != routeInfo.getNextRouterInfo()) {
            if (null != routeInfo.getNextRouterInfo().getLocationLevel()) {
                minLevel = routeInfo.getNextRouterInfo().getLocationLevel();
            }
            if (null != routeInfo.getNextRouterInfo().getMinAvailableLevel()) {
                minAvailableLevel = routeInfo.getNextRouterInfo().getMinAvailableLevel();
            }
        }
        LocationLevel maxLevel = config.getMaxMatchLevel();
        if (null == maxLevel) {
            maxLevel = LocationLevel.all;
        }

        Map<LocationLevel, String> clientLocationInfo = locationInfo.get();
        if (minLevel.ordinal() >= maxLevel.ordinal()) {
            List<Instance> instances = selectInstances(serviceInstances, minAvailableLevel, clientLocationInfo);
            if (CollectionUtils.isEmpty(instances)) {
                throw new PolarisException(ErrorCode.LOCATION_MISMATCH,
                        String.format("can not find any instance by level %s", minLevel.name()));
            }
            //?????????????????????
            return new RouteResult(selectInstances(
                    serviceInstances, minAvailableLevel, clientLocationInfo), RouteResult.State.Next);
        }
        CheckResult checkResult = new CheckResult();
        for (int i = minLevel.ordinal(); i < maxLevel.ordinal(); i++) {
            LocationLevel curLevel = LocationLevel.values()[i];
            checkResult = hasHealthyInstances(serviceInstances, routeInfo.getStatusDimensions(), curLevel,
                    clientLocationInfo);
            checkResult.curLevel = curLevel;
            if (!CollectionUtils.isEmpty(checkResult.instances)) {
                break;
            } else {
                minAvailableLevel = curLevel;
            }
        }
        if (CollectionUtils.isEmpty(checkResult.instances)) {
            throw new PolarisException(ErrorCode.LOCATION_MISMATCH,
                    String.format("can not find any instance by level %s", checkResult.curLevel.name()));
        }
        if (!config.isEnableDegradeByUnhealthyPercent() || checkResult.curLevel == LocationLevel.all) {
            return new RouteResult(checkResult.instances, RouteResult.State.Next);
        }
        int healthyInstanceCount = checkResult.healthyInstanceCount;
        double actualHealthyPercent = (double) healthyInstanceCount / (double) serviceInstances.getInstances().size();
        if (actualHealthyPercent <= healthyPercentToDegrade) {
            LOG.debug("[shouldDegrade] enableDegradeByUnhealthyPercent = {},unhealthyPercentToDegrade={},"
                            + "healthyPercent={},isStrict={},matchLevel={}",
                    config.isEnableDegradeByUnhealthyPercent(), config.getUnhealthyPercentToDegrade(),
                    actualHealthyPercent,
                    config.isStrictNearby(), checkResult.curLevel);
            RouteResult result = new RouteResult(checkResult.instances, RouteResult.State.Retry);
            result.getNextRouterInfo().setLocationLevel(nextLevel(checkResult.curLevel));
            result.getNextRouterInfo().setMinAvailableLevel(minAvailableLevel);
            return result;
        }
        return new RouteResult(checkResult.instances, RouteResult.State.Next);
    }

    private LocationLevel nextLevel(LocationLevel current) {
        if (current == LocationLevel.all) {
            return current;
        }
        return LocationLevel.values()[current.ordinal() + 1];
    }

    private static class CheckResult {

        LocationLevel curLevel;
        int healthyInstanceCount;
        List<Instance> instances = new ArrayList<>();
    }

    private CheckResult hasHealthyInstances(ServiceInstances svcInstances, Map<Level, StatusDimension> dimensions,
            LocationLevel targetLevel, Map<LocationLevel, String> clientInfo) {
        String clientZone = "";
        String clientRegion = "";
        String clientCampus = "";
        if (null != clientInfo) {
            clientZone = clientInfo.get(LocationLevel.zone);
            clientRegion = clientInfo.get(LocationLevel.region);
            clientCampus = clientInfo.get(LocationLevel.campus);
        }
        CheckResult checkResult = new CheckResult();
        for (Instance instance : svcInstances.getInstances()) {
            switch (targetLevel) {
                case zone:
                    if (clientZone.equals("") || clientZone.equals(instance.getZone())) {
                        checkResult.instances.add(instance);
                        if (isHealthyInstance(instance, dimensions)) {
                            checkResult.healthyInstanceCount++;
                        }
                    }
                    break;
                case campus:
                    if (clientCampus.equals("") || clientCampus.equals(instance.getCampus())) {
                        checkResult.instances.add(instance);
                        if (isHealthyInstance(instance, dimensions)) {
                            checkResult.healthyInstanceCount++;
                        }
                    }
                    break;
                case region:
                    if (clientRegion.equals("") || clientRegion.equals(instance.getRegion())) {
                        checkResult.instances.add(instance);
                        if (isHealthyInstance(instance, dimensions)) {
                            checkResult.healthyInstanceCount++;
                        }
                    }
                    break;
                default:
                    checkResult.instances.add(instance);
                    if (isHealthyInstance(instance, dimensions)) {
                        checkResult.healthyInstanceCount++;
                    }
                    break;
            }
        }
        return checkResult;
    }


    private List<Instance> selectInstances(
            ServiceInstances svcInstances, LocationLevel targetLevel, Map<LocationLevel, String> clientInfo) {
        List<Instance> instances = new ArrayList<>();
        String clientZone = "";
        String clientRegion = "";
        String clientCampus = "";
        if (null != clientInfo) {
            clientZone = clientInfo.get(LocationLevel.zone);
            clientRegion = clientInfo.get(LocationLevel.region);
            clientCampus = clientInfo.get(LocationLevel.campus);
        }
        for (Instance instance : svcInstances.getInstances()) {
            switch (targetLevel) {
                case zone:
                    if (clientZone.equals("") || clientZone.equals(instance.getZone())) {
                        instances.add(instance);
                    }
                    break;
                case campus:
                    if (clientCampus.equals("") || clientCampus.equals(instance.getCampus())) {
                        instances.add(instance);
                    }
                    break;
                case region:
                    if (clientRegion.equals("") || clientRegion.equals(instance.getRegion())) {
                        instances.add(instance);
                    }
                    break;
                default:
                    instances.add(instance);
                    break;
            }
        }
        return instances;
    }

    @Override
    public String getName() {
        return ServiceRouterConfig.DEFAULT_ROUTER_NEARBY;
    }

    @Override
    public Class<? extends Verifier> getPluginConfigClazz() {
        return NearbyRouterConfig.class;
    }

    @Override
    public PluginType getType() {
        return PluginTypes.SERVICE_ROUTER.getBaseType();
    }


    @Override
    public void init(InitContext ctx) throws PolarisException {
        valueContext = ctx.getValueContext();
        NearbyRouterConfig config = ctx.getConfig().getConsumer().getServiceRouter()
                .getPluginConfig(getName(), NearbyRouterConfig.class);
        if (config == null) {
            throw new PolarisException(ErrorCode.INVALID_CONFIG,
                    String.format("plugin %s config is missing", getName()));
        }
        this.config = config;
        LOG.debug("[init] config={}", this.config);
        locationReadyTimeout = (ctx.getConfig().getGlobal().getAPI().getReportInterval() + ctx.getConfig().getGlobal()
                .getServerConnector().getConnectTimeout()) * (ctx.getConfig().getGlobal().getAPI().getMaxRetryTimes()
                + 1);
        healthyPercentToDegrade = 1 - (double) config.getUnhealthyPercentToDegrade() / (double) 100;
        if (this.config.isEnableReportLocalAddress()) {
            reportClientExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(getName()));
        }
    }


    /**
     * ?????????AppContext????????????????????????
     *
     * @param extensions ???????????????
     * @throws PolarisException ??????
     */
    @Override
    public void postContextInit(Extensions extensions) throws PolarisException {
        //?????????????????????????????????
        //TODO:
        if (null != reportClientExecutor) {
            //????????????????????????????????????
            reportClientExecutor.scheduleAtFixedRate(
                    new ReportClientTask(extensions, valueContext), 0, 60, TimeUnit.SECONDS);
            LOG.info("reportClientExecutor has been started");
            //???????????????????????????????????????????????????????????????
            ensureLocationReady();
        }
    }

    /**
     * ????????????????????????????????????
     */
    public void ensureLocationReady() throws PolarisException {
        if (!this.config.isStrictNearby()) {
            return;
        }
        try {
            this.valueContext.waitForLocationReady(this.locationReadyTimeout);
            refreshLocationInfo();
        } catch (InterruptedException e) {
            throw new PolarisException(ErrorCode.LOCATION_MISMATCH,
                    "caller location not ready,and strict nearby is true.", e);

        }
    }

    private void refreshLocationInfo() {
        Map<LocationLevel, String> clientLocationInfo = new HashMap<>();
        for (LocationLevel key : LocationLevel.values()) {
            if (valueContext.getValue(key.name()) != null) {
                clientLocationInfo.put(key, valueContext.getValue(key.name()));
            }
        }
        locationInfo.set(clientLocationInfo);
        LOG.debug("[refreshLocationInfo] locationInfo={}", clientLocationInfo);
    }

    private static final String nearbyMetadataEnable = "internal-enable-nearby";

    @Override
    public Aspect getAspect() {
        return Aspect.MIDDLE;
    }

    @Override
    public boolean enable(RouteInfo routeInfo, ServiceMetadata dstSvcInfo) {
        if (!super.enable(routeInfo, dstSvcInfo)) {
            return false;
        }
        Map<LocationLevel, String> clientLocationInfo = locationInfo.get();
        if (MapUtils.isEmpty(clientLocationInfo)) {
            return false;
        }
        if (!dstSvcInfo.getMetadata().containsKey(nearbyMetadataEnable)) {
            return false;
        }
        return Boolean.parseBoolean(dstSvcInfo.getMetadata().get(nearbyMetadataEnable));
    }

    @Override
    protected void doDestroy() {
        LOG.info("reportClientExecutor has been stopped");
        ThreadPoolUtils.waitAndStopThreadPools(new ExecutorService[]{reportClientExecutor});
    }
}
