package org.apache.shardingproxy.agent.core.plugin;
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

import static java.util.Arrays.asList;

import java.io.File;
import java.util.LinkedList;

import org.junit.Test;

/**
 * {@link ApmPluginLoaderTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-15 v1.0.0
 * @since v1.0.0
 */
public class ApmPluginLoaderTests {

    @Test
    public void testJVMClassLoaderOrder() {
        File[] jarFiles = { new File("ext-lib/agentlib/plugins/opentelemetry-exporter-otlp-trace-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient_hotspot-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-exporter-otlp-common-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/jaeger-client-0.31.0.jar"),
                new File("ext-lib/agentlib/plugins/shardingsphere-agent-tracing-opentelemetry-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/shardingsphere-agent-tracing-zipkin-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/shardingsphere-agent-metrics-api-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/zipkin-reporter-2.15.2.jar"),
                new File("ext-lib/agentlib/plugins/zipkin-reporter-brave-2.15.2.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-proto-1.3.0-alpha.jar"),
                new File("ext-lib/agentlib/plugins/shardingsphere-agent-metrics-prometheus-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/jaeger-tracerresolver-0.31.0.jar"),
                new File("ext-lib/agentlib/plugins/collector-0.16.1.jar"),
                new File("ext-lib/agentlib/plugins/zipkin-sender-okhttp3-2.15.2.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient_common-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/zipkin-2.21.7.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-exporter-jaeger-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-sdk-common-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-api-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-sdk-metrics-1.3.0-alpha.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-sdk-extension-autoconfigure-1.3.0-alpha.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient_httpserver-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/opentracing-util-0.31.0.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient_tracer_otel-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-context-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/jaeger-thrift-0.31.0.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient_tracer_common-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/shardingsphere-agent-tracing-jaeger-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/shardingsphere-agent-logging-base-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-exporter-zipkin-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-semconv-1.3.0-alpha.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-sdk-trace-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-api-metrics-1.3.0-alpha.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/opentelemetry-sdk-1.3.0.jar"),
                new File("ext-lib/agentlib/plugins/opentracing-api-0.31.0.jar"),
                new File("ext-lib/agentlib/plugins/xcloud-shardingproxy-agent-extension-5.1.0.jar"),
                new File("ext-lib/agentlib/plugins/opentracing-noop-0.31.0.jar"),
                new File("ext-lib/agentlib/plugins/simpleclient_tracer_otel_agent-0.11.0.jar"),
                new File("ext-lib/agentlib/plugins/jaeger-core-0.31.0.jar") };

        LinkedList<File> files = new LinkedList<>(asList(jarFiles));
        System.out.println(files.get(0));

        // case1(negative):
        // java.util.Collections.sort(files,(f1,f2)->1.getAbsolutePath().contains("agent-extension")?-1:0);

        // case2(positive):
        // String extension = files.stream().filter(f
        // ->f.contains("agent-extension")).findAny().get();
        // files.remove(extension);
        // files.offerFirst(extension);

        // case3(positive):
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getAbsolutePath().contains("agent-extension")) {
                File tmp = files.get(0);
                files.set(0, files.get(i));
                files.set(i, tmp);
                break;
            }
        }
        System.out.println(files);
    }

}
