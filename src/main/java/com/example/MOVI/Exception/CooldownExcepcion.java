package com.example.MOVI.Exception;

import lombok.Getter;

@Getter
public class CooldownExcepcion extends RuntimeException {

    private final long minutosRestantes;
    private final long segundosRestantes;

    public CooldownExcepcion(String mensaje, long minutosRestantes, long segundosRestantes) {
        super(mensaje);
        this.minutosRestantes = minutosRestantes;
        this.segundosRestantes = segundosRestantes;
    }
}
