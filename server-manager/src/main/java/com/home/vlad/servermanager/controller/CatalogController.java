package com.home.vlad.servermanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.catalog.DeleteDNSRequest;
import com.home.vlad.servermanager.dto.catalog.ProvisionDNSRequest;
import com.home.vlad.servermanager.model.catalog.ServiceEntity;
import com.home.vlad.servermanager.service.catalog.CatalogService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/manage/api/v1/services")
public class CatalogController {
    private CatalogService catalogService;

    public CatalogController(CatalogService service) {
        this.catalogService = service;
    }

    @GetMapping
    @Operation(summary = "Returns a list of available services in the catalog", description = "Returns a list of available services in the catalog")
    public List<ServiceEntity> list() {
        return catalogService.list();
    }

    @PostMapping
    public ServiceEntity onboardService(@RequestBody ServiceEntity serviceEntity) {
        return catalogService.add(serviceEntity);
    }

    @PostMapping("/dns")
    public void provisionDNS(@RequestBody ProvisionDNSRequest request) {
        catalogService.provisionDNS(request);
    }

    @PostMapping("/dns/delete")
    public void deleteDNS(@RequestBody DeleteDNSRequest request) {
        catalogService.deleteDNS(request);
    }
}
