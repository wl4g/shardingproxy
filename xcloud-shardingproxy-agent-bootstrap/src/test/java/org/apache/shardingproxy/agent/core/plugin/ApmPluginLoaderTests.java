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

import java.util.LinkedList;

/**
 * {@link ApmPluginLoaderTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-15 v1.0.0
 * @since v1.0.0
 */
public class ApmPluginLoaderTests {

    public static void main(String[] args) {
        String[] jars = { "opentelemetry-exporter-otlp-trace-1.3.0.jar", "simpleclient_hotspot-0.11.0.jar",
                "opentelemetry-exporter-otlp-common-1.3.0.jar", "jaeger-client-0.31.0.jar",
                "shardingsphere-agent-tracing-opentelemetry-5.1.0.jar", "shardingsphere-agent-tracing-zipkin-5.1.0.jar",
                "shardingsphere-agent-metrics-api-5.1.0.jar", "zipkin-reporter-2.15.2.jar", "zipkin-reporter-brave-2.15.2.jar",
                "opentelemetry-proto-1.3.0-alpha.jar", "shardingsphere-agent-metrics-prometheus-5.1.0.jar",
                "jaeger-tracerresolver-0.31.0.jar", "collector-0.16.1.jar", "zipkin-sender-okhttp3-2.15.2.jar",
                "simpleclient_common-0.11.0.jar", "zipkin-2.21.7.jar", "opentelemetry-exporter-jaeger-1.3.0.jar",
                "opentelemetry-sdk-common-1.3.0.jar", "opentelemetry-api-1.3.0.jar", "opentelemetry-sdk-metrics-1.3.0-alpha.jar",
                "opentelemetry-sdk-extension-autoconfigure-1.3.0-alpha.jar", "simpleclient_httpserver-0.11.0.jar",
                "opentracing-util-0.31.0.jar", "simpleclient_tracer_otel-0.11.0.jar", "opentelemetry-context-1.3.0.jar",
                "jaeger-thrift-0.31.0.jar", "simpleclient_tracer_common-0.11.0.jar",
                "shardingsphere-agent-tracing-jaeger-5.1.0.jar", "shardingsphere-agent-logging-base-5.1.0.jar",
                "opentelemetry-exporter-zipkin-1.3.0.jar", "opentelemetry-semconv-1.3.0-alpha.jar",
                "opentelemetry-sdk-trace-1.3.0.jar", "opentelemetry-api-metrics-1.3.0-alpha.jar", "simpleclient-0.11.0.jar",
                "opentelemetry-sdk-1.3.0.jar", "opentracing-api-0.31.0.jar", "xcloud-shardingproxy-agent-extension-5.1.0.jar",
                "opentracing-noop-0.31.0.jar", "simpleclient_tracer_otel_agent-0.11.0.jar", "jaeger-core-0.31.0.jar" };

        LinkedList<String> files = new LinkedList<>(asList(jars));
        System.out.println(files.get(0));
        String extension = files.stream().filter(f -> f.contains("agent-extension")).findAny().get();
        files.remove(extension);
        files.offerFirst(extension);
        // Collections.sort(files,(f1,f2)->(f1.contains("agent-extension"))?-1:0);
        System.out.println(files.get(0));
    }

}
