package com.example.MOVI.Enum;

public enum EstadoAliado {
    PENDIENTE,  // Solicitud de registro esperando revisión del administrador
    APROBADO,   // El administrador validó la marca y puede publicar ofertas
    RECHAZADO   // El administrador rechazó la solicitud de registro
}
