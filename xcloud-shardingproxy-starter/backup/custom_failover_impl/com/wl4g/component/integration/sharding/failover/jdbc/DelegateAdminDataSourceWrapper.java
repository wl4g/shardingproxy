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
package com.wl4g.component.integration.sharding.failover.jdbc;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.sharding.failover.exception.FailoverException;
import com.wl4g.component.integration.sharding.failover.exception.InvalidStateFailoverException;
import com.wl4g.component.integration.sharding.failover.exception.NoNextAdminDataSourceFailoverException;
import com.zaxxer.hikari.HikariDataSource;

/**
 * {@link DelegateAdminDataSourceWrapper}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-29 v1.0.0
 * @since v1.0.0
 */
public final class DelegateAdminDataSourceWrapper implements Iterator<HikariDataSource>, Closeable {
    protected final SmartLogger log = getLogger(getClass());

    private final TreeMap<String, HikariDataSource> dataSources = new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    });

    private final AtomicInteger selectionPos = new AtomicInteger(DEFAULT_SELECTION_POS);
    private volatile HikariDataSource selection;

    @Override
    public boolean hasNext() {
        return (selectionPos.get() + 1) < dataSources.size();
    }

    @Override
    public synchronized HikariDataSource next() {
        if (!hasNext()) {
            throw new NoNextAdminDataSourceFailoverException(format(
                    "There are no more data sources. attempted: %s, all dataSources: %s", selectionPos.get(), dataSources));
        }
        selectionPos.incrementAndGet(); // +1

        int index = 0;
        for (String dsname : dataSources.keySet()) {
            if (index++ == selectionPos.get()) {
                return (selection = dataSources.get(dsname));
            }
        }

        throw new FailoverException(
                format("Failed to get data sources. attempted: %s, all dataSources: %s", selectionPos.get(), dataSources));
    }

    public boolean available() {
        return !dataSources.isEmpty();
    }

    public DataSource get() {
        if (isNull(selection)) {
            next();
        }
        if (isNull(selection)) {
            throw new InvalidStateFailoverException("There are currently no available data sources selected.");
        }
        return selection;
    }

    public DelegateAdminDataSourceWrapper reset() {
        selectionPos.set(DEFAULT_SELECTION_POS);
        return this;
    }

    public synchronized DelegateAdminDataSourceWrapper putDataSource(String dataSourceName, HikariDataSource dataSource) {
        dataSources.put(notNullOf(dataSourceName, "dataSourceName"), notNullOf(dataSource, "dataSource"));
        return this;
    }

    @Override
    public void close() throws IOException {
        dataSources.forEach((dsname, ds) -> {
            try {
                ds.close();
            } catch (Exception e) {
                log.error(format("Cannot close original dataSource. - %s", ds.getJdbcUrl()), e);
            }
        });
    }

    private static final int DEFAULT_SELECTION_POS = 0;
}
