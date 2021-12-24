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
package com.wl4g.component.integration.sharding.failover.postgresql;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import com.wl4g.component.integration.sharding.failover.AbstractProxyFailover;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.failover.postgresql.stats.PostgresqlNodeStats;
import com.zaxxer.hikari.HikariDataSource;

/**
 * {@link PostgresqlProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public class PostgresqlProxyFailover extends AbstractProxyFailover<PostgresqlNodeStats> {

    public PostgresqlProxyFailover(FailoverBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(initializer, metadata);
    }

    @Override
    public PostgresqlNodeStats inspecting(JdbcOperator operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruldDataSourceJdbcPort, HikariDataSource adminDataSource) {
        // TODO Auto-generated method stub

    }
}
