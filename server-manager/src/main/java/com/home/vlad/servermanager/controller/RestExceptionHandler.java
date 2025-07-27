package com.home.vlad.servermanager.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.home.vlad.servermanager.exception.libvirt.LibvirtVMNotFoundException;
import com.home.vlad.servermanager.exception.novnc.NoVNCAlreadyRunningException;
import com.home.vlad.servermanager.exception.novnc.NoVNCNotRunningException;
import com.home.vlad.servermanager.exception.vm.VMNotFoundException;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(VMNotFoundException.class)
    public ResponseEntity<?> handleNotFound(VMNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LibvirtVMNotFoundException.class)
    public ResponseEntity<?> handleLibvirtNotFound(LibvirtVMNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoVNCAlreadyRunningException.class)
    public ResponseEntity<?> handleNoVNCAlreadyRunning(NoVNCAlreadyRunningException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoVNCNotRunningException.class)
    public ResponseEntity<?> handleNoVNCNotRunning(NoVNCNotRunningException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
    }
}
