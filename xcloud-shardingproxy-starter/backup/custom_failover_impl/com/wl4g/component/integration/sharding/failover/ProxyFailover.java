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

import java.io.Closeable;
import java.util.List;

import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link ProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public interface ProxyFailover<S extends NodeStats> extends Closeable {

    void start() throws Exception;

    S inspecting(JdbcOperator operator) throws Exception;

    @Getter
    @Setter
    public static abstract class NodeStats {
        public abstract List<? extends NodeInfo> getPrimaryNodes();

        public abstract List<? extends NodeInfo> getStandbyNodes();

        public abstract boolean valid();

        @Getter
        @Setter
        public static abstract class NodeInfo {
            public abstract String getNodeId();

            public abstract String getHost();

            public abstract int getPort();

            public String toAddressString() {
                return getHost() + ":" + getPort();
            }

            @Override
            public String toString() {
                return toAddressString();
            }

        }

    }

}
