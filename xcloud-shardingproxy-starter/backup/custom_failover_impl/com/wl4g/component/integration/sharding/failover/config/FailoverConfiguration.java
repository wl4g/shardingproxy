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
package com.wl4g.component.integration.sharding.failover.config;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.synchronizedList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.net.HostAndPort;
import com.wl4g.component.common.lang.HostUtils;
import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats.NodeInfo;
import com.wl4g.component.integration.sharding.failover.exception.InvalidStateFailoverException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link FailoverConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-26 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
public class FailoverConfiguration {
    private boolean enable = true;
    private long inspectInitialDelayMs = 3_000L;
    private long inspectMinDelayMs = 10_000L;
    private long inspectMaxDelayMs = 30_000L;
    private List<FailoverAdminDataSourceConfig> adminDataSources = synchronizedList(new ArrayList<>());

    public FailoverConfiguration mergeFrom(FailoverConfiguration config) {
        try {
            BeanUtilsBean2.getInstance().copyProperties(this, config);
            return this;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public FailoverAdminDataSourceConfig getAdminDataSourceConfig(String schemaName) {
        return safeList(getAdminDataSources()).stream().filter(c -> StringUtils2.equals(c.getSchemaName(), schemaName))
                .findFirst().orElse(null);
    }

    public static FailoverConfiguration build(Properties props) {
        FailoverConfiguration config = parseJSON(valueOf(safeMap(props).get(KEY_FAILOVER_CONF_JSON)),
                FailoverConfiguration.class);
        safeList(config.getAdminDataSources()).forEach(c -> {
            // Check schemaName.
            if (!ProxyContext.getInstance().getAllSchemaNames().contains(c.getSchemaName())) {
                throw new InvalidStateFailoverException(
                        format("Invalid failover configuration. unknown schemaName: %s", c.getSchemaName()), null);
            }
            // Parse DB internal/external address.
            safeList(c.getMappings()).forEach(m -> m.parse());
        });
        return config;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public final static class FailoverAdminDataSourceConfig {
        private String schemaName;
        private String username = "root";
        private String password;
        private long connectionTimeout = 6_000L;
        private int maximumPoolSize = 1;
        private int minimumIdle = 1;
        private long idleTimeout = 0L;
        private long maxLifetime = 180_000L;
        private List<DataSourceAddressMapping> mappings = new ArrayList<>();

        public DataSourceAddressMapping getMappedByInternalAddress(NodeInfo node) {
            for (DataSourceAddressMapping mapping : safeList(getMappings())) {
                HostAndPort internal = mapping.getParsedInternalAddr();
                if (internal.getPort() == node.getPort() && HostUtils.isSameHost(internal.getHost(), node.getHost())) {
                    return mapping;
                }
            }
            return null;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public final static class DataSourceAddressMapping {
            private String internalAddr;
            private List<String> externalAddrs = new ArrayList<>();

            // Parsed hostAndPort string.
            private @JsonIgnore transient HostAndPort parsedInternalAddr;
            private @JsonIgnore transient List<HostAndPort> parsedExternalAddrs = new ArrayList<>();

            void parse() {
                this.parsedInternalAddr = HostAndPort.fromString(getInternalAddr());
                safeList(getExternalAddrs()).forEach(addr -> this.parsedExternalAddrs.add(HostAndPort.fromString(addr)));
            }
        }
    }

    private static final String KEY_FAILOVER_CONF_JSON = "failover-configuration-json"; // failover-admin-dataSource-configuration
}
