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

package org.apache.shardingsphere.agent.metrics.prometheus.wrapper;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapperFactory;
import org.yaml.snakeyaml.Yaml;

import com.wl4g.component.common.resource.StreamResource;
import com.wl4g.component.common.resource.resolver.ClassPathResourcePatternResolver;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import lombok.extern.slf4j.Slf4j;

/**
 * Prometheus metrics wrapper factory.
 */
@Slf4j
public class PrometheusWrapperFactory implements MetricsWrapperFactory {

    private static List<Map<String, Object>> metrics;

    static {
        parseMetricsYaml();
    }

    private static void parseMetricsYaml() {
        //
        // [Old logic.]
        //
        // Yaml yaml = new Yaml();
        // InputStream
        // in=PrometheusWrapperFactory.class.getResourceAsStream("/prometheus/metrics.yaml");
        // Map<String,List<Map<String,Object>>>metricsMap=yaml.loadAs(in,LinkedHashMap.class);
        // metrics = metricsMap.get("metrics");

        //
        // [FEATURE for ADD classPath scanning and merge metrics configuration]
        //
        metrics = loadAndMergeMetricsConfiguration();
    }

    //
    // [FEATURE for ADD classPath scanning and merge metrics configuration]
    //
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> loadAndMergeMetricsConfiguration() {
        List<Map<String, Object>> mergedMetrics = new ArrayList<>(32);
        try {
            Yaml yaml = new Yaml();
            Set<StreamResource> res = new ClassPathResourcePatternResolver().getResources("classpath*:/prometheus/metrics.yaml");
            log.info("Loading prometheus metrics for - {}", res);
            for (StreamResource r : res) {
                Map<String, List<Map<String, Object>>> map = yaml.loadAs(r.getInputStream(), LinkedHashMap.class);
                mergedMetrics.addAll(map.getOrDefault("metrics", emptyList()));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return mergedMetrics;
    }

    /**
     * Create the metrics wrapper.
     *
     * @param id
     *            id
     * @return optional of metrics wrapper
     */
    @Override
    public Optional<MetricsWrapper> create(final String id) {
        return createById(id);
    }

    /**
     * Create the gauge metric family.
     *
     * @param id
     *            string
     * @return the optional of gauge metric family
     */
    public Optional<GaugeMetricFamily> createGaugeMetricFamily(final String id) {
        Optional<Map<String, Object>> metricMap = findById(id);
        if (!metricMap.isPresent()) {
            return Optional.empty();
        }
        Map<String, Object> metric = metricMap.get();
        if (null == getType(metric)) {
            return Optional.empty();
        }
        if ("GAUGEMETRICFAMILY".equalsIgnoreCase(getType(metric))) {
            return createGaugeMetricFamily(metric);
        } else {
            return Optional.empty();
        }
    }

    private Optional<GaugeMetricFamily> createGaugeMetricFamily(final Map<String, Object> metric) {
        if (null != getLabels(metric)) {
            return Optional.of(new GaugeMetricFamily(getName(metric), getHelp(metric), getLabels(metric)));
        } else {
            return Optional.of(new GaugeMetricFamily(getName(metric), getHelp(metric), 1));
        }
    }

    private Optional<MetricsWrapper> createById(final String id) {
        Optional<Map<String, Object>> metricMap = findById(id);
        if (!metricMap.isPresent()) {
            return Optional.empty();
        }
        Map<String, Object> metric = metricMap.get();
        if (null == getType(metric)) {
            return Optional.empty();
        }
        switch (getType(metric).toUpperCase()) {
        case "COUNTER":
            return createCounter(metric);
        case "GAUGE":
            return createGauge(metric);
        case "HISTOGRAM":
            return createHistogram(metric);
        case "SUMMARY":
            return createSummary(metric);
        default:
            return Optional.empty();
        }
    }

    private Optional<MetricsWrapper> createCounter(final Map<String, Object> metric) {
        Counter.Builder builder = Counter.build().name(getName(metric)).help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).toArray(new String[0]));
        }
        Counter counter = builder.register();
        CounterWrapper wrapper = new CounterWrapper(counter);
        return Optional.of(wrapper);
    }

    private Optional<MetricsWrapper> createGauge(final Map<String, Object> metric) {
        Gauge.Builder builder = Gauge.build().name(getName(metric)).help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).toArray(new String[0]));
        }
        Gauge gauge = builder.register();
        GaugeWrapper wrapper = new GaugeWrapper(gauge);
        return Optional.of(wrapper);
    }

    private Optional<MetricsWrapper> createHistogram(final Map<String, Object> metric) {
        Histogram.Builder builder = Histogram.build().name(getName(metric)).help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).toArray(new String[0]));
        }
        if (null != getProps(metric)) {
            parserHistogramProps(builder, getProps(metric));
        }
        Histogram histogram = builder.register();
        HistogramWrapper wrapper = new HistogramWrapper(histogram);
        return Optional.of(wrapper);
    }

    private Optional<MetricsWrapper> createSummary(final Map<String, Object> metric) {
        Summary.Builder builder = Summary.build().name(getName(metric)).help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).toArray(new String[0]));
        }
        Summary summary = builder.register();
        SummaryWrapper wrapper = new SummaryWrapper(summary);
        return Optional.of(wrapper);
    }

    @SuppressWarnings("unchecked")
    private void parserHistogramProps(final Histogram.Builder builder, final Map<String, Object> props) {
        if (null == props.get("buckets")) {
            return;
        }
        Map<String, Object> b = (Map<String, Object>) props.get("buckets");
        if ("exp".equals(b.get("type"))) {
            double start = null == b.get("start") ? 1 : Double.parseDouble(b.get("start").toString());
            double factor = null == b.get("factor") ? 1 : Double.parseDouble(b.get("factor").toString());
            int count = null == b.get("count") ? 1 : (int) b.get("count");
            builder.exponentialBuckets(start, factor, count);
        } else if ("linear".equals(b.get("type"))) {
            double start = null == b.get("start") ? 1 : Double.parseDouble(b.get("start").toString());
            double width = null == b.get("width") ? 1 : Double.parseDouble(b.get("width").toString());
            int count = null == b.get("count") ? 1 : (int) b.get("count");
            builder.linearBuckets(start, width, count);
        }
    }

    /**
     * Get metric represented as map.
     *
     * @param id
     *            metric id
     * @return the optional of metric map
     */
    protected Optional<Map<String, Object>> findById(final String id) {
        return metrics.stream().filter(m -> id.equals(getId(m))).findFirst();
    }

    /**
     * Get metric id.
     *
     * @param metric
     *            metric Map
     * @return id of the metric
     */
    protected String getId(final Map<String, Object> metric) {
        return (String) metric.get("id");
    }

    /**
     * Get metric type.
     *
     * @param metric
     *            metric Map
     * @return type of the metric
     */
    protected String getType(final Map<String, Object> metric) {
        return (String) metric.get("type");
    }

    /**
     * Get metric name.
     *
     * @param metric
     *            metric Map
     * @return name of the metric
     */
    protected String getName(final Map<String, Object> metric) {
        return (String) metric.get("name");
    }

    /**
     * Get metric help message.
     *
     * @param metric
     *            metric Map
     * @return help message of the metric
     */
    protected String getHelp(final Map<String, Object> metric) {
        return (String) metric.get("help");
    }

    /**
     * Get metric labels.
     *
     * @param metric
     *            metric Map
     * @return labels of the metric
     */
    @SuppressWarnings("unchecked")
    protected List<String> getLabels(final Map<String, Object> metric) {
        return (List<String>) metric.get("labels");
    }

    /**
     * Get metric properties.
     *
     * @param metric
     *            metric Map
     * @return properties of the metric
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getProps(final Map<String, Object> metric) {
        return (Map<String, Object>) metric.get("props");
    }
}
