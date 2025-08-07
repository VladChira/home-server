package com.home.vlad.servermanager.repository.vm;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.vlad.servermanager.model.vm.VirtualMachineEntity;

public interface VirtualMachineRepository extends JpaRepository<VirtualMachineEntity, Long> {
    Optional<VirtualMachineEntity> findByName(String name);
}
