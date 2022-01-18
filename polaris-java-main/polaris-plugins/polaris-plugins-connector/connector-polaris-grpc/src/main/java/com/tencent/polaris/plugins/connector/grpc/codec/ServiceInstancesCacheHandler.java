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

package com.tencent.polaris.plugins.connector.grpc.codec;

import com.tencent.polaris.api.plugin.registry.CacheHandler;
import com.tencent.polaris.api.pojo.RegistryCacheValue;
import com.tencent.polaris.api.pojo.ServiceEventKey.EventType;
import com.tencent.polaris.client.pb.ResponseProto.DiscoverResponse;
import com.tencent.polaris.client.pojo.ServiceInstancesByProto;
import java.util.function.Function;

public class ServiceInstancesCacheHandler implements CacheHandler {

    @Override
    public EventType getTargetEventType() {
        return EventType.INSTANCE;
    }

    @Override
    public CachedStatus compareMessage(RegistryCacheValue oldValue, Object newValue) {
        DiscoverResponse discoverResponse = (DiscoverResponse) newValue;
        return CommonHandler.compareMessage(getTargetEventType(), oldValue, discoverResponse,
                new Function<DiscoverResponse, String>() {
                    @Override
                    public String apply(DiscoverResponse discoverResponse) {
                        return discoverResponse.getService().getRevision().getValue();
                    }
                });
    }

    @Override
    public RegistryCacheValue messageToCacheValue(RegistryCacheValue oldValue, Object newValue, boolean isCacheLoaded) {
        DiscoverResponse discoverResponse = (DiscoverResponse) newValue;
        ServiceInstancesByProto oldServiceInstances = null;
        if (null != oldValue) {
            oldServiceInstances = (ServiceInstancesByProto) oldValue;
        }
        return new ServiceInstancesByProto(discoverResponse, oldServiceInstances, isCacheLoaded);
    }
}
