package com.home.vlad.servermanager.exception.libvirt;

public class LibvirtFailedToStartException extends RuntimeException{
    public LibvirtFailedToStartException(String name) {
        super("Failed to start VM: " + name);
    }
}
