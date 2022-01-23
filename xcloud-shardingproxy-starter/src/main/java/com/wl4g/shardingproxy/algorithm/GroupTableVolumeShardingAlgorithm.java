/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.shardingproxy.algorithm;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;

/**
 * Based group volume range sharding algorithm.
 * 
 * for example:
 * 
 * <pre>
 * {
 *   "id": [
 *     {
 *       lowerClose: 0,
 *       upperOpen: 4000_0000,
 *       expression: "rw_userdb_r0z0mgr0_db${(id % 2) as int}"
 *     },
 *     {
 *       lowerClose: 4000_0000,
 *       upperOpen: 20000_0000,
 *       expression: "rw_userdb_r0z0mgr1_db${(id % 7) as int}"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-19 v1.0.0
 * @since v1.0.0
 */
@Slf4j
public class GroupTableVolumeShardingAlgorithm extends BaseGroupVolumeShardingAlgorithm {

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames,
            final RangeShardingValue<Comparable<?>> shardingValue) {
        // for example: 4000_0000<=shardingValue<=5000_0000
        //
        // Traverse all partition range configurations, as long as there is an
        // intersection with the shardingValue, it should be used as the target
        // data nodes.

        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        long lowerValue = getLowerValue(shardingValue.getColumnName(), shardingValue.getValueRange());
        long upperValue = getUpperValue(shardingValue.getColumnName(), shardingValue.getValueRange());
        Range<Comparable<?>> rangeValue = Range.range(lowerValue, BoundType.CLOSED, upperValue, BoundType.OPEN);

        for (Entry<Integer, Range<Comparable<?>>> entry : getPartitionRangeMap()
                .getOrDefault(shardingValue.getColumnName(), emptyMap()).entrySet()) {
            // Check if two range have intersection?
            if (hasIntersection(rangeValue, entry.getValue())) {
                List<AlgorithmExpression> expressions = getAlgorithmExprMap().getOrDefault(shardingValue.getColumnName(),
                        emptyList());
                AlgorithmExpression expr = expressions.get(entry.getKey());
                Closure<?> closure = createClosure(expr.getSuffixExpr());
                // TODO by default -1
                closure.setProperty(shardingValue.getColumnName(), -1);
                result.add(expr.getPrefixStr().concat(closure.call().toString()));
            }
        }

        log.debug("Determined range sharding targets: {}, by shardingValue: {}, availableTargetNames: {}", result, shardingValue,
                availableTargetNames);
        return result;
    }

    @Override
    public String getType() {
        return "GROUP_VOLUME_TABLE";
    }

}
