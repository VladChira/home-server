package com.home.vlad.servermanager.exception.libvirt;

public class LibvirtFailedToConnectException extends RuntimeException {
    public LibvirtFailedToConnectException() {
        super("Failed to connect to qemu");
    }
}
