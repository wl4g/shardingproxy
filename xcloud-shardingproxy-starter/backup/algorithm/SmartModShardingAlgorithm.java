/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.component.integration.sharding.algorithm;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import com.wl4g.component.common.annotation.Reserved;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link SmartModShardingAlgorithm}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-16
 * @sine v1.0
 * @see
 */
@Getter
@Setter
@Reserved
public class SmartModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {

    private static final String EXPRESSION_KEY = "algorithm-expression";

    private Properties props = new Properties();
    private ShardingModAssignExpression shardingModAssignExpression;
    private int shardingAllTableCount;

    @Override
    public void init() {
        shardingModAssignExpression = getShardingModAssignExpression();
        shardingAllTableCount = (int) shardingModAssignExpression.getShardingAssign().values().stream()
                .flatMap(mods -> mods.stream()).count();
    }

    private ShardingModAssignExpression getShardingModAssignExpression() {
        notNullOf(props.containsKey(EXPRESSION_KEY), "Sharding mod assign expression cannot be null.");
        return ShardingModAssignExpression.parse(props.get(EXPRESSION_KEY).toString());
    }

    @Override
    public String doSharding(final Collection<String> availableTargetNames,
            final PreciseShardingValue<Comparable<?>> shardingValue) {
        for (String targetName : availableTargetNames) {
            List<Long> mods = shardingModAssignExpression.getShardingAssign().get(targetName);
            if (nonNull(mods) && mods.contains(getLongValue(shardingValue.getValue()) % shardingAllTableCount)) {
                return targetName;
            }
        }
        return null;
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames,
            final RangeShardingValue<Comparable<?>> shardingValue) {
        return isContainAllTargets(shardingValue) ? availableTargetNames
                : getAvailableTargetNames(availableTargetNames, shardingValue);
    }

    private boolean isContainAllTargets(final RangeShardingValue<Comparable<?>> shardingValue) {
        return !shardingValue.getValueRange().hasUpperBound()
                || shardingValue.getValueRange().hasLowerBound() && getLongValue(shardingValue.getValueRange().upperEndpoint())
                        - getLongValue(shardingValue.getValueRange().lowerEndpoint()) >= shardingAllTableCount - 1;
    }

    private Collection<String> getAvailableTargetNames(final Collection<String> availableTargetNames,
            final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for (long i = getLongValue(shardingValue.getValueRange().lowerEndpoint()); i <= getLongValue(
                shardingValue.getValueRange().upperEndpoint()); i++) {
            for (String targetName : availableTargetNames) {
                List<Long> mods = shardingModAssignExpression.getShardingAssign().get(targetName);
                if (nonNull(mods) && mods.contains(i % shardingAllTableCount)) {
                    result.add(targetName);
                }
            }
        }
        return result;
    }

    private long getLongValue(final Comparable<?> value) {
        return Long.parseLong(value.toString());
    }

    @Override
    public int getAutoTablesAmount() {
        return shardingAllTableCount;
    }

    @Override
    public String getType() {
        return "SMART_MOD";
    }

    @Override
    public Collection<String> getAllPropertyKeys() {
        return asList(EXPRESSION_KEY);
    }

    @Getter
    static final class ShardingModAssignExpression {
        private final Map<String, List<Long>> shardingAssign = synchronizedMap(new HashMap<>(4));

        public static ShardingModAssignExpression parse(String expression) {
            notNullOf(expression, "Sharding mod assign expression cannot be null.");
            isTrue(expression.contains("->"), ERR_INVALID_EXPRESSION_MSG);

            ShardingModAssignExpression resolved = new ShardingModAssignExpression();
            for (String parts : safeArrayToList(split(expression, "|"))) {
                String[] dbAndMods = split(trimToEmpty(parts), "->"); // g0db0->0,1,2,3
                isTrue(nonNull(dbAndMods) && dbAndMods.length == 2, ERR_INVALID_EXPRESSION_MSG);

                List<Long> mods = safeArrayToList(split(dbAndMods[1], ",")).stream().map(m -> Long.parseLong(trimToEmpty(m)))
                        .collect(toList());
                isTrue(!mods.isEmpty(), ERR_INVALID_EXPRESSION_MSG);

                resolved.shardingAssign.put(trimToEmpty(dbAndMods[0]), mods);
            }
            return resolved;
        }

        public static final String ERR_INVALID_EXPRESSION_MSG = "Sharding mod assign expression invalid. for example: 'g0db0->0,1,2,3|g0db1->4,5,6|g0db2->7,8,9'";
    }

}
