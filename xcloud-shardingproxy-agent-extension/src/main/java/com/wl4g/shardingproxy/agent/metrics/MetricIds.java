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

/**
 * {@link MetricIds}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-23 v1.0.0
 * @since v1.0.0
 * @see {@link org.apache.shardingsphere.agent.metrics.api.constant.MetricIds}
 * @see {@link org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory#parseMetricsYaml()}
 * @see ./resources/prometheus/metrics.yaml
 */
public class MetricIds {

    public static final String DB_DISCOVERY_PRIMARY_DS = "dbdiscoveryPrimaryDatasource";

    public static final String DB_DISCOVERY_DISABLE_DS = "dbdiscoveryDisableDatasource";

    public static final String DB_DISCOVERY_DS = "dbdiscoveryDatasource";

}
