package com.home.vlad.servermanager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.libvirt.VMStatus;
import com.home.vlad.servermanager.model.vm.VirtualMachineEntity;
import com.home.vlad.servermanager.service.vm.VirtualMachineService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/manage/api/v1/vms")
public class VirtualMachineController {
    private VirtualMachineService vmService;

    public VirtualMachineController(VirtualMachineService vmService) {
        this.vmService = vmService;
    }

    @GetMapping
    @Operation(summary = "Returns a list of all available onboarded VMs", description = "Returns a list of all available onboarded VMs")
    public List<VirtualMachineEntity> list() {
        return vmService.list();
    }

    @GetMapping("/{name}")
    public VirtualMachineEntity get(@PathVariable String name) {
        return vmService.findByName(name);
    }

    @PostMapping
    public VirtualMachineEntity onboard(@RequestBody VirtualMachineEntity vm) {
        return vmService.onboard(vm);
    }


    @GetMapping("/{name}/status")
    public VMStatus status(@PathVariable String name) {
        return vmService.getStatusByName(name);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        vmService.deleteByName(name);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{name}/start")
    public ResponseEntity<Void> start(@PathVariable String name) {
        vmService.startVMByName(name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{name}/shutdown")
    public ResponseEntity<Void> shutdown(@PathVariable String name) {
        vmService.shutdownVMByName(name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{name}/force-shutdown")
    public ResponseEntity<Void> forceShutdown(@PathVariable String name) {
        vmService.forceShutdownVMByName(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/guac-token")
    public ResponseEntity<Map<String, String>> getGuacToken() {
        return ResponseEntity.ok(Map.of("guac-token", vmService.getGuacToken()));
    }
}
