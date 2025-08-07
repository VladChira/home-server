package com.home.vlad.servermanager.model.vm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "virtual_machine")
public class VirtualMachineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "novnc_port", nullable = false)
    private Integer novncPort;

    @Column(name = "vm_port", nullable = false)
    private Integer vmPort;

    @Column(name = "base_path", nullable = false)
    private String basePath;
}
