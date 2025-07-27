package com.home.vlad.servermanager.exception.novnc;

public class NoVNCFailedToStartException extends RuntimeException{
     public NoVNCFailedToStartException(int port, String name) {
        super("Failed to start novnc on port " + port + " for VM " + name);
    }
}
