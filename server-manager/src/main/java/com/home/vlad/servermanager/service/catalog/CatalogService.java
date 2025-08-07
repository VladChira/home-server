package com.home.vlad.servermanager.service.catalog;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.home.vlad.servermanager.dto.catalog.DeleteDNSIpPortRequest;
import com.home.vlad.servermanager.dto.catalog.DeleteDNSRequest;
import com.home.vlad.servermanager.dto.catalog.ProvisionDNSIpPortRequest;
import com.home.vlad.servermanager.dto.catalog.ProvisionDNSRequest;
import com.home.vlad.servermanager.exception.catalog.ServiceNotFoundException;
import com.home.vlad.servermanager.model.catalog.ServiceEntity;
import com.home.vlad.servermanager.repository.catalog.ServiceRepository;

import jakarta.transaction.Transactional;

@Service
public class CatalogService {
    private final ServiceRepository serviceRepository;

    private final WebClient webClient;

    @Autowired
    public CatalogService(ServiceRepository repository) {
        this.serviceRepository = repository;
        this.webClient = WebClient.builder()
                .baseUrl("http://192.168.0.170:5000")
                .build();
    }

    public List<ServiceEntity> list() {
        return serviceRepository.findAll();
    }

    public ServiceEntity add(ServiceEntity service) {
        return serviceRepository.save(service);
    }

    public ServiceEntity get(String key) {
        return serviceRepository.findById(key).orElseThrow(() -> new ServiceNotFoundException(key));
    }

    public void delete(String key) {
        serviceRepository.deleteById(key);
    }

    @Transactional
    public ServiceEntity update(String serviceKey, ServiceEntity updated) {
        ServiceEntity service = get(serviceKey);
        BeanUtils.copyProperties(updated, service, "serviceKey");
        return serviceRepository.save(service);
    }

    public void provisionDNS(ProvisionDNSRequest request) {
        ServiceEntity service = get(request.getServiceKey());
        String domainName = request.getDomain();

        String ip = service.getIp();
        int port = service.getPort();

        ProvisionDNSIpPortRequest req = ProvisionDNSIpPortRequest.builder()
                .domain(domainName)
                .ip(ip)
                .port(port)
                .build();

        webClient.post().uri("/api/services/dns")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve().bodyToMono(Void.class)
                .block();

        // Since the provisioning was sucessful, update the db records too
        service.setDomain(domainName);
        update(request.getServiceKey(), service);
    }

    public void deleteDNS(DeleteDNSRequest request) {
        ServiceEntity service = get(request.getServiceKey());

        String ip = service.getIp();
        int port = service.getPort();

        DeleteDNSIpPortRequest req = DeleteDNSIpPortRequest.builder()
                .ip(ip)
                .port(port)
                .build();

        webClient.post().uri("/api/services/dns/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve().bodyToMono(Void.class)
                .block();

        // Since the provisioning was sucessful, update the db records too
        service.setDomain(null);
        update(request.getServiceKey(), service);
    }
}
