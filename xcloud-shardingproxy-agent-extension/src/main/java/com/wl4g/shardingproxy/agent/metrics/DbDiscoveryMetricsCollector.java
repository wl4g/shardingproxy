/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.shardingproxy.agent.metrics;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;

import com.wl4g.shardingproxy.agent.event.DbDiscoveryEventHandler;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.Describable;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link DbDiscoveryMetricsCollector}</br>
 * 
 * for example shardingsphere-agent-metrics-prometheus startup the flow sources
 * code:
 * {@link org.apache.shardingsphere.agent.bootstrap.ShardingSphereAgent#premain()}
 * {@link org.apache.shardingsphere.agent.core.plugin.ApmPluginLoader#loadAllPlugins()}
 * {@link org.apache.shardingsphere.agent.metrics.prometheus.service,PrometheusPluginBootService#startServer()}
 * {@link io.prometheus.client.exporter.HTTPServer.HTTPMetricHandler#handle()}
 * {@link io.prometheus.client.CollectorRegistry#filteredMetricFamilySamples()}
 * {@link io.prometheus.client.CollectorRegistry#findNextElement()}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-19 v1.0.0
 * @since v1.0.0
 */
@Slf4j
public class DbDiscoveryMetricsCollector extends Collector implements Describable {

    public DbDiscoveryMetricsCollector() {
        CollectorRegistry.defaultRegistry.register(this);
    }

    @Override
    public List<MetricFamilySamples> describe() {
        return FACTORY.getDefinitionMetrics().stream().map(m -> {
            Type type = Type.valueOf((String) m.get("type"));
            return new MetricFamilySamples((String) m.get("name"), (String) m.get("unit"), type, (String) m.get("help"), null);
        }).collect(toList());
    }

    /**
     * Refer to:
     * {@link org.apache.shardingsphere.agent.metrics.prometheus.collector.ProxyInfoCollector#collect()}
     */
    @Override
    public List<MetricFamilySamples> collect() {
        log.debug("Collecting dbdiscovery metrics ...");

        LinkedList<PrimaryDataSourceChangedEvent> eventQueue = DbDiscoveryEventHandler.getInstance().getEventQueue();
        List<MetricFamilySamples> result = new LinkedList<>();
        Optional<GaugeMetricFamily> writeDS = FACTORY.createGaugeMetricFamily(MetricIds.DB_DISCOVERY_WRITE_DS);
        writeDS.ifPresent(m -> {
            while (!eventQueue.isEmpty()) {
                PrimaryDataSourceChangedEvent event = eventQueue.pollFirst();
                m.addMetric(Arrays.asList(event.getSchemaName(), event.getGroupName(), event.getDataSourceName()),
                        /* DS_STATE_MAP.get("") */1);
            }
        });
        writeDS.ifPresent(result::add);
        return result;
    }

    private static final ExtensionPrometheusWrapperFactory FACTORY = new ExtensionPrometheusWrapperFactory();
    private static final ConcurrentHashMap<String, Integer> DS_STATE_MAP = new ConcurrentHashMap<>();

    static {
        DS_STATE_MAP.put("RW", 1); // read-write
        DS_STATE_MAP.put("R", 2); // read
    }

}
