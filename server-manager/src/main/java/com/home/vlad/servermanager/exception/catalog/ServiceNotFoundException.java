package com.home.vlad.servermanager.exception.catalog;

public class ServiceNotFoundException extends RuntimeException{
    public ServiceNotFoundException(String id) {
        super("Could not find servie with id/name" + id);
    }
}

