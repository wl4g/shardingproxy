/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.metrics.prometheus.service;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.exception.PluginConfigurationException;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.BuildInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.MetaDataInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.collector.ProxyInfoCollector;
import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

import com.wl4g.shardingproxy.agent.metrics.ExtensionMetricsCollector;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Prometheus plugin boot service.
 */
@Slf4j
public final class PrometheusPluginBootService implements PluginBootService {

    private HTTPServer httpServer;

    @Override
    public void start(final PluginConfiguration pluginConfiguration) {
        if (!checkConfig(pluginConfiguration)) {
            throw new PluginConfigurationException("prometheus config error, host is null or port is %s",
                    pluginConfiguration.getPort());
        }
        startServer(pluginConfiguration);
        MetricsPool.setMetricsFactory(new PrometheusWrapperFactory());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close() {
        if (null != httpServer) {
            httpServer.stop();
        }
    }

    @Override
    public String getType() {
        return "Prometheus";
    }

    private boolean checkConfig(final PluginConfiguration pluginConfiguration) {
        return pluginConfiguration.getPort() > 0;
    }

    private void startServer(final PluginConfiguration configuration) {
        boolean enabled = Boolean.parseBoolean(configuration.getProps().getProperty("JVM_INFORMATION_COLLECTOR_ENABLED"));
        registerDefault();
        registerJvm(enabled);
        int port = configuration.getPort();
        String host = configuration.getHost();
        InetSocketAddress inetSocketAddress;
        if (null == host || "".equalsIgnoreCase(host)) {
            inetSocketAddress = new InetSocketAddress(port);
        } else {
            inetSocketAddress = new InetSocketAddress(host, port);
        }
        try {
            httpServer = new HTTPServer(inetSocketAddress, CollectorRegistry.defaultRegistry, true);
            log.info("Prometheus metrics HTTP server `{}:{}` start success", inetSocketAddress.getHostString(),
                    inetSocketAddress.getPort());
        } catch (final IOException ex) {
            log.error("Prometheus metrics HTTP server start fail", ex);
        }
    }

    private void registerDefault() {
        new ProxyInfoCollector().register();
        new BuildInfoCollector().register();
        new MetaDataInfoCollector().register();

        //
        // [FEATURE for ADD DB-discovery metrics collector]
        //
        new ExtensionMetricsCollector().register();
    }

    private void registerJvm(final boolean enabled) {
        if (enabled) {
            DefaultExports.initialize();
        }
    }
}
