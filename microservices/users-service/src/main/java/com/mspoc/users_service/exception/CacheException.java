package com.mspoc.users_service.exception;

/**
 * Excepción lanzada cuando hay un error con el caché.
 * 
 * @author Luis Balarezo
 */
public class CacheException extends RuntimeException {

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
