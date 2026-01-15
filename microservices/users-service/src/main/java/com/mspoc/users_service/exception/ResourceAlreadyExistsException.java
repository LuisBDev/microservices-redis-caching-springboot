package com.mspoc.users_service.exception;

/**
 * Excepción lanzada cuando ya existe un recurso con los mismos datos únicos.
 * 
 * @author Luis Balarezo
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
