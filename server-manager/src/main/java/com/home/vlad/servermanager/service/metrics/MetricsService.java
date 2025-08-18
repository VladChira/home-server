package com.home.vlad.servermanager.service.metrics;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class MetricsService {
    private static final String PROMETHEUS_BASE_URL = "http://192.168.0.169:9090";

    // PromQL (tuned for typical node_exporter semantics)
    // RAM GB used (per instance)
    private static final String Q_RAM_USED_GB = "(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) * 1e-9";

    // CPU % used across cores (per instance): 100 * (1 - avg by (instance)
    // rate(idle[1m]))
    private static final String Q_CPU_USED_PCT = "100 * (1 - avg by (instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[1m])))";

    // CPU temperature in C: average across sensors, annotated with chip_name
    // (works with node_hwmon_* from node_exporter)
    private static final String Q_CPU_TEMP_C = "avg without(sensor) "
            + "(node_hwmon_temp_celsius * on(chip) group_left(chip_name) node_hwmon_chip_names)";

    private final WebClient webClient;

    public MetricsService() {
        DefaultUriBuilderFactory f = new DefaultUriBuilderFactory(PROMETHEUS_BASE_URL);
        f.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        this.webClient = WebClient.builder()
                .baseUrl(PROMETHEUS_BASE_URL)
                .uriBuilderFactory(f)
                .build();
    }

    public JsonNode ramUsagePercentSince(long sinceSeconds, long stepSeconds) {
        long now = Instant.now().getEpochSecond();
        long start = now - sinceSeconds;
        return rangeQuery(Q_RAM_USED_GB, start, now, stepSeconds);
    }

    public JsonNode cpuUsagePercentSince(long sinceSeconds, long stepSeconds) {
        long now = Instant.now().getEpochSecond();
        long start = now - sinceSeconds;
        return rangeQuery(Q_CPU_USED_PCT, start, now, stepSeconds);
    }

    public JsonNode cpuTemperatureSince(long sinceSeconds, long stepSeconds) {
        long now = Instant.now().getEpochSecond();
        long start = now - sinceSeconds;
        return rangeQuery(Q_CPU_TEMP_C, start, now, stepSeconds);
    }

    private JsonNode rangeQuery(String query, long startEpochSec, long endEpochSec, long stepSeconds) {
        String encoded = UriUtils.encodeQueryParam(query, StandardCharsets.UTF_8);
        return webClient.get().uri(builder -> builder.path("/api/v1/query_range")
                .queryParam("query", encoded)
                .queryParam("start", startEpochSec)
                .queryParam("end", endEpochSec)
                .queryParam("step", stepSeconds)
                .build())
                .retrieve().bodyToMono(JsonNode.class).block()
                .get("data")
                .get("result")
                .get(0)
                .get("values"); // this part is a bit hardcoded but that's fine
    }
}
