/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.common.DefaultDivider;
import com.navercorp.pinpoint.batch.common.Divider;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.Optional;

/**
 * @author minwoo.jung
 */
public class AgentCountPartitioner implements Partitioner {
    private static final String PARTITION_NAME_PREFIX = "agent_count_partition_number_";
    private static final String BATCH_NAME = "agent_count_batch";

    private final Divider divider;

    public AgentCountPartitioner(@Qualifier("divider") Optional<Divider> divider) {
        this.divider = divider.orElseGet(DefaultDivider::new);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        return divider.divide(PARTITION_NAME_PREFIX, BATCH_NAME);
    }
}
