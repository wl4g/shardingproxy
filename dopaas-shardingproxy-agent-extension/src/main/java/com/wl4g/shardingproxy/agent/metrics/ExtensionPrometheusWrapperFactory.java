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

import static com.wl4g.infra.common.reflect.ReflectionUtils2.findFieldNullable;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory;

/**
 * {@link ExtensionPrometheusWrapperFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-24 v1.0.0
 * @since v1.0.0
 */
public class ExtensionPrometheusWrapperFactory extends PrometheusWrapperFactory {

    public List<Map<String, Object>> getDefinitionMetrics() {
        return getField(METRIFS_FIELD, this, true);
    }

    private final static Field METRIFS_FIELD = findFieldNullable(PrometheusWrapperFactory.class, "metrics", List.class);

}
