package com.home.vlad.servermanager.exception.novnc;

public class NoVNCNotRunningException extends RuntimeException{
    public NoVNCNotRunningException(String name) {
        super("NoVNC is not running for VM " + name);
    }
}
