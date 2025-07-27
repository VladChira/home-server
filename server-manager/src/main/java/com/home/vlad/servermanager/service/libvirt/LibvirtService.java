package com.home.vlad.servermanager.service.libvirt;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.DomainInfo.DomainState;
import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.libvirt.VMStatus;
import com.home.vlad.servermanager.exception.libvirt.LibvirtFailedToConnectException;
import com.home.vlad.servermanager.exception.libvirt.LibvirtFailedToGetStateException;
import com.home.vlad.servermanager.exception.libvirt.LibvirtFailedToShutdownException;
import com.home.vlad.servermanager.exception.libvirt.LibvirtFailedToStartException;
import com.home.vlad.servermanager.exception.libvirt.LibvirtVMNotFoundException;

@Service
public class LibvirtService {
    private static final String CONNECT_URI = "qemu:///system";

    private Domain getVMDomainByName(String vmName) {
        Connect conn;
        try {
            conn = new Connect(CONNECT_URI);
        } catch (LibvirtException e) {
            throw new LibvirtFailedToConnectException();
        }

        Domain vmDomain;
        try {
            vmDomain = conn.domainLookupByName(vmName);
        } catch (LibvirtException e) {
            throw new LibvirtVMNotFoundException(vmName);
        }

        return vmDomain;
    }

    public VMStatus getStatusByName(String vmName) {
        Domain vmDomain = getVMDomainByName(vmName);

        DomainState vmState;
        try {
            vmState = vmDomain.getInfo().state;
        } catch (LibvirtException e) {
            throw new LibvirtFailedToGetStateException(vmName);
        }

        return new VMStatus(vmName, vmState.toString());
    }

    public void start(String vmName) {
        Domain vmDomain = getVMDomainByName(vmName);

        try {
            if (vmDomain.isActive() == 0) {
                vmDomain.create();
            }
        } catch (LibvirtException e) {
            throw new LibvirtFailedToStartException(vmName);
        }
    }

    public void shutdown(String vmName) {
        Domain vmDomain = getVMDomainByName(vmName);

        try {
            if (vmDomain.isActive() == 1) {
                vmDomain.shutdown();
            }
        } catch (LibvirtException e) {
            throw new LibvirtFailedToShutdownException(vmName);
        }
    }

    public void forceShutdown(String vmName) {
        Domain vmDomain = getVMDomainByName(vmName);

        try {
            if (vmDomain.isActive() == 1) {
                vmDomain.destroy();
            }
        } catch (LibvirtException e) {
            throw new LibvirtFailedToShutdownException(vmName);
        }
    }
}
