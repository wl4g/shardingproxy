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
package com.wl4g.shardingproxy.util;

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.StringUtils2.startsWithIgnoreCase;
import static com.wl4g.infra.common.lang.TypeConverts.parseIntOrDefault;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link JdbcUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-20 v1.0.0
 * @since v1.0.0
 */
public abstract class JdbcUtil {

    private static final String JDBC_PREFIX = "jdbc:";
    private static final String URL_SEPAR_SLASH2 = "://";

    public static JdbcInformation resolve(String jdbcUrl) {
        // e.g:jdbc:mysql://127.0.0.1:3306/userdb_g0db0?serverTimezone=UTC&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8
        if (startsWithIgnoreCase(jdbcUrl, JDBC_PREFIX)) {
            jdbcUrl = jdbcUrl.substring(jdbcUrl.indexOf(JDBC_PREFIX) + JDBC_PREFIX.length());
        }

        // e.g:jdbc:mysql://127.0.0.1:3306/userdb_g0db0?serverTimezone=UTC&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8
        if (containsIgnoreCase(jdbcUrl, URL_SEPAR_SLASH2)) {
            URI uri = URI.create(jdbcUrl);
            notNull(uri.getHost(), "Unable resolve jdbc url host from: %s", jdbcUrl);
            isTrue(uri.getPort() > 0, "Unable resolve jdbc url port from: %s", jdbcUrl);
            String databaseName = uri.getPath();
            databaseName = startsWith(databaseName, "/") ? databaseName.substring(1) : databaseName;
            return new JdbcInformation(jdbcUrl, JDBC_PREFIX.concat(uri.getScheme()), uri.getHost(), uri.getPort(), databaseName);
        }

        // e.g: 127.0.0.1:3306
        String[] parts = split(jdbcUrl, ":");
        if (nonNull(parts) && parts.length == 2) {
            return new JdbcInformation(jdbcUrl, null, parts[0], parseIntOrDefault(parts[1]), null);
        }

        return new JdbcInformation(jdbcUrl, null, null, 0, null);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static final class JdbcInformation {
        private String raw;
        private String schema;
        private String host;
        private int port;
        private String database;
    }

}
