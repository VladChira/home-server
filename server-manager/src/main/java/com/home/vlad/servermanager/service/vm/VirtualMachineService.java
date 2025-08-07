package com.home.vlad.servermanager.service.vm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.libvirt.VMStatus;
import com.home.vlad.servermanager.dto.novnc.NoVncStatus;
import com.home.vlad.servermanager.exception.vm.VMNotFoundException;
import com.home.vlad.servermanager.model.vm.VirtualMachineEntity;
import com.home.vlad.servermanager.repository.vm.VirtualMachineRepository;
import com.home.vlad.servermanager.service.libvirt.LibvirtService;
import com.home.vlad.servermanager.service.novnc.NoVNCService;

@Service
public class VirtualMachineService {
    Logger logger = LoggerFactory.getLogger(VirtualMachineService.class);

    private final VirtualMachineRepository repo;

    private final LibvirtService libvirtService;
    private final NoVNCService noVNCService;

    public VirtualMachineService(VirtualMachineRepository repo, LibvirtService libvirtService,
            NoVNCService noVNCService) {
        this.repo = repo;
        this.libvirtService = libvirtService;
        this.noVNCService = noVNCService;
    }

    private VirtualMachineEntity ensureVMExists(String name) {
        return repo.findByName(name).orElseThrow(() -> new VMNotFoundException(name));
    }

    public VirtualMachineEntity onboard(VirtualMachineEntity vm) {
        logger.info("Onboarding a new VM: " + vm.toString());
        return repo.save(vm);
    }

    public List<VirtualMachineEntity> list() {
        return repo.findAll();
    }

    public VirtualMachineEntity findByName(String name) {
        return repo.findByName(name)
                .orElseThrow(() -> new VMNotFoundException(name));
    }

    public void deleteByName(String name) {
        logger.info("Deleting a VM with name: " + name);
        VirtualMachineEntity vm = repo.findByName(name)
                .orElseThrow(() -> new VMNotFoundException(name));
        repo.delete(vm);
    }

    public VMStatus getStatusByName(String name) {
        ensureVMExists(name);
        return libvirtService.getStatusByName(name);
    }

    public void startVMByName(String name) {
        logger.info("Starting VM with name: " + name);
        ensureVMExists(name);
        libvirtService.start(name);
    }

    public void shutdownVMByName(String name) {
        logger.info("Shutting down VM with name: " + name);
        ensureVMExists(name);
        libvirtService.shutdown(name);
    }

    public void forceShutdownVMByName(String name) {
        logger.warn("Force shutting down VM with name: " + name);
        ensureVMExists(name);
        libvirtService.forceShutdown(name);
    }

    public NoVncStatus getNoVNCStatus(String name) {
        ensureVMExists(name);
        return noVNCService.getNoVncStatus(name);
    }

    public void startNoVNC(String name) {
        logger.info("Starting noVNC for VM with name: " + name);
        VirtualMachineEntity vm = ensureVMExists(name);
        noVNCService.start(name, vm.getVmPort(), vm.getNovncPort());
    }

    public void stopNoVNC(String name) {
        logger.info("Stopping noVNC for VM with name: " + name);
        ensureVMExists(name);
        noVNCService.stop(name);
    }
}
