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

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;

import java.util.List;
import java.util.Vector;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import com.wl4g.component.common.lang.SimpleVersionComparator;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.mariadb.MariaDBProxyFailover;
import com.wl4g.component.integration.sharding.failover.mysql.MySQL57GroupReplicationProxyFailover;
import com.wl4g.component.integration.sharding.failover.oracle.OracleProxyFailover;
import com.wl4g.component.integration.sharding.failover.postgresql.PostgresqlProxyFailover;

/**
 * {@link ProxyFailoverManager}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public final class ProxyFailoverManager {
    private final SmartLogger log = getLogger(getClass());
    private final static List<ProxyFailover<? extends NodeStats>> failovers = new Vector<>(4);

    private ProxyFailoverManager() {
    }

    public ProxyFailoverManager init(FailoverBootstrapInitializer initializer) {
        notNullOf(initializer, "initializer");

        ProxyContext proxy = ProxyContext.getInstance();
        for (String schemaName : proxy.getAllSchemaNames()) {
            ShardingSphereMetaData metadata = proxy.getMetaData(schemaName);
            DatabaseType databaseType = metadata.getResource().getDatabaseType();
            CachedDatabaseMetaData cachedMetaData = metadata.getResource().getCachedDatabaseMetaData();
            if (databaseType instanceof MySQLDatabaseType) {
                checkMySQLVersionSupport(cachedMetaData);
                failovers.add(new MySQL57GroupReplicationProxyFailover(initializer, metadata));
            } else if (databaseType instanceof PostgreSQLDatabaseType) {
                failovers.add(new PostgresqlProxyFailover(initializer, metadata));
            } else if (databaseType instanceof OracleDatabaseType) {
                failovers.add(new OracleProxyFailover(initializer, metadata));
            } else if (databaseType instanceof MariaDBDatabaseType) {
                failovers.add(new MariaDBProxyFailover(initializer, metadata));
            } else {
                throw new UnsupportedOperationException(format("Not supported failover database type: %s", databaseType));
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopAll();
            }
        });

        return this;
    }

    @SuppressWarnings("unchecked")
    public void startAll() {
        for (ProxyFailover<? extends NodeStats> failover : failovers) {
            try {
                if (!((AbstractProxyFailover<NodeStats>) failover).isStarted()) {
                    failover.start();
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void stopAll() {
        for (ProxyFailover<? extends NodeStats> failover : failovers) {
            try {
                failover.close();
            } catch (Exception e) {
                log.error("Failed to close proxyFailover: {}", failover);
            }
        }
    }

    public static void checkMySQLVersionSupport(CachedDatabaseMetaData cachedMetaData) {
        if (SimpleVersionComparator.INSTANCE.compare(cachedMetaData.getDatabaseProductVersion(), "5.7") < 0) {
            throw new UnsupportedOperationException(
                    format("The supported version range must be greater than or equal to mysql 5.7"));
        }
    }

    public static ProxyFailoverManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        private static final ProxyFailoverManager INSTANCE = new ProxyFailoverManager();
    }

}
