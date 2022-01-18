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

package com.tencent.polaris.plugins.router.rule;

import com.tencent.polaris.api.config.consumer.ServiceRouterConfig;
import com.tencent.polaris.api.exception.ErrorCode;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.plugin.PluginType;
import com.tencent.polaris.api.plugin.common.InitContext;
import com.tencent.polaris.api.plugin.common.PluginTypes;
import com.tencent.polaris.api.plugin.route.RouteInfo;
import com.tencent.polaris.api.plugin.route.RouteResult;
import com.tencent.polaris.api.plugin.route.ServiceRouter;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.pojo.ServiceMetadata;
import com.tencent.polaris.api.utils.CollectionUtils;
import com.tencent.polaris.api.utils.MapUtils;
import com.tencent.polaris.api.utils.RuleUtils;
import com.tencent.polaris.client.pb.ModelProto.MatchString;
import com.tencent.polaris.client.pb.RoutingProto;
import com.tencent.polaris.client.util.Utils;
import com.tencent.polaris.plugins.router.common.AbstractServiceRouter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于规则的服务路由能力
 *
 * @author andrewshan
 * @date 2019/8/28
 */
public class RuleBasedRouter extends AbstractServiceRouter {

    private static final Logger LOG = LoggerFactory.getLogger(RuleBasedRouter.class);

    private Map<String, String> globalVariablesConfig;

    /**
     * 根据路由规则进行服务实例过滤, 并返回过滤后的实例列表
     *
     * @param routeInfo
     * @param ruleMatchType
     * @return 路由规则列表
     * @throws PolarisException
     */
    private List<RoutingProto.Route> getRoutesFromRule(RouteInfo routeInfo, RuleMatchType ruleMatchType)
            throws PolarisException {
        // 跟据服务类型获取对应路由规则
        // 被调inbound
        if (ruleMatchType == RuleMatchType.destRouteRuleMatch) {
            if (null == routeInfo.getDestRouteRule() || null == routeInfo.getDestRouteRule().getRule()) {
                return null;
            }
            Object routing = routeInfo.getDestRouteRule().getRule();
            if (!(routing instanceof RoutingProto.Routing)) {
                throw new PolarisException(ErrorCode.API_INVALID_ARGUMENT, "getRuleFilteredInstances param invalid, "
                        + "inbound routing must be instance of RoutingProto.Routing");
            }

            return ((RoutingProto.Routing) routing).getInboundsList();
        }

        // 主调outbound
        if (null == routeInfo.getSourceRouteRule() || null == routeInfo.getSourceRouteRule().getRule()) {
            return null;
        }
        Object routing = routeInfo.getSourceRouteRule().getRule();
        if (!(routing instanceof RoutingProto.Routing)) {
            throw new PolarisException(ErrorCode.API_INVALID_ARGUMENT, "getRuleFilteredInstances param invalid, "
                    + "outbound routing must be instance of RoutingProto.Routing");
        }
        return ((RoutingProto.Routing) routing).getOutboundsList();
    }

    // 匹配source规则
    private boolean matchSource(List<RoutingProto.Source> sources, ServiceMetadata sourceService,
            RuleMatchType ruleMatchType, Map<String, String> multiEnvRouterParamMap) {
        if (CollectionUtils.isEmpty(sources)) {
            return true;
        }

        // source匹配成功标志
        boolean matched = true;
        for (RoutingProto.Source source : sources) {
            // 对于inbound规则, 需要匹配source服务
            if (ruleMatchType == RuleMatchType.destRouteRuleMatch) {
                if (sourceService == null) {
                    // 如果没有source服务信息, 判断rule是否支持全匹配
                    if (!RuleUtils.MATCH_ALL.equals(source.getNamespace().getValue()) || !RuleUtils.MATCH_ALL
                            .equals(source.getService().getValue())) {
                        matched = false;
                        continue;
                    }
                } else {
                    // 如果有source服务信息, 需要匹配服务信息
                    // 如果命名空间|服务不为"*"且不等于原服务, 则匹配失败
                    if (!RuleUtils.MATCH_ALL.equals(source.getNamespace().getValue()) && !source.getNamespace()
                            .getValue().equals(sourceService.getNamespace())) {
                        matched = false;
                        continue;
                    }

                    if (!RuleUtils.MATCH_ALL.equals(source.getService().getValue()) && !source.getService()
                            .getValue().equals(sourceService.getService())) {
                        matched = false;
                        continue;
                    }
                }
            }

            // 如果rule中metadata为空, 匹配成功, 结束
            if (MapUtils.isEmpty(source.getMetadataMap())) {
                matched = true;
                break;
            }

            // 如果没有源服务信息, 本次匹配失败
            if (sourceService == null) {
                matched = false;
                continue;
            }

            matched = matchMetadata(
                    source.getMetadataMap(), sourceService.getMetadata(), true, multiEnvRouterParamMap);
            if (matched) {
                break;
            }
        }

        return matched;
    }


    // 匹配metadata
    private boolean matchMetadata(Map<String, MatchString> ruleMeta, Map<String, String> destMeta,
            boolean isMatchSource, Map<String, String> multiEnvRouterParamMap) {
        // 如果规则metadata为空, 返回成功
        if (MapUtils.isEmpty(ruleMeta)) {
            return true;
        }

        // 如果规则metadata不为空, 待匹配规则为空, 直接返回失败
        if (MapUtils.isEmpty(destMeta)) {
            return false;
        }

        // metadata是否全部匹配
        boolean allMetaMatched = true;
        // dest中找到的metadata个数, 用于辅助判断是否能匹配成功
        int matchNum = 0;

        for (Map.Entry<String, MatchString> entry : ruleMeta.entrySet()) {
            String ruleMetaKey = entry.getKey();
            MatchString ruleMetaValue = entry.getValue();
            if (RuleUtils.isMatchAllValue(ruleMetaValue)) {
                matchNum++;
                continue;
            }
            if (destMeta.containsKey(ruleMetaKey)) {
                matchNum++;
                if (!ruleMetaValue.hasValue()
                        && ruleMetaValue.getValueType() != MatchString.ValueType.PARAMETER) {
                    continue;
                }

                String destMetaValue = destMeta.get(ruleMetaKey);

                allMetaMatched = isAllMetaMatched(isMatchSource, true, ruleMetaKey, ruleMetaValue, destMetaValue,
                        multiEnvRouterParamMap);
            }

            if (!allMetaMatched) {
                break;
            }
        }

        // 如果一个metadata未找到, 匹配失败
        if (matchNum == 0) {
            allMetaMatched = false;
        }

        return allMetaMatched;
    }

    private boolean isAllMetaMatched(boolean isMatchSource, boolean allMetaMatched, String ruleMetaKey,
            MatchString ruleMetaValue, String destMetaValue, Map<String, String> multiEnvRouterParamMap) {
        if (ruleMetaValue.getType() == MatchString.MatchStringType.REGEX) {
            // 正则匹配
            allMetaMatched = matchValueByValueType(isMatchSource, true, ruleMetaKey, ruleMetaValue, destMetaValue,
                    multiEnvRouterParamMap);
        } else {
            // 精确匹配
            allMetaMatched = matchValueByValueType(isMatchSource, false, ruleMetaKey, ruleMetaValue, destMetaValue,
                    multiEnvRouterParamMap);
        }
        return allMetaMatched;
    }

    private boolean matchValueByValueType(boolean isMatchSource, boolean isRegex, String ruleMetaKey,
            MatchString ruleMetaValue, String destMetaValue, Map<String, String> multiEnvRouterParamMap) {
        boolean allMetaMatched = true;

        switch (ruleMetaValue.getValueType()) {
            case PARAMETER:
                // 通过参数传入
                if (isMatchSource) {
                    // 当匹配的是source，记录请求的 K V
                    multiEnvRouterParamMap.put(ruleMetaKey, destMetaValue);
                } else {
                    // 当匹配的是dest， 判断value
                    if (!multiEnvRouterParamMap.containsKey(ruleMetaKey)) {
                        allMetaMatched = false;
                    } else {
                        String ruleValue = multiEnvRouterParamMap.get(ruleMetaKey);
                        // contains key
                        allMetaMatched = matchValue(isRegex, destMetaValue, ruleValue);
                    }
                }
                break;
            case VARIABLE:
                if (globalVariablesConfig.containsKey(ruleMetaKey)) {
                    // 1.先从配置获取
                    String ruleValue = (String) globalVariablesConfig.get(ruleMetaKey);
                    allMetaMatched = matchValue(isRegex, destMetaValue, ruleValue);
                } else {
                    // 2.从环境变量中获取  key从规则中获取
                    String key = ruleMetaValue.getValue().getValue();
                    if (!System.getenv().containsKey(key)) {
                        allMetaMatched = false;
                    } else {
                        String value = System.getenv(key);
                        allMetaMatched = matchValue(isRegex, destMetaValue, value);
                    }
                    if (!System.getenv().containsKey(key) || !System.getenv(key).equals(destMetaValue)) {
                        allMetaMatched = false;
                    }
                }
                break;
            default:
                allMetaMatched = matchValue(isRegex, destMetaValue, ruleMetaValue.getValue().getValue());
        }

        return allMetaMatched;
    }

    private boolean matchValue(boolean isRegex, String destMetaValue, String ruleValue) {
        boolean allMetaMatched = true;

        if (isRegex) {
            boolean match = Utils.regMatch(ruleValue, destMetaValue);
            if (!match) {
                allMetaMatched = false;
            }
        } else {
            if (!destMetaValue.equals(ruleValue)) {
                allMetaMatched = false;
            }
        }
        return allMetaMatched;
    }


    private List<Instance> getRuleFilteredInstances(RouteInfo routeInfo, ServiceInstances instances,
            RuleMatchType ruleMatchType) throws PolarisException {
        // 获取路由规则
        List<RoutingProto.Route> routes = getRoutesFromRule(routeInfo, ruleMatchType);
        if (CollectionUtils.isEmpty(routes)) {
            return Collections.emptyList();
        }
        Map<String, String> multiEnvRouterParamMap = new HashMap<>();
        for (RoutingProto.Route route : routes) {
            if (route == null) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getRuleFilteredInstances, route:{}", route.toString());
            }

            // 匹配source规则
            boolean sourceMatched = matchSource(route.getSourcesList(), routeInfo.getSourceService(), ruleMatchType,
                    multiEnvRouterParamMap);
            if (!sourceMatched) {
                continue;
            }

            // 如果source匹配成功, 继续匹配destination规则
            // 然后将结果写进map(key: 权重, value: 带权重的实例分组)
            Map<Integer, PrioritySubsets> subsetsMap = new HashMap<>();
            int smallestPriority = -1;
            for (RoutingProto.Destination dest : route.getDestinationsList()) {
                if (dest == null) {
                    continue;
                }

                // 对于outbound规则, 需要匹配DestService服务
                if (ruleMatchType == RuleMatchType.sourceRouteRuleMatch) {
                    if (!RuleUtils.MATCH_ALL.equals(dest.getNamespace().getValue()) && !dest.getNamespace().getValue()
                            .equals(routeInfo.getDestService().getNamespace())) {
                        continue;
                    }

                    if (!RuleUtils.MATCH_ALL.equals(dest.getService().getValue()) && !dest.getService().getValue()
                            .equals(routeInfo.getDestService().getService())) {
                        continue;
                    }
                }

                if (dest.getWeight().getValue() == 0) {
                    continue;
                }

                boolean populated = populateSubsetsFromDest(instances, dest, subsetsMap, multiEnvRouterParamMap);
                if (populated) {
                    int priority = dest.getPriority().getValue();
                    if (smallestPriority < 0 || smallestPriority > priority) {
                        smallestPriority = priority;
                    }
                }
            }

            // 如果未匹配到分组, 继续匹配
            if (MapUtils.isEmpty(subsetsMap)) {
                continue;
            }
            // 匹配到分组, 返回
            return selectInstances(routeInfo, subsetsMap.get(smallestPriority));
        }

        // 全部匹配完成, 未匹配到任何分组, 返回空
        return Collections.emptyList();
    }

    /**
     * populateSubsetsFromDest 根据destination中的规则填充分组列表
     *
     * @param instances 实例信息
     * @param dest 目标规则
     * @param subsetsMap 实例分组
     * @return 是否成功加入subset列表
     */
    private boolean populateSubsetsFromDest(
            ServiceInstances instances, RoutingProto.Destination dest, Map<Integer, PrioritySubsets> subsetsMap,
            Map<String, String> multiEnvRouterParamMap) {
        // 获取subset
        List<Instance> oriInstances = instances.getInstances();
        List<Instance> filteredInstances = new ArrayList<>();
        for (Instance ins : oriInstances) {
            if (!matchMetadata(dest.getMetadataMap(), ins.getMetadata(), false, multiEnvRouterParamMap)) {
                continue;
            }
            filteredInstances.add(ins);
        }

        // subset中无实例
        if (CollectionUtils.isEmpty(filteredInstances)) {
            return false;
        }

        // 根据优先级填充subset列表
        int priority = dest.getPriority().getValue();
        int weight = dest.getWeight().getValue();
        PrioritySubsets weightedSubsets = subsetsMap.get(priority);
        if (weightedSubsets == null) {
            PrioritySubsets prioritySubsets = new PrioritySubsets();

            WeightedSubset weightedSubset = new WeightedSubset();
            weightedSubset.setInstances(filteredInstances);
            weightedSubset.setWeight(weight);

            prioritySubsets.setSubsets(new ArrayList<>(Collections.singletonList(weightedSubset)));
            prioritySubsets.setTotalWeight(weight);

            subsetsMap.put(priority, prioritySubsets);

        } else {
            WeightedSubset weightedSubset = new WeightedSubset();
            weightedSubset.setInstances(filteredInstances);
            weightedSubset.setWeight(weight);

            weightedSubsets.getSubsets().add(weightedSubset);
            weightedSubsets.setTotalWeight(weightedSubsets.getTotalWeight() + weight);
        }
        return true;
    }

    //selectInstances 从subset中选取实例
    private List<Instance> selectInstances(RouteInfo routeInfo, PrioritySubsets weightedSubsets) {
        if (weightedSubsets.getSubsets().size() == 1) {
            return weightedSubsets.getSubsets().get(0).instances;
        }
        Random random = new Random();

        // 根据区间算法选择subset
        long weight = random.nextInt(weightedSubsets.getTotalWeight());
        //匹配的区间下标
        int matchedRange = -1;
        for (WeightedSubset weightedSubset : weightedSubsets.getSubsets()) {
            weight -= weightedSubset.getWeight();
            if (weight < 0) {
                return weightedSubset.getInstances();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public RouteResult router(RouteInfo routeInfo, ServiceInstances instances) {
        // 根据匹配过程修改状态, 默认无路由策略状态
        RuleStatus ruleStatus;
        // 优先匹配inbound规则, 成功则不需要继续匹配outbound规则
        List<Instance> destFilteredInstances = new ArrayList<>();
        List<Instance> sourceFilteredInstances = new ArrayList<>();
        if (routeInfo.getDestRouteRule() != null) {
            destFilteredInstances = getRuleFilteredInstances(routeInfo, instances,
                    RuleMatchType.destRouteRuleMatch);
            if (destFilteredInstances.isEmpty()) {
                ruleStatus = RuleStatus.destRuleFail;
            } else {
                ruleStatus = RuleStatus.destRuleSucc;
            }
        } else {
            // 然后匹配outbound规则
            sourceFilteredInstances = getRuleFilteredInstances(routeInfo, instances,
                    RuleMatchType.sourceRouteRuleMatch);
            if (sourceFilteredInstances.isEmpty()) {
                ruleStatus = RuleStatus.sourceRuleFail;
            } else {
                ruleStatus = RuleStatus.sourceRuleSucc;
            }
        }
        switch (ruleStatus) {
            case sourceRuleSucc:
                return new RouteResult(sourceFilteredInstances, RouteResult.State.Next);
            case destRuleSucc:
                return new RouteResult(destFilteredInstances, RouteResult.State.Next);
            default:
                // 如果规则匹配失败, 返回错误
                LOG.error("route rule not match, rule status: {}", ruleStatus);
                throw new PolarisException(ErrorCode.ROUTE_RULE_NOT_MATCH, "getFilteredInstances route rule not match");
        }
    }


    private List<Instance> getHealthyInstances(List<Instance> instances) {
        List<Instance> healthyInstances = new ArrayList<>();
        for (Instance instance : instances) {
            if (instance.isHealthy()) {
                healthyInstances.add(instance);
            }
        }

        return healthyInstances;
    }

    private List<Instance> getHealthyInstancesOrAllIfAllDead(List<Instance> instances) {
        List<Instance> healthyInstances = getHealthyInstances(instances);
        if (!healthyInstances.isEmpty()) {
            //如果有健康实例，则只返回健康实例
            LOG.info("getHealthyInstancesOrAllIfAllDead found {} healthy instances, return heathy ones",
                    healthyInstances.size());
            return healthyInstances;
        }
        return instances;
    }

    private List<Instance> getInstancesOfEnv(ServiceInstances serviceInstances, String env) {
        List<Instance> instances = serviceInstances.getInstances();
        List<Instance> retInstances = new ArrayList<>();
        for (Instance ins : instances) {
            if (env.equals(ins.getMetadata().get("env"))) {
                retInstances.add(ins);
            }
        }

        return retInstances;
    }


    @Override
    public String getName() {
        return ServiceRouterConfig.DEFAULT_ROUTER_RULE;
    }

    @Override
    public PluginType getType() {
        return PluginTypes.SERVICE_ROUTER.getBaseType();
    }

    @Override
    public void init(InitContext ctx) throws PolarisException {
        globalVariablesConfig = ctx.getConfig().getGlobal().getSystem().getVariables();
    }

    @Override
    public ServiceRouter.Aspect getAspect() {
        return ServiceRouter.Aspect.MIDDLE;
    }

    @Override
    public boolean enable(RouteInfo routeInfo, ServiceMetadata dstSvcInfo) {
        List<RoutingProto.Route> dstRoutes = getRoutesFromRule(routeInfo, RuleMatchType.destRouteRuleMatch);
        List<RoutingProto.Route> srcRoutes = getRoutesFromRule(routeInfo, RuleMatchType.sourceRouteRuleMatch);
        return !(CollectionUtils.isEmpty(dstRoutes) && CollectionUtils.isEmpty(srcRoutes));
    }
}