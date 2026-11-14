package com.example.MOVI.Exception;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String mensaje) {
        super(mensaje);
    }
}
