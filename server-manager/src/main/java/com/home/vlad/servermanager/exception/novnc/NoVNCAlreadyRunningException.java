package com.home.vlad.servermanager.exception.novnc;

public class NoVNCAlreadyRunningException extends RuntimeException{
    public NoVNCAlreadyRunningException(String name) {
        super("NoVNC is already running for VM" + name);
    }
}
