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
package com.wl4g.shardingproxy.agent.metrics;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.join;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;

/**
 * {@link ExtensionMetricsCollectorTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-07 v1.0.0
 * @since v1.0.0
 */
public class ExtensionMetricsCollectorTests {

    public static void main(String[] args) throws Exception {
        // testPrimaryDataSourceChanged();
        System.out.println(InetAddress.getLocalHost().getHostName());
    }

    public static void testPrimaryDataSourceChanged() {
        LinkedList<PrimaryDataSourceChangedEvent> queue = new LinkedList<>();
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        // FailoverChanged 1
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_1"));
        // FailoverChanged 2
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_2"));
        // FailoverChanged 3
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        // FailoverChanged 4
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_1"));
        // FailoverChanged 5
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));
        queue.add(new PrimaryDataSourceChangedEvent("userdb", "ha_userdb_g0db0", "userdb_g0db0_0"));

        LinkedHashMap<String, List<PrimaryDataSourceChangedEvent>> groupMap = queue.stream().collect(
                groupingBy(event -> join(event.getGroupName(), "@", event.getSchemaName()), LinkedHashMap::new, toList()));

        Optional<MetricsWrapper> counter = new ExtensionPrometheusWrapperFactory()
                .create(MetricIds.EXT_DB_DISCOVERY_PRIMARY_DS_CHANGED_COUNTER);
        counter.ifPresent(m -> {
            for (String key : groupMap.keySet()) {
                List<PrimaryDataSourceChangedEvent> events = groupMap.getOrDefault(key, emptyList());
                for (int i = 1, end = events.size() - 1; i < end; i++) {
                    PrimaryDataSourceChangedEvent last = events.get(i - 1);
                    PrimaryDataSourceChangedEvent event = events.get(i);
                    if (!equalsAnyIgnoreCase(last.getDataSourceName(), event.getDataSourceName())) {
                        m.inc(1, event.getSchemaName(), event.getGroupName());
                    }
                }
            }
        });

        System.out.println(counter.get());
    }

}
