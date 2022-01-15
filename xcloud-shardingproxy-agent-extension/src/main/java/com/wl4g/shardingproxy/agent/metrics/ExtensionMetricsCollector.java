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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.lang.System.currentTimeMillis;
//import static java.util.stream.Collectors.groupingBy;
//import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.StringUtils.split;
//import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
//import static org.apache.commons.lang3.StringUtils.join;

//import java.lang.reflect.Field;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
//import java.util.LinkedHashMap;
//import java.util.concurrent.atomic.AtomicBoolean;

//import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.CounterWrapper;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.agent.metrics.api.util.MetricsUtil;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import com.google.common.eventbus.Subscribe;
//import com.wl4g.component.common.reflect.ReflectionUtils2;
//import com.wl4g.shardingproxy.agent.event.DatabaseDiscoveryEventHandler;

//import io.prometheus.client.Counter;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ExtensionMetricsCollector}</br>
 * 
 * for example shardingsphere-agent-metrics-prometheus startup the flow sources
 * code:
 * {@link org.apache.shardingsphere.agent.bootstrap.ShardingSphereAgent#premain()}
 * {@link org.apache.shardingsphere.agent.core.plugin.ApmPluginLoader#loadAllPlugins()}
 * {@link org.apache.shardingsphere.agent.core.plugin.PluginBootServiceManager#startAllServices(Map)}
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
public class ExtensionMetricsCollector
        extends Collector /* implements Describable */ {

    public ExtensionMetricsCollector() {
        ShardingSphereEventBus.getInstance().register(this);
    }

    //
    // Registered on
    // org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory
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
     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator#renew()}
     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber#update()}
     */
    @Subscribe
    public void onPrimaryDataSourceChanged(PrimaryDataSourceChangedEvent event) {
        log.warn("On changed primaryDataSource event: ({}) - {}.{}.{}", PrimaryDataSourceChangedEvent.class.getSimpleName(),
                event.getSchemaName(), event.getGroupName(), event.getDataSourceName());
        COUNTER_PRIMARY_DS_CHANGED.inc(1, event.getSchemaName(), event.getGroupName());
    }

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
        collectBasicMetrics(result);
        collectPrimaryDataSourcesMetrics(result);
        collectAllAndDisableDataSourcesMetrics(result);
        return result;
    }

    private void collectBasicMetrics(final List<MetricFamilySamples> result) {
        Optional<GaugeMetricFamily> gauge = FACTORY.createGaugeMetricFamily(MetricIds.EXT_BASIC_UPTIME);
        gauge.ifPresent(m -> {
            String hostname = "unknown";
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn("Cannot get local hostname, cause by {}", e.getMessage());
            }
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
            m.addMetric(singletonList(hostname), (currentTimeMillis() - bean.getStartTime()) / 1000);
            result.add(m);
        });
    }

    /**
     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber}
     */
    private void collectPrimaryDataSourcesMetrics(final List<MetricFamilySamples> result) {
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts()
                .getMetaDataPersistService();

        persistService.ifPresent(p -> {
            // // Calculate current primary dataSources changed count metrics.
            // LinkedList<PrimaryDataSourceChangedEvent> queue =
            // DatabaseDiscoveryEventHandler.getInstance().getEventQueue();
            // if (queue.size() == 0) {
            // queue.add(new PrimaryDataSourceChangedEvent("userdb_g0db0",
            // "ha_userdb_g0db0", "userdb_g0db0_0"));
            // }
            //
            // LinkedHashMap<String, List<PrimaryDataSourceChangedEvent>>
            // groupMap = queue.stream().collect(
            // groupingBy(event -> join(event.getGroupName(), "@",
            // event.getSchemaName()), LinkedHashMap::new, toList()));
            // AtomicBoolean hasChanged = new AtomicBoolean(false);
            // for (String key : groupMap.keySet()) {
            // List<PrimaryDataSourceChangedEvent> events =
            // groupMap.getOrDefault(key, emptyList());
            // // A changed in the primary dataSources
            // if (events.size() == 1) {
            // hasChanged.set(true);
            // COUNTER_PRIMARY_DS_CHANGED.inc(1, events.get(0).getSchemaName(),
            // events.get(0).getGroupName());
            // } else {
            // for (int i = 1; i < events.size(); i++) {
            // PrimaryDataSourceChangedEvent last = events.get(i - 1);
            // PrimaryDataSourceChangedEvent event = events.get(i);
            // if (!equalsAnyIgnoreCase(last.getDataSourceName(),
            // event.getDataSourceName())) {
            // hasChanged.set(true);
            // COUNTER_PRIMARY_DS_CHANGED.inc(1, event.getSchemaName(),
            // event.getGroupName());
            // }
            // }
            // }
            // }
            // if (hasChanged.get()) {
            // Counter c = ReflectionUtils2.getField(COUNTER_FIELD,
            // COUNTER_PRIMARY_DS_CHANGED, true);
            // result.addAll(c.collect());
            // queue.clear();
            // }

            // Gets current primary dataSources metrics.
            Optional<GaugeMetricFamily> gauge = FACTORY.createGaugeMetricFamily(MetricIds.EXT_DB_DISCOVERY_PRIMARY_DS);
            gauge.ifPresent(m -> {
                // persistService.get().getRepository().getProps().getProperty("namespace");
                String statusPrimaryPath = StorageStatusNode.getStatusPath(StorageNodeStatus.PRIMARY);
                List<String> primaryKeys = p.getRepository().getChildrenKeys(statusPrimaryPath);
                safeList(primaryKeys).stream().forEach(schemaAndDiscoveryGroupName -> {
                    String path = statusPrimaryPath.concat("/").concat(schemaAndDiscoveryGroupName);
                    String primaryDataSourceName = p.getRepository().get(path);
                    String[] parts = split(schemaAndDiscoveryGroupName, ".");
                    String schemaName = parts[0];
                    String discoveryGroupName = parts[1];
                    m.addMetric(Arrays.asList(schemaName, discoveryGroupName, primaryDataSourceName), 1);

                    // If the primary dataSources has never been changed, then
                    // all DB-discovery metrics will be populated by default.
                    COUNTER_PRIMARY_DS_CHANGED.inc(0, schemaName, discoveryGroupName);
                });
                result.add(m);
            });
        });
    }

    /**
     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber}
     */
    private void collectAllAndDisableDataSourcesMetrics(final List<MetricFamilySamples> result) {
        Optional<GaugeMetricFamily> disabledGauge = FACTORY.createGaugeMetricFamily(MetricIds.EXT_DB_DISCOVERY_DISABLE_DS);
        Optional<GaugeMetricFamily> allGauge = FACTORY.createGaugeMetricFamily(MetricIds.EXT_DB_DISCOVERY_ALL_DS);

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
                // Gets all dataSourceNames. (ShardingSphere has watch the
                // register center and kept it up to date.)
                safeMap(metaDataContexts.getMetaDataMap()).values().stream().forEach(meta -> {
                    String schemaName = meta.getName();
                    Set<String> dataSourceNames = meta.getResource().getDataSources().keySet();
                    safeList(dataSourceNames).forEach(dsname -> {
                        m.addMetric(Arrays.asList(schemaName, dsname), 1);
                    });
                });
                result.add(m);
            });
        }
    }

    private static final String PROXY_CONTEXT_CLASS_STR = "org.apache.shardingsphere.proxy.backend.context.ProxyContext";
    // private static final Field
    // COUNTER_FIELD=ReflectionUtils2.findField(CounterWrapper.class,"counter",Counter.class);

    private static final ExtensionPrometheusWrapperFactory FACTORY = new ExtensionPrometheusWrapperFactory();
    private static final MetricsWrapper COUNTER_PRIMARY_DS_CHANGED;

    static {
        COUNTER_PRIMARY_DS_CHANGED = FACTORY.create(MetricIds.EXT_DB_DISCOVERY_PRIMARY_DS_CHANGED_COUNTER).get();
    }

}
