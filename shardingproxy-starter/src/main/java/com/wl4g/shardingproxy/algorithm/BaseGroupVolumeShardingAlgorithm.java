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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import groovy.lang.Closure;
import groovy.util.Expando;
import lombok.Getter;
import lombok.Setter;
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
@Getter
public abstract class BaseGroupVolumeShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private static final String ALGORITHM_EXPRESSION_KEY = "algorithm-expression";

    @Getter
    @Setter
    private Properties props = new Properties();

    private volatile Map<String, List<AlgorithmExpression>> algorithmExprMap;
    private volatile Map<String, Map<Integer, Range<Comparable<?>>>> partitionRangeMap;

    @Override
    public void init() {
        initPartitionRange();
    }

    @Override
    public String doSharding(final Collection<String> availableTargetNames,
            final PreciseShardingValue<Comparable<?>> shardingValue) {
        Integer partition = getPartition(shardingValue.getColumnName(), shardingValue.getValue());
        // miss range index.
        if (isNull(partition)) {
            // When it does not belong to any partition, it has exceeded the
            // scope of the shard configuration, and it is reasonable to query
            // no data.
            throw new IllegalArgumentException(format(
                    "Shard value: %s does not match any shard configuration range, please confirm whether the shard configuration is correct",
                    shardingValue));
        }

        AlgorithmExpression expr = algorithmExprMap.getOrDefault(shardingValue.getColumnName(), emptyList()).get(partition);
        Closure<?> closure = createClosure(expr.getSuffixExpr());
        closure.setProperty(shardingValue.getColumnName(), shardingValue.getValue());
        String result = closure.call().toString();

        List<String> candidateTargetNames = safeMap(expr.getResultMapping()).entrySet().stream()
                .filter(e -> safeList(e.getValue()).stream().anyMatch(e0 -> StringUtils.equals(e0, result))).map(e -> e.getKey())
                .collect(toList());
        if (candidateTargetNames.isEmpty()) {
            log.debug(format(
                    "Fallback to use non-mapping target: %s as candidate, could not determine sharding target because candidate is empty, by expr: %s, result: %s, shardingValue: %s, availableTargetNames: %s",
                    result, shardingValue, candidateTargetNames, expr.getSuffixExpr(), result, shardingValue,
                    availableTargetNames));
            candidateTargetNames.add(result);
        } else if (candidateTargetNames.size() > 1) {
            throw new IllegalStateException(format(
                    "Could not determine multi candidate sharding target, by expr: %s, result: %s, shardingValue: %s, availableTargetNames: %s",
                    candidateTargetNames, expr.getSuffixExpr(), result, shardingValue, availableTargetNames));
        }

        log.debug("Determined precise sharding target: {}, by shardingValue: {}, availableTargetNames: {}",
                candidateTargetNames.get(0), shardingValue, availableTargetNames);
        return expr.getPrefixStr().concat(candidateTargetNames.get(0));
    }

    protected Long getLowerValue(final String columnName, final Range<Comparable<?>> valueRange) {
        return valueRange.hasLowerBound() ? Long.parseLong(valueRange.lowerEndpoint().toString()) : 0;
    }

    protected Long getUpperValue(final String columnName, final Range<Comparable<?>> valueRange) {
        return valueRange.hasUpperBound() ? Long.parseLong(valueRange.upperEndpoint().toString()) : partitionRangeMap.size() - 1;
    }

    protected Integer getPartition(final String columnName, final Comparable<?> value) {
        for (Entry<Integer, Range<Comparable<?>>> entry : partitionRangeMap.getOrDefault(columnName, emptyMap()).entrySet()) {
            if (entry.getValue().contains(Long.parseLong(value.toString()))) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected Closure<?> createClosure(String expressionString) {
        Closure<?> result = new InlineExpressionParser(expressionString).evaluateClosure().rehydrate(new Expando(), null, null);
        result.setResolveStrategy(Closure.DELEGATE_ONLY);
        return result;
    }

    protected boolean hasIntersection(Range<Comparable<?>> rangeValue, Range<Comparable<?>> range) {
        try {
            return !range.intersection(rangeValue).isEmpty();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void initPartitionRange() {
        Preconditions.checkState(props.containsKey(ALGORITHM_EXPRESSION_KEY), "Sharding algorithm expression cannot be null.");
        algorithmExprMap = parseJSON(props.getProperty(ALGORITHM_EXPRESSION_KEY),
                new TypeReference<LinkedHashMap<String, List<AlgorithmExpression>>>() {
                });
        Preconditions.checkArgument((nonNull(algorithmExprMap) && !algorithmExprMap.isEmpty()), "Sharding ranges is not valid.");
        partitionRangeMap = new HashMap<>(algorithmExprMap.size(), 1);
        algorithmExprMap.forEach((key, ranges) -> {
            int i = 0;
            Map<Integer, Range<Comparable<?>>> columnExpressionMap = new HashMap<>(ranges.size(), 1);
            for (AlgorithmExpression ele : ranges) {
                columnExpressionMap.put(i++, Range.closedOpen(ele.getLowerClose(), ele.getUpperOpen()));
            }
            partitionRangeMap.put(key, columnExpressionMap);
        });
    }

    @Getter
    @Setter
    public static class AlgorithmExpression {
        private Long lowerClose;
        private Long upperOpen;
        private String prefixStr;
        private String suffixExpr;
        private Map<String, List<String>> resultMapping;
    }

}
