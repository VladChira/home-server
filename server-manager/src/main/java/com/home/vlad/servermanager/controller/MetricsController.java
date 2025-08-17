package com.home.vlad.servermanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.home.vlad.servermanager.service.metrics.MetricsService;


@RestController
@RequestMapping("/manage/api/v1/metrics")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/ram")
    public JsonNode getRAMUsage(@RequestParam(value = "since") Integer sinceSeconds,
            @RequestParam(value = "step") Integer stepSeconds) {
        return metricsService.ramUsagePercentSince(sinceSeconds, stepSeconds);
    }

    @GetMapping("/cpu")
    public JsonNode getCPUUsage(@RequestParam(value = "since") Integer sinceSeconds,
            @RequestParam(value = "step") Integer stepSeconds) {
        return metricsService.cpuUsagePercentSince(sinceSeconds, stepSeconds);
    }

    @GetMapping("/temperature")
    public JsonNode getTemperatureUsage(@RequestParam(value = "since") Integer sinceSeconds,
            @RequestParam(value = "step") Integer stepSeconds) {
        return metricsService.cpuTemperatureSince(sinceSeconds, stepSeconds);
    }
}
