package com.home.vlad.servermanager.service.vm;

import java.util.List;

import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.libvirt.VMStatus;
import com.home.vlad.servermanager.dto.novnc.NoVncStatus;
import com.home.vlad.servermanager.exception.vm.VMNotFoundException;
import com.home.vlad.servermanager.model.vm.VirtualMachine;
import com.home.vlad.servermanager.repository.vm.VirtualMachineRepository;
import com.home.vlad.servermanager.service.libvirt.LibvirtService;
import com.home.vlad.servermanager.service.novnc.NoVNCService;

@Service
public class VirtualMachineService {
    private final VirtualMachineRepository repo;

    private final LibvirtService libvirtService;
    private final NoVNCService noVNCService;

    public VirtualMachineService(VirtualMachineRepository repo, LibvirtService libvirtService,
            NoVNCService noVNCService) {
        this.repo = repo;
        this.libvirtService = libvirtService;
        this.noVNCService = noVNCService;
    }

    private VirtualMachine ensureVMExists(String name) {
        return repo.findByName(name).orElseThrow(() -> new VMNotFoundException(name));
    }

    public VirtualMachine onboard(VirtualMachine vm) {
        return repo.save(vm);
    }

    public List<VirtualMachine> list() {
        return repo.findAll();
    }

    public VirtualMachine findByName(String name) {
        return repo.findByName(name)
                .orElseThrow(() -> new VMNotFoundException(name));
    }

    public void deleteByName(String name) {
        VirtualMachine vm = repo.findByName(name)
                .orElseThrow(() -> new VMNotFoundException(name));
        repo.delete(vm);
    }

    public VMStatus getStatusByName(String name) {
        ensureVMExists(name);
        return libvirtService.getStatusByName(name);
    }

    public void startVMByName(String name) {
        ensureVMExists(name);
        libvirtService.start(name);
    }

    public void shutdownVMByName(String name) {
        ensureVMExists(name);
        libvirtService.shutdown(name);
    }

    public void forceShutdownVMByName(String name) {
        ensureVMExists(name);
        libvirtService.forceShutdown(name);
    }

    public NoVncStatus getNoVNCStatus(String name) {
        ensureVMExists(name);
        return noVNCService.getNoVncStatus(name);
    }

    public void startNoVNC(String name) {
        VirtualMachine vm = ensureVMExists(name);
        noVNCService.start(name, vm.getVmPort(), vm.getNovncPort());
    }

    public void stopNoVNC(String name) {
        ensureVMExists(name);
        noVNCService.stop(name);
    }
}
