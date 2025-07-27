package com.home.vlad.servermanager.exception.libvirt;

public class LibvirtFailedToGetStateException extends RuntimeException{
    public LibvirtFailedToGetStateException(String name) {
        super("Failed to fetch state of VM:" + name);
    }
}
