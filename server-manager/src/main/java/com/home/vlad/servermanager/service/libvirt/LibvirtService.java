package com.home.vlad.servermanager.service.libvirt;

import java.time.Duration;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Duration TIMEOUT = Duration.ofSeconds(8);
    private static final Duration POLL = Duration.ofMillis(700);

    private Logger logger = LoggerFactory.getLogger(LibvirtService.class);

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

            waitForState(vmDomain, DomainInfo.DomainState.VIR_DOMAIN_RUNNING, TIMEOUT, POLL);
            logger.info("VM started");
        } catch (LibvirtException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LibvirtFailedToStartException(vmName);
        }
    }

    public void shutdown(String vmName) {
        Domain vmDomain = getVMDomainByName(vmName);

        try {
            if (vmDomain.isActive() == 1) {
                vmDomain.shutdown();
            }
            waitForState(vmDomain, DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF, TIMEOUT, POLL);
            logger.info("VM shut down");
        } catch (LibvirtException | InterruptedException e) {
            Thread.currentThread().interrupt();
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

    private boolean waitForState(Domain d,
            DomainState target,
            Duration timeout,
            Duration poll) throws LibvirtException, InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            DomainInfo.DomainState s = d.getInfo().state;
            if (target.equals(s))
                return true;
            logger.info("Still waiting, current state is " + s.toString());
            Thread.sleep(poll.toMillis());
        }
        return false;
    }
}
