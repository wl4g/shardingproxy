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

package com.wl4g.shardingproxy.plugins.service;

import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

import com.wl4g.shardingproxy.plugins.metrics.DbDiscoveryEventMetricsCollector;

import lombok.extern.slf4j.Slf4j;

/**
 * Extension plugin boot service.
 */
@Slf4j
public final class ExtensionPluginBootService implements PluginBootService {

    @Override
    public void start(final PluginConfiguration pluginConfiguration) {
        log.info("Starting extension prometheus plugin ...");
        registerCollectors();
    }

    @Override
    public void close() {
    }

    @Override
    public String getType() {
        return "ExtensionPrometheus";
    }

    private void registerCollectors() {
        new DbDiscoveryEventMetricsCollector().register();
    }

}
