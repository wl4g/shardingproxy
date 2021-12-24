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
package com.wl4g.component.integration.sharding.failover;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.Assert2.state;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import com.google.common.net.HostAndPort;
import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.lang.Assert2;
import com.wl4g.component.common.lang.HostUtils;
import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.task.GenericTaskRunner;
import com.wl4g.component.common.task.RunnerProperties;
import com.wl4g.component.common.task.RunnerProperties.StartupMode;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats.NodeInfo;
import com.wl4g.component.integration.sharding.failover.config.FailoverConfiguration;
import com.wl4g.component.integration.sharding.failover.config.FailoverConfiguration.FailoverAdminDataSourceConfig;
import com.wl4g.component.integration.sharding.failover.config.FailoverConfiguration.FailoverAdminDataSourceConfig.DataSourceAddressMapping;
import com.wl4g.component.integration.sharding.failover.exception.FailoverException;
import com.wl4g.component.integration.sharding.failover.exception.InvalidStateFailoverException;
import com.wl4g.component.integration.sharding.failover.exception.NoNextAdminDataSourceFailoverException;
import com.wl4g.component.integration.sharding.failover.exception.UnreachablePrimaryNodeFailoverException;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.jdbc.DelegateAdminDataSourceWrapper;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.util.JdbcUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil.JdbcInformation;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * It has been officially implemented
 * {@link org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType},
 * but the 5.0.0-beta is still very unstable. Therefore, at present, we still
 * use the self implemented failover.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public abstract class AbstractProxyFailover<S extends NodeStats> extends GenericTaskRunner<RunnerProperties>
        implements ProxyFailover<S> {

    private final FailoverBootstrapInitializer initializer;
    private final ShardingSphereMetaData metadata;
    private final Map<String, DelegateAdminDataSourceWrapper> cachingAdminDataSources = new ConcurrentHashMap<>(4);
    private final Map<String, List<String>> schemaInitDefineRWDataSources = synchronizedMap(new HashMap<>(4));

    public AbstractProxyFailover(FailoverBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(new RunnerProperties(StartupMode.NOSTARTUP, 1));
        this.initializer = notNullOf(initializer, "initializer");
        this.metadata = notNullOf(metadata, "metadata");
        FailoverShutdownManager.getInstance().addShutdownHandler(this);
    }

    @Override
    protected void startingPropertiesSet() {
        FailoverConfiguration failoverConfig = ProxyContext.getInstance().getFailoverConfig();
        getWorker().scheduleWithRandomDelay(this, failoverConfig.getInspectInitialDelayMs(),
                failoverConfig.getInspectMinDelayMs(), failoverConfig.getInspectMaxDelayMs(), MILLISECONDS);

        // Initial read-write dataSources.
        initSchemaReadwriteDefineDataSources();
    }

    @Override
    public void run() {
        final String lockName = getSchemaName().concat(".failover");
        Optional<ShardingSphereLock> op = ProxyContext.getInstance().getLock();
        try {
            Assert2.isTrue(op.isPresent(), FailoverException.class,
                    "Failover running in governance mode, the lock must be enabled. Please check config '%s'",
                    ConfigurationPropertyKey.LOCK_ENABLED.getKey());
            if (op.get().tryLock(lockName, ProxyContext.getInstance().getFailoverConfig().getInspectMinDelayMs())) {
                log.info("Locked, failover processing ...");
                processFailover();
            } else {
                log.warn("No obtain failover lock, skip for processing.");
            }
        } catch (Exception e) {
            log.error("Failed to process backend dbnodes primary standby failover.", e);
        } finally {
            if (op.isPresent()) {
                op.get().releaseLock(lockName);
            }
        }
    }

    /**
     * Read write splitting definition dataSources in initialization schema.
     */
    private void initSchemaReadwriteDefineDataSources() {
        // Initial load final define schema read-write dataSources.
        SchemaConfigurationWrapper initSchemaConfig = loadSchemaConfiguration();

        for (ReadwriteSplittingRuleConfiguration rwRule : safeList(initSchemaConfig.getReadwriteRuleConfigs())) {
            for (ReadwriteSplittingDataSourceRuleConfiguration rwds : rwRule.getDataSources()) {
                List<String> mergeRWDataSources = new ArrayList<>(4);
                mergeRWDataSources.addAll(rwds.getReadDataSourceNames());
                mergeRWDataSources.add(rwds.getWriteDataSourceName());
                schemaInitDefineRWDataSources.put(rwds.getName(), mergeRWDataSources);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processFailover() throws Exception {
        // Load last-time configuration before read-write dataSources change.
        SchemaConfigurationWrapper oldSchemaConfig = loadSchemaConfiguration();

        // Other rule configurations do not need to be update.
        List<RuleConfiguration> newAllRuleConfigs = new ArrayList<>(oldSchemaConfig.getOtherRuleConfigs());

        boolean anyChanaged = false;
        for (ReadwriteSplittingRuleConfiguration oldRwRuleConfig : safeList(oldSchemaConfig.getReadwriteRuleConfigs())) {
            // New read-write-splitting dataSources.
            List<ReadwriteSplittingDataSourceRuleConfiguration> newRwDataSources = new ArrayList<>(4);

            for (ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource : safeList(oldRwRuleConfig.getDataSources())) {
                // Obtain backend node admin dataSource.
                DelegateAdminDataSourceWrapper delegate = obtainSelectedBackendNodeAdminDataSource(oldRwDataSource.getName(),
                        oldRwDataSource.getWriteDataSourceName(), oldRwDataSource.getReadDataSourceNames());

                log.debug("Inspecting ... oldRwDataSourceConfig: {}, actualAdminDataSource: {}",
                        () -> toJSONString(oldRwDataSource), () -> delegate);

                // Try inspecting all primary and standby nodes.
                S newNodeStats = runWithAttempt(delegate, oldRwDataSource);

                // Selection new primary node.
                NodeInfo newPrimaryNode = chooseNewPrimaryNode(newNodeStats.getPrimaryNodes());

                // Transform get new primary dataSource name.
                String newPrimaryDataSourceName = transformToMappedNewPrimaryDataSourceName(
                        oldSchemaConfig.getAllDataSourceConfigs(), oldRwDataSource, newPrimaryNode);
                String oldPrimaryDataSourceName = oldRwDataSource.getWriteDataSourceName();

                // Check dataSource primary changed?
                if (isChangedPrimaryNode(newPrimaryDataSourceName, oldPrimaryDataSourceName)) {
                    // Gets changed new read dataSourceNames
                    List<String> newReadDataSourceNames = transformToMappedNewReadDataSourceNames(
                            oldSchemaConfig.getAllDataSourceConfigs(), oldRwDataSource, newNodeStats.getStandbyNodes());
                    if (CollectionUtils2.isEmpty(newReadDataSourceNames)) {
                        throw new InvalidStateFailoverException(
                                format("No matches found new read dataSource names by standbyNodes: %s",
                                        newNodeStats.getStandbyNodes()));
                    }

                    // New read-write-splitting dataSource.
                    ReadwriteSplittingDataSourceRuleConfiguration newRwDataSource = new ReadwriteSplittingDataSourceRuleConfiguration(
                            oldRwDataSource.getName(), oldRwDataSource.getAutoAwareDataSourceName(), newPrimaryDataSourceName,
                            newReadDataSourceNames, oldRwDataSource.getLoadBalancerName());
                    newRwDataSources.add(newRwDataSource);
                    anyChanaged = true;
                } else { // Not changed
                    newRwDataSources.add(oldRwDataSource);
                    log.debug(
                            "Skiping change read-write-splitting, becuase primary dataSourceName it's up to date. {}, actualSchemaName: {}, schemaName: {}",
                            oldPrimaryDataSourceName, oldRwDataSource.getName(), getSchemaName());
                }
            }
            if (!newRwDataSources.isEmpty()) {
                newAllRuleConfigs
                        .add(new ReadwriteSplittingRuleConfiguration(newRwDataSources, oldRwRuleConfig.getLoadBalancers()));
            }
        }

        // Do changed
        if (anyChanaged && !newAllRuleConfigs.isEmpty()) {
            doChangeReadwriteSplittingRuleConfiguration(newAllRuleConfigs);
        }
    }

    /**
     * Obtain selection back-end node administrator dataSource.
     * 
     * @param haReadwriteDataSourceName
     * @param writeDataSourceName
     * @param readDataSourceNames
     * @return
     * @throws SQLException
     */
    private synchronized DelegateAdminDataSourceWrapper obtainSelectedBackendNodeAdminDataSource(String haReadwriteDataSourceName,
            String writeDataSourceName, List<String> readDataSourceNames) throws SQLException {

        // Merge current schema rule read-write dataSourceNames.
        List<String> mergeSchemaRuleRwDataSourceNames = new ArrayList<>(readDataSourceNames);
        mergeSchemaRuleRwDataSourceNames.add(writeDataSourceName);

        // First, load from caching.
        DelegateAdminDataSourceWrapper delegate = cachingAdminDataSources.get(haReadwriteDataSourceName);
        if (nonNull(delegate)) {
            return delegate;
        }

        log.info("Trying obtain next dbnodes admin dataSource ... - {}", haReadwriteDataSourceName);

        delegate = new DelegateAdminDataSourceWrapper();
        for (Entry<String, DataSource> ent : metadata.getResource().getDataSources().entrySet()) {
            String dsName = ent.getKey();
            // Skip data source names that do not belong to the data source
            // collection of the current read-write separation configuration.
            // (YAML tag: !READWRITE_SPLITTING)
            if (!mergeSchemaRuleRwDataSourceNames.contains(dsName)) {
                continue;
            }

            DataSource ds = ent.getValue();
            try {
                // Find backend node business dataSource jdbcUrl.
                String jdbcUrl = null;
                if (ds instanceof HikariDataSource) {
                    jdbcUrl = ((HikariDataSource) ds).getJdbcUrl();
                } else {
                    throw new UnsupportedOperationException(format("No supported dataSource type. %s", ds.getClass()));
                }
                state(!isBlank(jdbcUrl), "Unable get backend node admin dataSource jdbcUrl.");
                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);

                // Setup basic JDBC configuration.
                FailoverAdminDataSourceConfig adminDataSourceConfig = ProxyContext.getInstance().getFailoverConfig()
                        .getAdminDataSourceConfig(getSchemaName());
                HikariDataSource adminDataSource = new HikariDataSource();
                adminDataSource.setUsername(adminDataSourceConfig.getUsername());
                adminDataSource.setPassword(adminDataSourceConfig.getPassword());
                adminDataSource.setConnectionTimeout(adminDataSourceConfig.getConnectionTimeout());
                adminDataSource.setMaximumPoolSize(adminDataSourceConfig.getMaximumPoolSize());
                adminDataSource.setMinimumIdle(adminDataSourceConfig.getMinimumIdle());
                adminDataSource.setIdleTimeout(adminDataSourceConfig.getIdleTimeout());
                adminDataSource.setMaxLifetime(adminDataSourceConfig.getMaxLifetime());

                // Custom admin JDBC dataSource configuration.
                decorateAdminBackendDataSource(dsName, info.getHost(), info.getPort(), adminDataSource);

                cachingAdminDataSources.put(haReadwriteDataSourceName, delegate.putDataSource(dsName, adminDataSource));
            } catch (Exception e) {
                log.warn("Cannot build backend connection of dataSourceName: {}", dsName);
            }
        }

        if (delegate.available()) {
            return delegate;
        }

        throw new SQLException(format("Cannot to build backend admin delegate dataSource. all biz dataSources: %s",
                metadata.getResource().getDataSources()));
    }

    /**
     * Execution inspecting with attempts.
     * 
     * @param delegate
     * @param rwDs
     * @return
     */
    @SuppressWarnings("unchecked")
    private S runWithAttempt(DelegateAdminDataSourceWrapper delegate, ReadwriteSplittingDataSourceRuleConfiguration rwDs) {
        // Try inspecting all primary and standby nodes.
        S newNodeStats = null;
        boolean attempting = true;
        while (attempting) {
            try (JdbcOperator operator = new JdbcOperator(delegate.get());) {
                // Inspect primary/standby latest information.
                final S result = inspecting(operator);
                log.debug("Inspect result readwrite dataSourceName: {}, nodeStats: {}", () -> rwDs.getName(),
                        () -> toJSONString(result));
                newNodeStats = result;

                // Check inspected result valid?
                if (nonNull(newNodeStats) && newNodeStats.valid()) {
                    attempting = false;
                } else {
                    delegate.next(); // Try to next node
                }
            } catch (NoNextAdminDataSourceFailoverException e) {
                attempting = false;
                delegate.reset(); // Unavailable nodes may have recovered?
                throw e;
            } catch (Exception e) {
                delegate.next();
                log.error("Failed to inspected. readwrite dataSourceName: {}", rwDs.getName(), e);
            }
        }

        if (CollectionUtils2.isEmpty(newNodeStats.getPrimaryNodes())) {
            throw new UnreachablePrimaryNodeFailoverException("No primary node information is currently queried.");
        }
        return newNodeStats;
    }

    /**
     * Custom admin JDBC dataSource configuration.
     * 
     * @param ruleDataSourceName
     * @param ruleDataSourceJdbcHost
     * @param ruldDataSourceJdbcPort
     * @param adminDataSource
     */
    protected abstract void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruldDataSourceJdbcPort, HikariDataSource adminDataSource);

    /**
     * Choosing new primary node {@link NodeInfo}
     * 
     * @param newPrimaryNodes
     * @return
     */
    protected NodeInfo chooseNewPrimaryNode(List<? extends NodeInfo> newPrimaryNodes) {
        return newPrimaryNodes.get(0); // By default
    }

    /**
     * Transform internal address of the database instance to the external
     * mapping address, and then calculate the new primary node dataSourceName.
     * 
     * @param allDataSourceConfigs
     * @param oldRwDataSource
     * @param newPrimaryNode
     * @return
     */
    private String transformToMappedNewPrimaryDataSourceName(Map<String, DataSourceConfiguration> allDataSourceConfigs,
            ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource, NodeInfo newPrimaryNode) {
        // Load init define read-write dataSources.
        List<String> initDefineRwDataSources = schemaInitDefineRWDataSources.get(oldRwDataSource.getName());

        // Transform DB host/port to external(LB) address.
        DataSourceAddressMapping mapping = ProxyContext.getInstance().getFailoverConfig()
                .getAdminDataSourceConfig(getSchemaName()).getMappedByInternalAddress(newPrimaryNode);

        // First, find by newPrimaryNode mapping external addresses.
        for (HostAndPort external : safeList(mapping.getParsedExternalAddrs())) {
            String newPrimaryDataSourceName = findMatchingDataSourceName(allDataSourceConfigs, initDefineRwDataSources,
                    external.getHost(), external.getPort());
            if (!isBlank(newPrimaryDataSourceName)) {
                return newPrimaryDataSourceName;
            }
        }

        // Fallback, find by newPrimaryNode internal address.
        String newPrimaryDataSourceName = findMatchingDataSourceName(allDataSourceConfigs, initDefineRwDataSources,
                newPrimaryNode.getHost(), newPrimaryNode.getPort());
        if (!isBlank(newPrimaryDataSourceName)) {
            return newPrimaryDataSourceName;
        }

        throw new InvalidStateFailoverException(
                format("No matches found new primary dataSource names by newPrimaryNode: %s", newPrimaryNode));
    }

    /**
     * Check whether the master node has been changed.
     * 
     * @param newPrimaryDataSourceName
     * @param oldPrimaryDataSourceName
     * @see https://shardingsphere.apache.org/document/current/cn/features/governance/management/registry-center/#metadataschemenamedatasources
     * @return
     */
    private boolean isChangedPrimaryNode(String newPrimaryDataSourceName, String oldPrimaryDataSourceName) {
        return !StringUtils2.equals(oldPrimaryDataSourceName, newPrimaryDataSourceName);
    }

    /**
     * Transform internal address of the database instance to the external
     * mapping address, and then calculate the new standby nodes
     * dataSourceNames.
     * 
     * @param allDataSourceConfigs
     * @param oldRwDataSource
     * @param newStandbyNodes
     * @return
     */
    private List<String> transformToMappedNewReadDataSourceNames(Map<String, DataSourceConfiguration> allDataSourceConfigs,
            ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource, List<? extends NodeInfo> newStandbyNodes) {

        // Load init define read-write dataSources.
        List<String> initDefineRwDataSources = schemaInitDefineRWDataSources.get(oldRwDataSource.getName());

        List<String> newReadDataSourceNames = new ArrayList<>(4);
        for (NodeInfo node : safeList(newStandbyNodes)) {
            // Transform DB host/port to external(LB) address.
            DataSourceAddressMapping mapping = ProxyContext.getInstance().getFailoverConfig()
                    .getAdminDataSourceConfig(getSchemaName()).getMappedByInternalAddress(node);

            // Find matches definition read dataSource name by external
            // addresses.
            for (HostAndPort external : safeList(mapping.getParsedExternalAddrs())) {
                String dataSourceName = findMatchingDataSourceName(allDataSourceConfigs, initDefineRwDataSources,
                        external.getHost(), external.getPort());
                if (!isBlank(dataSourceName)) {
                    newReadDataSourceNames.add(dataSourceName);
                }
            }
        }
        return newReadDataSourceNames;
    }

    /**
     * Gets the schema name of the target to be processed by the current
     * failover.
     * 
     * Notes: a failover instance is responsible for monitoring a schema.
     * 
     * @return
     */
    protected String getSchemaName() {
        return metadata.getName();
    }

    /**
     * Load schema configuration.
     * 
     * @return
     */
    private SchemaConfigurationWrapper loadSchemaConfiguration() {
        // Load all dataSources.
        Map<String, DataSourceConfiguration> allDataSourceConfigs = initializer.loadDataSourceConfigs(getSchemaName());

        // Load disabled dataSources.
        Collection<String> disableDataSources = initializer.loadDisableDataSources(getSchemaName());

        // Excluding disable dataSource.
        allDataSourceConfigs = safeMap(allDataSourceConfigs).entrySet().stream()
                .filter(e -> !disableDataSources.contains(e.getKey())).collect(toMap(e -> e.getKey(), e -> e.getValue()));

        // Load all rules.
        Collection<RuleConfiguration> allRuleConfigs = initializer.loadRuleConfigs(getSchemaName());

        List<ReadwriteSplittingRuleConfiguration> readwriteRuleConfigs = new ArrayList<>(4);
        List<RuleConfiguration> otherRuleConfigs = new ArrayList<>(4);
        for (RuleConfiguration ruleConfig : safeList(allRuleConfigs)) {
            if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
                readwriteRuleConfigs.add((ReadwriteSplittingRuleConfiguration) ruleConfig);
            } else {
                otherRuleConfigs.add(ruleConfig);
            }
        }

        return new SchemaConfigurationWrapper(allDataSourceConfigs, allRuleConfigs, readwriteRuleConfigs, otherRuleConfigs);
    }

    /**
     * Transform matching configuration dataSource name by host and port.
     * 
     * @param allDataSourceConfigs
     * @param initDefineRwDataSources
     * @param externalDbHost
     * @param externalDbPort
     * @return
     */
    private String findMatchingDataSourceName(Map<String, DataSourceConfiguration> allDataSourceConfigs,
            List<String> initDefineRwDataSources, String externalDbHost, int externalDbPort) {

        for (Entry<String, DataSourceConfiguration> ent : safeMap(allDataSourceConfigs).entrySet()) {
            String defineDataSourceName = ent.getKey();
            String jdbcUrl = valueOf(ent.getValue().getProps().get("jdbcUrl"));

            if (initDefineRwDataSources.contains(defineDataSourceName)) {
                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
                if (info.getPort() == externalDbPort && HostUtils.isSameHost(info.getHost(), externalDbHost)) {
                    return defineDataSourceName; // Define dataSource name.
                }
            }
        }

        return null;
    }

    /**
     * Do changing readWriteSplitting rule configuration.
     * 
     * @param newReadWriteSplittingRuleConfigs
     */
    private void doChangeReadwriteSplittingRuleConfiguration(List<RuleConfiguration> newReadWriteSplittingRuleConfigs) {
        log.info("\n");
        log.info("----------------------------------------------------------");
        log.warn("=>> FAILED, PERFORMING PRIMARY-STANDBY HANDOVER !!! <<=");
        log.info("=>> Changed new read-write-splitting rule configuration ... - {}",
                toJSONString(newReadWriteSplittingRuleConfigs));
        log.info("----------------------------------------------------------\n");
        initializer.updateSchemaRuleConfiguration(getSchemaName(), newReadWriteSplittingRuleConfigs);
    }

    /**
     * {@link SchemaConfigurationWrapper}
     * 
     * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2021-07-30 v1.0.0
     * @since v1.0.0
     */
    @Getter
    @AllArgsConstructor
    static class SchemaConfigurationWrapper {
        private Map<String, DataSourceConfiguration> allDataSourceConfigs;
        private Collection<RuleConfiguration> allRuleConfigs;
        private List<ReadwriteSplittingRuleConfiguration> readwriteRuleConfigs;
        private List<RuleConfiguration> otherRuleConfigs;
    }

    /**
     * {@link FailoverShutdownManager}
     * 
     * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2021-07-30 v1.0.0
     * @since v1.0.0
     */
    static class FailoverShutdownManager {
        private static final FailoverShutdownManager INSTANCE = new FailoverShutdownManager();
        private final SmartLogger log = getLogger(FailoverShutdownManager.class);
        private final List<AbstractProxyFailover<? extends NodeStats>> shutdownFailovers = new Vector<>(4);
        private Thread shutdownThread;

        public static FailoverShutdownManager getInstance() {
            return INSTANCE;
        }

        public void addShutdownHandler(AbstractProxyFailover<? extends NodeStats> failover) {
            shutdownFailovers.add(notNullOf(failover, "failover"));
            initIfNecessary();
        }

        /**
         * Initialization failovers to JVM shutdown handlers.
         */
        private synchronized void initIfNecessary() {
            if (isNull(shutdownThread)) {
                shutdownThread = new Thread(() -> {
                    safeList(shutdownFailovers).forEach(failover -> {
                        safeMap(failover.cachingAdminDataSources).forEach((dataSourceName, delegate) -> {
                            try {
                                log.info("Closing admin dataSource: {}, of '{}'", delegate, dataSourceName);
                                delegate.close();
                            } catch (Exception e) {
                                log.info("Failed to close admin dataSource: {}, of '{}'", delegate, dataSourceName);
                            }
                        });
                    });
                });
            }
        }

    }

}
