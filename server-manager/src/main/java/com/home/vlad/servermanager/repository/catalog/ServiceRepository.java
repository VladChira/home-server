package com.home.vlad.servermanager.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.vlad.servermanager.model.catalog.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, String> {
}
