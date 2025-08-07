package com.home.vlad.servermanager.model.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Id
    @Column(name = "`service_key`", length = 255)
    private String key;

     @JsonProperty("service_name")
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(nullable = false, length = 255)
    private String owner;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @JsonProperty("created_at")
    @Column(name = "created_at", nullable = false, length = 255)
    private String createdAt;

    @ElementCollection
    @CollectionTable(
      name = "service_tags",
      joinColumns = @JoinColumn(name = "service_key")
    )
    @Column(name = "tag", length = 100)
    private List<String> tags;

    @Column(length = 45)
    private String ip;

    private Integer port;

    @Column(length = 255)
    private String domain;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
