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

package org.apache.shardingsphere.dbdiscovery.mgr;

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;

import com.wl4g.shardingproxy.dbdiscovery.ExtensionDiscoveryConfiguration;
import com.wl4g.shardingproxy.util.ConfigPropertySource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MGR data base discovery type.
 * 
 * </br>
 * Refer sources code:
 * 
 * @see {@link org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder#beforeBuildContextManager()}
 * @see {@link org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule#findMasterSlaveRelation(String,Map)}
 * @see {@link org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule#initHeartBeatJobs(String,Map)}
 * @see {@link org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryDataSourceRule#updatePrimaryDataSourceName(String)}
 * @see {@link org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType#updatePrimaryDataSource}
 * @see {@link org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder#afterBuildContextManager()}
 * @see {@link org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder#disableDataSources()}
 * @see {@link org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule#updateStatus(DataSourceStatusChangedEvent)}
 * @see {@link org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryDataSourceRule#disableDataSource(String)}
 * @see {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator}
 */
@Slf4j
public final class MGRDatabaseDiscoveryType implements DatabaseDiscoveryType {

    private static final String PLUGIN_STATUS = "SELECT * FROM information_schema.PLUGINS WHERE PLUGIN_NAME='group_replication'";
    private static final String MEMBER_COUNT = "SELECT count(*) FROM performance_schema.replication_group_members";
    private static final String GROUP_NAME = "SELECT * FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_group_name'";
    private static final String SINGLE_PRIMARY = "SELECT * FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_single_primary_mode'";
    private static final String MEMBER_LIST = "SELECT MEMBER_HOST, MEMBER_PORT, MEMBER_STATE FROM performance_schema.replication_group_members";

    private String oldPrimaryDataSource;

    //
    // [FEATURE for ADD advance property source]
    //
    @Getter
    @Setter
    private Properties props = new ConfigPropertySource();

    //
    // [FEATURE for ADD extension configuration]
    //
    private ExtensionDiscoveryConfiguration extDiscoveryConfig;

    @Override
    public void checkDatabaseDiscoveryConfiguration(String schemaName, Map<String, DataSource> dataSourceMap)
            throws SQLException {
        try (Connection connection = dataSourceMap.get(oldPrimaryDataSource).getConnection();
                Statement statement = connection.createStatement()) {
            checkPluginIsActive(statement);
            checkMemberCount(statement);
            checkServerGroupName(statement);
            checkIsSinglePrimaryMode(statement);
        }
    }

    private void checkPluginIsActive(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(PLUGIN_STATUS)) {
            while (resultSet.next()) {
                if (!"ACTIVE".equals(resultSet.getString("PLUGIN_STATUS"))) {
                    throw new ShardingSphereConfigurationException("MGR plugin is not active.");
                }
            }
        }
    }

    private void checkMemberCount(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(MEMBER_COUNT)) {
            while (resultSet.next()) {
                if (resultSet.getInt(1) < 1) {
                    throw new ShardingSphereConfigurationException("MGR member count < 1");
                }
            }
        }
    }

    private void checkServerGroupName(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(GROUP_NAME)) {
            while (resultSet.next()) {
                String serverGroupName = resultSet.getString("VARIABLE_VALUE");
                String ruleGroupName = props.getProperty("group-name");
                if (!serverGroupName.equals(ruleGroupName)) {
                    throw new ShardingSphereConfigurationException(
                            "MGR group name is not consistent\n" + "serverGroupName: %s\nruleGroupName: %s", serverGroupName,
                            ruleGroupName);
                }
            }
        }
    }

    private void checkIsSinglePrimaryMode(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SINGLE_PRIMARY)) {
            while (resultSet.next()) {
                if (!"ON".equals(resultSet.getString("VARIABLE_VALUE"))) {
                    throw new ShardingSphereConfigurationException("MGR is not in single primary mode");
                }
            }
        }
    }

    @Override
    public void updatePrimaryDataSource(final String schemaName, final Map<String, DataSource> allDataSourceMap,
            final Collection<String> disabledDataSourceNames, final String groupName) {
        Map<String, DataSource> activeDataSourceMap = new HashMap<>(allDataSourceMap);
        if (!disabledDataSourceNames.isEmpty()) {
            activeDataSourceMap.entrySet().removeIf(each -> disabledDataSourceNames.contains(each.getKey()));
        }
        String newPrimaryDataSource = determinePrimaryDataSource(activeDataSourceMap);
        if (newPrimaryDataSource.isEmpty()) {
            return;
        }
        if (!newPrimaryDataSource.equals(oldPrimaryDataSource)) {
            oldPrimaryDataSource = newPrimaryDataSource;
            //
            // [ADD description]
            //
            // Refer source code:
            // org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator
            // org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher.StorageNodeStateChangedWatcher#createGovernanceEvent()
            // org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory#watch()
            ShardingSphereEventBus.getInstance()
                    .post(new PrimaryDataSourceChangedEvent(schemaName, groupName, newPrimaryDataSource));
        }
    }

    private String determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        String primaryDataSourceURL = findPrimaryDataSourceURL(dataSourceMap);
        return findPrimaryDataSourceName(primaryDataSourceURL, dataSourceMap);
    }

    private String findPrimaryDataSourceURL(final Map<String, DataSource> dataSourceMap) {
        //
        // [Old logic.]
        //
        // String result = "";
        // String sql = "SELECT MEMBER_HOST, MEMBER_PORT FROM
        // performance_schema.replication_group_members WHERE MEMBER_ID = "
        // + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE
        // VARIABLE_NAME = 'group_replication_primary_member')";
        // for (DataSource each : dataSourceMap.values()) {
        // try (Connection connection = each.getConnection();
        // Statement statement = connection.createStatement();
        // ResultSet resultSet = statement.executeQuery(sql)) {
        // if (resultSet.next()) {
        // return String.format("%s:%s", resultSet.getString("MEMBER_HOST"),
        // resultSet.getString("MEMBER_PORT"));
        // }
        // } catch (final SQLException ex) {
        // log.error("An exception occurred while find primary data source url",
        // ex);
        // }
        // }
        // return result;

        //
        // [BUGFIX for ADD check for MGR member(host/port)]
        //
        String result = "";
        String sql = "SELECT MEMBER_HOST, MEMBER_PORT FROM performance_schema.replication_group_members WHERE MEMBER_ID = "
                + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'group_replication_primary_member')";
        for (DataSource each : dataSourceMap.values()) {
            try (Connection connection = each.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                if (resultSet.next()) {
                    // [FIX]: If the currently connected node, for example due
                    // to the previous OS downtime, causes mysqld to restart,
                    // and then has not had time to join the MGR (at this time
                    // the main member has moved to other nodes), the number of
                    // records returned by this SQL is 0 at this time, if not
                    // check empty will lead to logical errors later.
                    String host = resultSet.getString("MEMBER_HOST");
                    String port = resultSet.getString("MEMBER_PORT");
                    if (!(isAnyBlank(host, port) || equalsAnyIgnoreCase("null", host, port))) {
                        return String.format("%s:%s", host, port);
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return result;
    }

    private String findPrimaryDataSourceName(final String primaryDataSourceURL, final Map<String, DataSource> dataSourceMap) {
        //
        // [Old logic.]
        //
        // String result = "";
        // for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
        // String url;
        // try (Connection connection = entry.getValue().getConnection()) {
        // url = connection.getMetaData().getURL();
        // if (null != url && url.contains(primaryDataSourceURL)) {
        // return entry.getKey();
        // }
        // } catch (final SQLException ex) {
        // log.error("An exception occurred while find primary data source
        // name", ex);
        // }
        // }
        // return result;

        //
        // [FEATURE for ADD DataSource URL addresses mapping matches]
        //
        // if (isEmpty(dataSourceMap)) {
        // throw new IllegalStateException("The primary dataSource cannot be
        // found because the datasourceMap is empty.");
        // }
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            String url;
            try (Connection connection = entry.getValue().getConnection()) {
                url = connection.getMetaData().getURL();
                if (null != url) {
                    if (url.contains(primaryDataSourceURL)
                            || ExtensionDiscoveryConfiguration.Util.matchs(getExtDiscoveryConfig(), url, primaryDataSourceURL)) {
                        return entry.getKey();
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source name", ex);
            }
        }
        log.warn(
                "The datasource name is not matched when the database is discovered, or please check the extension configuration of 'memberHostMappings' is correct. - {}",
                toJSONString(getExtDiscoveryConfig()));
        return EMPTY;
    }

    @Override
    public void updateMemberState(final String schemaName, final Map<String, DataSource> allDataSourceMap,
            final Collection<String> disabledDataSourceNames) {
        //
        // [Old logic.]
        //
        // Map<String, DataSource> activeDataSourceMap = new
        // HashMap<>(allDataSourceMap);
        // if (!disabledDataSourceNames.isEmpty()) {
        // activeDataSourceMap.entrySet().removeIf(each ->
        // disabledDataSourceNames.contains(each.getKey()));
        // }
        // List<String> memberDataSourceURLs =
        // findMemberDataSourceURLs(activeDataSourceMap);
        // if (memberDataSourceURLs.isEmpty()) {
        // return;
        // }
        // Map<String, String> dataSourceURLs = new HashMap<>(16, 1);
        // determineDisabledDataSource(schemaName,activeDataSourceMap,memberDataSourceURLs,dataSourceURLs);
        // determineEnabledDataSource(dataSourceMap,schemaName,memberDataSourceURLs,dataSourceURLs);

        //
        // [FEATURE for ADD members dataSources URL to mapped addresses]
        //
        List<String> memberDataSourceURLs = findMemberDataSourceURLs(allDataSourceMap);
        if (memberDataSourceURLs.isEmpty()) {
            return;
        }
        // Merge to mapped member URLs.
        List<String> mergedMemberDataSourceURLs = mergeMappedMemberDataSourceURLs(memberDataSourceURLs);
        Map<String, String> dataSourceURLs = new HashMap<>(16, 1);
        determineDisabledDataSource(schemaName, allDataSourceMap, mergedMemberDataSourceURLs, dataSourceURLs);
        determineEnabledDataSource(allDataSourceMap, schemaName, mergedMemberDataSourceURLs, dataSourceURLs);
    }

    //
    // [FEATURE for ADD merge mapped members dataSources URL to mapped
    // addresses]
    //
    private List<String> mergeMappedMemberDataSourceURLs(List<String> memberDataSourceURLs) {
        List<String> result = new LinkedList<>();
        for (String url : memberDataSourceURLs) {
            List<String> mappedDataSourceURLs = ExtensionDiscoveryConfiguration.Util
                    .findMappingAddressesByMemberAddr(getExtDiscoveryConfig(), url);
            result.addAll(mappedDataSourceURLs);
            result.add(url);
        }
        return result;
    }

    private List<String> findMemberDataSourceURLs(final Map<String, DataSource> activeDataSourceMap) {
        List<String> result = new LinkedList<>();
        try (Connection connection = activeDataSourceMap.get(oldPrimaryDataSource).getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(MEMBER_LIST);
            while (resultSet.next()) {
                if (!"ONLINE".equals(resultSet.getString("MEMBER_STATE"))) {
                    continue;
                }
                result.add(String.format("%s:%s", resultSet.getString("MEMBER_HOST"), resultSet.getString("MEMBER_PORT")));
            }
        } catch (final SQLException ex) {
            log.error("An exception occurred while find member data source urls", ex);
        }
        return result;
    }

    private void determineDisabledDataSource(final String schemaName, final Map<String, DataSource> allDataSourceMap,
            final List<String> memberDataSourceURLs, final Map<String, String> dataSourceURLs) {
        for (Entry<String, DataSource> entry : allDataSourceMap.entrySet()) {
            boolean disable = true;
            String url = null;
            try (Connection connection = entry.getValue().getConnection()) {
                url = connection.getMetaData().getURL();
                for (String each : memberDataSourceURLs) {
                    //
                    // [BUGFIX for ADD matches mapped URL]
                    //
                    if (null != url && (url.contains(each)
                            || ExtensionDiscoveryConfiguration.Util.matchs(getExtDiscoveryConfig(), url, each))) {
                        disable = false;
                        break;
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find data source urls", ex);
            }
            if (disable) {
                ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, entry.getKey(), true));
            } else if (!url.isEmpty()) {
                dataSourceURLs.put(entry.getKey(), url);
            }
        }
    }

    private void determineEnabledDataSource(final Map<String, DataSource> allDataSourceMap, final String schemaName,
            final List<String> memberDataSourceURLs, final Map<String, String> enabledDataSourceURLs) {
        //
        // [Old logic.]
        //
        // for (String each : memberDataSourceURLs) {
        // boolean enable = true;
        // for (Entry<String, String> entry : enabledDataSourceURLs.entrySet())
        // {
        // if (entry.getValue().contains(each)) {
        // enable = false;
        // break;
        // }
        // }
        // if (!enable) {
        // continue;
        // }
        // for (Entry<String, DataSource> entry : allDataSourceMap.entrySet()) {
        // String url;
        // try (Connection connection = entry.getValue().getConnection()) {
        // url = connection.getMetaData().getURL();
        // if (null != url && url.contains(each)) {
        // ShardingSphereEventBus.getInstance().post(new
        // DataSourceDisabledEvent(schemaName, entry.getKey(), false));
        // break;
        // }
        // } catch (final SQLException ex) {
        // log.error("An exception occurred while find enable data source urls",
        // ex);
        // }
        // }
        // }

        //
        // [BUGFIX As long as it does not mean disabled, it means enabled]
        //
        for (Entry<String, String> entry : enabledDataSourceURLs.entrySet()) {
            ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, entry.getKey(), false));
        }
    }

    @Override
    public String getPrimaryDataSource() {
        return oldPrimaryDataSource;
    }

    @Override
    public String getType() {
        return "MGR";
    }

    //
    // [FEATURE for ADD EXTENSION property]
    //
    private ExtensionDiscoveryConfiguration getExtDiscoveryConfig() {
        if (isNull(extDiscoveryConfig)) {
            synchronized (this) {
                if (isNull(extDiscoveryConfig)) {
                    this.extDiscoveryConfig = ExtensionDiscoveryConfiguration.Util
                            .build(props.getProperty(ExtensionDiscoveryConfiguration.PROPS_KEY));
                }
            }
        }
        return extDiscoveryConfig;
    }

}
