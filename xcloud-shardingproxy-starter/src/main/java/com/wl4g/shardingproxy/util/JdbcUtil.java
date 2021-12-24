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

import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.notNull;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link JdbcUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-20 v1.0.0
 * @since v1.0.0
 */
public abstract class JdbcUtil {

    public static JdbcInformation resolve(String jdbcUrl) {
        // e.g:jdbc:mysql://127.0.0.1:3306/userdb_g1db0?serverTimezone=UTC&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8
        URI uri = URI.create(jdbcUrl.substring(jdbcUrl.indexOf("jdbc:") + 5));
        notNull(uri.getHost(), "Unable resolve jdbc url host from: %s", jdbcUrl);
        isTrue(uri.getPort() > 0, "Unable resolve jdbc url port from: %s", jdbcUrl);

        String dbname = uri.getPath();
        dbname = startsWith(dbname, "/") ? dbname.substring(1) : dbname;
        return new JdbcInformation("jdbc:".concat(uri.getScheme()), uri.getHost(), uri.getPort(), dbname);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class JdbcInformation {
        private String schema;
        private String host;
        private int port;
        private String database;
    }
}
