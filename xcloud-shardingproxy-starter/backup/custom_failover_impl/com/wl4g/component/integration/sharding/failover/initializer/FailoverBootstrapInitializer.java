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

package com.wl4g.component.integration.sharding.failover.initializer;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static java.util.stream.Collectors.toList;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.scaling.core.api.ScalingWorker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import com.wl4g.component.integration.sharding.failover.ProxyFailoverManager;
import com.wl4g.component.integration.sharding.failover.config.FailoverConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Support for failovered bootstrap initializer.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-11 v1.0.0
 * @since v1.0.0
 * @see {@link BootstrapInitializer}
 */
@RequiredArgsConstructor
@Slf4j
public final class FailoverBootstrapInitializer {

    private final ShardingSphereProxy shardingSphereProxy = new ShardingSphereProxy();
    private final RegistryCenter registryCenter;

    public FailoverBootstrapInitializer(final int port) {
        this.registryCenter = new RegistryCenter(null, port); // TODO
    }

    /**
     * Initialize.
     *
     * @param yamlConfig
     *            YAML proxy configuration
     * @param port
     *            proxy port
     * @throws SQLException
     *             SQL exception
     */
    public void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ModeConfiguration modeConfig = null == yamlConfig.getServerConfiguration().getMode() ? null
                : new ModeConfigurationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getMode());
        initContext(yamlConfig, modeConfig, port);
        setDatabaseServerInfo();
        initScaling(yamlConfig, modeConfig);
    }

    private void initContext(final YamlProxyConfiguration yamlConfig, final ModeConfiguration modeConfig, final int port)
            throws SQLException {
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        boolean isOverwrite = null == modeConfig || modeConfig.isOverwrite();
        Map<String, Map<String, DataSource>> dataSourcesMap = getDataSourcesMap(proxyConfig.getSchemaDataSources());
        ContextManager contextManager = ContextManagerBuilderFactory.newInstance(modeConfig).build(modeConfig, dataSourcesMap,
                proxyConfig.getSchemaRules(), proxyConfig.getGlobalRules(), proxyConfig.getProps(), isOverwrite, port);
        ProxyContext.getInstance().init(contextManager);

        //
        // ADD for failover.
        //
        ProxyFailoverManager.getInstance().init(this).startAll();
        shardingSphereProxy.start(port);

        // ADD merge failover configuration.
        FailoverConfiguration failoverConfig = FailoverConfiguration.build(yamlConfig.getServerConfiguration().getProps());
        ProxyContext.getInstance().getFailoverConfig().mergeFrom(failoverConfig);
    }

    // TODO add DataSourceParameter param to ContextManagerBuilder to avoid
    // re-build data source
    private Map<String, Map<String, DataSource>> getDataSourcesMap(
            final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourceParametersMap.size(), 1);
        for (Entry<String, Map<String, DataSourceParameter>> entry : dataSourceParametersMap.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter
                    .getDataSourceMap(DataSourceParameterConverter.getDataSourceConfigurationMap(entry.getValue())));
        }
        return result;
    }

    private void setDatabaseServerInfo() {
        findBackendDataSource().ifPresent(dataSourceSample -> {
            DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSourceSample);
            log.info(databaseServerInfo.toString());
            switch (databaseServerInfo.getDatabaseName()) {
            case "MySQL":
                MySQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
                break;
            case "PostgreSQL":
                PostgreSQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
                break;
            default:
            }
        });
    }

    private Optional<DataSource> findBackendDataSource() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<ShardingSphereMetaData> metaData = metaDataContexts.getMetaDataMap().values().stream()
                .filter(ShardingSphereMetaData::isComplete).findFirst();
        return metaData.flatMap(optional -> optional.getResource().getDataSources().values().stream().findFirst());
    }

    private void initScaling(final YamlProxyConfiguration yamlConfig, final ModeConfiguration modeConfig) {
        Optional<ServerConfiguration> scalingConfig = findScalingConfiguration(yamlConfig);
        if (!scalingConfig.isPresent()) {
            return;
        }
        // TODO decouple "Cluster" to pluggable
        if (null != modeConfig && "Cluster".equals(modeConfig.getType())) {
            scalingConfig.get().setModeConfiguration(modeConfig);
            ScalingContext.getInstance().init(scalingConfig.get());
            ScalingWorker.init();
        } else {
            ScalingContext.getInstance().init(scalingConfig.get());
        }
    }

    private Optional<ServerConfiguration> findScalingConfiguration(final YamlProxyConfiguration yamlConfig) {
        if (null == yamlConfig.getServerConfiguration().getScaling()) {
            return Optional.empty();
        }
        ServerConfiguration result = new ServerConfiguration();
        result.setBlockQueueSize(yamlConfig.getServerConfiguration().getScaling().getBlockQueueSize());
        result.setWorkerThread(yamlConfig.getServerConfiguration().getScaling().getWorkerThread());
        YamlShardingSphereAlgorithmConfiguration autoSwitchConfig = yamlConfig.getServerConfiguration().getScaling()
                .getClusterAutoSwitchAlgorithm();
        if (null != autoSwitchConfig) {
            result.setClusterAutoSwitchAlgorithm(
                    new ShardingSphereAlgorithmConfiguration(autoSwitchConfig.getType(), autoSwitchConfig.getProps()));
        }
        YamlShardingSphereAlgorithmConfiguration dataConsistencyCheckConfig = yamlConfig.getServerConfiguration().getScaling()
                .getDataConsistencyCheckAlgorithm();
        if (null != dataConsistencyCheckConfig) {
            result.setDataConsistencyCheckAlgorithm(new ShardingSphereAlgorithmConfiguration(dataConsistencyCheckConfig.getType(),
                    dataConsistencyCheckConfig.getProps()));
        }
        return Optional.of(result);
    }

    //
    // ADD for FAILOVER
    //

    public Map<String, DataSourceConfiguration> loadDataSourceConfigs(String schemaName) {
        return registryCenter.getRegistryCenter().getDataSourceService().load(schemaName);
    }

    public Collection<RuleConfiguration> loadRuleConfigs(String schemaName) {
        return registryCenter.getRegistryCenter().getSchemaRuleService().load(schemaName);
    }

    public Collection<String> loadDisableDataSources(String schemaName) {
        return registryCenter.getRegistryCenter().getDataSourceStatusService().loadDisabledDataSources(schemaName);
    }

    public synchronized void updateSchemaRuleConfiguration(String schemaName,
            Collection<? extends RuleConfiguration> schemaRuleConfigs) {
        SchemaRuleRegistryService schemaRuleService = registryCenter.getRegistryCenter().getSchemaRuleService();
        schemaRuleService.persist(schemaName, safeList(schemaRuleConfigs).stream().map(c -> c).collect(toList()));
    }

}
