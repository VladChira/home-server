package com.home.vlad.servermanager.repository.vm;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.vlad.servermanager.model.vm.VirtualMachine;

public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Long> {
    Optional<VirtualMachine> findByName(String name);
}
