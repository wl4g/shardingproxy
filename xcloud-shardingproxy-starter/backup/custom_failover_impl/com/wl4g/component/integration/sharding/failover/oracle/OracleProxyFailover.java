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
package com.wl4g.component.integration.sharding.failover.oracle;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import com.wl4g.component.integration.sharding.failover.AbstractProxyFailover;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.failover.oracle.stats.OracleNodeStats;
import com.zaxxer.hikari.HikariDataSource;

/**
 * {@link OracleProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-19 v1.0.0
 * @since v1.0.0
 */
public class OracleProxyFailover extends AbstractProxyFailover<OracleNodeStats> {

    public OracleProxyFailover(FailoverBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(initializer, metadata);
    }

    @Override
    public OracleNodeStats inspecting(JdbcOperator operator) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruldDataSourceJdbcPort, HikariDataSource adminDataSource) {
        throw new UnsupportedOperationException();
    }

}
