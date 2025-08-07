package com.home.vlad.servermanager.exception.catalog;

public class FailedToProvisionException extends RuntimeException{
    public FailedToProvisionException(String serviceKey, String domain) {
        super("Failed to provision domain name " + domain + " for service " + serviceKey);
    }
}
