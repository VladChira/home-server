package com.home.vlad.servermanager.exception.libvirt;

public class LibvirtVMNotFoundException extends RuntimeException{
    public LibvirtVMNotFoundException(String name) {
        super("VM not found on host: " + name);
    }
}
