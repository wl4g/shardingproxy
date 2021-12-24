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
package com.wl4g.shardingproxy.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.bytebuddy.listener.LoggingListener;
import org.apache.shardingsphere.agent.core.bytebuddy.transformer.ShardingSphereTransformer;
import org.apache.shardingsphere.agent.core.config.loader.AgentConfigurationLoader;
import org.apache.shardingsphere.agent.core.config.registry.AgentConfigurationRegistry;
import org.apache.shardingsphere.agent.core.plugin.ApmPluginLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginBootServiceManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * {@link ShardingProxyAgent}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-24 v1.0.0
 * @since v1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShardingProxyAgent {

    /**
     * Premain for instrumentation.
     *
     * @param arguments
     *            arguments
     * @param instrumentation
     *            instrumentation
     * @throws IOException
     *             IO exception
     */
    public static void premain(final String arguments, final Instrumentation instrumentation) throws IOException {
        AgentConfiguration agentConfiguration = AgentConfigurationLoader.load();
        AgentConfigurationRegistry.INSTANCE.put(agentConfiguration);
        ApmPluginLoader loader = createPluginLoader();
        setUpAgentBuilder(instrumentation, loader);
        setupPluginBootService(agentConfiguration.getPlugins());
    }

    private static ApmPluginLoader createPluginLoader() throws IOException {
        ApmPluginLoader result = ApmPluginLoader.getInstance();
        result.loadAllPlugins();
        return result;
    }

    private static void setupPluginBootService(final Map<String, PluginConfiguration> pluginConfigurationMap) {
        PluginBootServiceManager.startAllServices(pluginConfigurationMap);
        Runtime.getRuntime().addShutdownHook(new Thread(PluginBootServiceManager::closeAllServices));
    }

    private static void setUpAgentBuilder(final Instrumentation instrumentation, final ApmPluginLoader loader) {
        AgentBuilder agentBuilder = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic()).or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent."));
        agentBuilder.type(loader.typeMatcher()).transform(new ShardingSphereTransformer(loader))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(new LoggingListener()).installOn(instrumentation);
    }

}
