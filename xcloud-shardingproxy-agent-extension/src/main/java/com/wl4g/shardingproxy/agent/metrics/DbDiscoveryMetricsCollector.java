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

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.split;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.agent.metrics.api.util.MetricsUtil;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import io.prometheus.client.Collector;
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
public class DbDiscoveryMetricsCollector
        extends Collector /* implements Describable */ {

    public DbDiscoveryMetricsCollector() {
        CollectorRegistry.defaultRegistry.register(this);
    }

    //
    // Registered on PrometheusWrapperFactory
    //
    // @Override
    // public List<MetricFamilySamples> describe() {
    // return FACTORY.getDefinitionMetrics().stream().map(m -> {
    // Type type = Type.valueOf((String) m.get("type"));
    // return new MetricFamilySamples((String) m.get("name"), (String)
    // m.getOrDefault("unit", ""), type,
    // (String) m.getOrDefault("help", ""), emptyList());
    // }).collect(toList());
    // }

    /**
     * Refer to:
     * {@link org.apache.shardingsphere.agent.metrics.prometheus.collector.ProxyInfoCollector#collect()}
     * {@link org.apache.shardingsphere.agent.metrics.prometheus.collector.MetaDataInfoCollector#collect()}
     */
    @Override
    public List<MetricFamilySamples> collect() {
        log.debug("Collecting dbdiscovery metrics ...");

        List<MetricFamilySamples> result = new LinkedList<>();
        if (MetricsUtil.classNotExist(PROXY_CONTEXT_CLASS_STR)) {
            return result;
        }

        // Optional<GaugeMetricFamily> gauge =
        // FACTORY.createGaugeMetricFamily(MetricIds.DB_DISCOVERY_PRIMARY_DS);
        // if (MetricsUtil.classNotExist(PROXY_CONTEXT_CLASS_STR) ||
        // !gauge.isPresent()) {
        // return result;
        // }
        // LinkedList<PrimaryDataSourceChangedEvent> eventQueue =
        // DbDiscoveryEventHandler.getInstance().getEventQueue();
        // gauge.ifPresent(m -> {
        // while (!eventQueue.isEmpty()) {
        // PrimaryDataSourceChangedEvent event = eventQueue.pollFirst();
        // m.addMetric(Arrays.asList(event.getSchemaName(),
        // event.getGroupName(), event.getDataSourceName()), 1);
        // }
        // });

        collectPrimaryDataSources(result);
        collectAllAndDisableDataSources(result);
        return result;
    }

    /**
     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber}
     */
    private void collectPrimaryDataSources(final List<MetricFamilySamples> result) {
        Optional<GaugeMetricFamily> gauge = FACTORY.createGaugeMetricFamily(MetricIds.DB_DISCOVERY_PRIMARY_DS);
        gauge.ifPresent(m -> {
            MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
            Optional<MetaDataPersistService> persistService = metaDataContexts.getMetaDataPersistService();
            if (persistService.isPresent()) {
                // persistService.get().getRepository().getProps().getProperty("namespace");
                String statusPrimaryPath = StorageStatusNode.getStatusPath(StorageNodeStatus.PRIMARY);
                List<String> primaryKeys = persistService.get().getRepository().getChildrenKeys(statusPrimaryPath);
                safeList(primaryKeys).stream().forEach(schemaAndDiscoveryGroupName -> {
                    String path = statusPrimaryPath.concat("/").concat(schemaAndDiscoveryGroupName);
                    String primaryDataSourceName = persistService.get().getRepository().get(path);
                    String[] parts = split(schemaAndDiscoveryGroupName, ".");
                    m.addMetric(Arrays.asList(parts[0], parts[1], primaryDataSourceName), 1);
                });
                result.add(m);
            }
        });
    }

    /**
     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber}
     */
    private void collectAllAndDisableDataSources(final List<MetricFamilySamples> result) {
        Optional<GaugeMetricFamily> disabledGauge = FACTORY.createGaugeMetricFamily(MetricIds.DB_DISCOVERY_DISABLE_DS);
        Optional<GaugeMetricFamily> allGauge = FACTORY.createGaugeMetricFamily(MetricIds.DB_DISCOVERY_DS);

        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<MetaDataPersistService> persistService = metaDataContexts.getMetaDataPersistService();
        if (persistService.isPresent()) {
            // Gets disabled paths.
            String statusDisablePath = StorageStatusNode.getStatusPath(StorageNodeStatus.DISABLE);
            List<String> disableKeys = safeList(persistService.get().getRepository().getChildrenKeys(statusDisablePath));

            // Transform to dataSourceNames.
            List<String> disableDataSourceNames = disableKeys.stream().map(k -> split(k, ".")[1]).collect(toList());

            // Transform to mapping of dataSourceName to schemaName.
            Map<String, String> disabledSchemaDataSourcesMap = disableKeys.stream().map(schemaAndDataSourceName -> {
                String[] parts = split(schemaAndDataSourceName, ".");
                String schemaName = parts[0];
                String dataSourceName = parts[1];
                return singletonMap(dataSourceName, schemaName);
            }).flatMap(map -> map.entrySet().stream()).collect(toMap(k -> k.getKey(), v -> v.getValue()));

            // Gets all dataSourceNames. (ShardingSphere has watch the
            // remote registry and kept it up to date.)
            Collection<String> allDataSourceNames = safeMap(
                    ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap()).values().stream()
                            .map(meta -> meta.getResource().getDataSources().keySet()).flatMap(names -> names.stream())
                            .collect(toList());

            // List<String> mergedDataSourceNames =
            // Stream.of(remoteDisableDataSourceNames, localDataSourceNames)
            // .flatMap(Collection::stream).distinct().collect(Collectors.toList());

            // Add disabled dataSources to metrics.
            disabledGauge.ifPresent(m -> {
                disableDataSourceNames.stream().forEach(dsname -> {
                    Optional<String> schemaName = Optional.ofNullable(disabledSchemaDataSourcesMap.get(dsname));
                    if (schemaName.isPresent()) {
                        m.addMetric(Arrays.asList(schemaName.get(), dsname), 1);
                    } else {
                        // log.warn(
                        // "The mapping to {} and {} is not matched, and the
                        // local configuration and remote configuration of the
                        // data source are changed? and it has not been updated
                        // in time? - remote dataSources: {}, local dataSources:
                        // {}", schemaName, dsname,
                        // remoteDisableDataSourceNames,
                        // localDataSourceNames);
                    }
                });
                result.add(m);
            });

            // Add all dataSources to metrics.
            allGauge.ifPresent(m -> {
                allDataSourceNames.stream().forEach(dsname -> {
                    Optional<String> schemaName = Optional.ofNullable(disabledSchemaDataSourcesMap.get(dsname));
                    if (schemaName.isPresent()) {
                        m.addMetric(Arrays.asList(schemaName.get(), dsname), 1);
                    }
                });
                result.add(m);
            });
        }
    }

    private static final ExtensionPrometheusWrapperFactory FACTORY = new ExtensionPrometheusWrapperFactory();

    private static final String PROXY_CONTEXT_CLASS_STR = "org.apache.shardingsphere.proxy.backend.context.ProxyContext";

}
