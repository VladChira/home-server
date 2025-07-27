package com.home.vlad.servermanager.exception.vm;

public class VMNotFoundException extends RuntimeException {
    public VMNotFoundException(String name) {
        super("VM not found: " + name);
    }
}
