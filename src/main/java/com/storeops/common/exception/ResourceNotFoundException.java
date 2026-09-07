package com.storeops.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource, String id) {
        super("RESOURCE_NOT_FOUND", resource + " not found: " + id, HttpStatus.NOT_FOUND);
    }
}
