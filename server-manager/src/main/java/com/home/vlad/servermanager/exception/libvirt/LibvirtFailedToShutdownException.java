package com.home.vlad.servermanager.exception.libvirt;

public class LibvirtFailedToShutdownException extends RuntimeException{
    public LibvirtFailedToShutdownException(String name) {
        super("Failed to shutdown VM: " + name);
    }
}
