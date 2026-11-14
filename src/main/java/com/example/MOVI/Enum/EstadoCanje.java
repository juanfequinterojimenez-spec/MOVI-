package com.example.MOVI.Enum;

public enum EstadoCanje {
    PENDIENTE,  // El pasajero generó el canje y el cupón está listo en su billetera para usar
    UTILIZADO,  // El aliado validó el código comprobante en el local (cupón redimido)
    EXPIRADO,   // El cupón venció sin haberse usado
    CANCELADO,
    VIGENTE// El canje fue anulado y los puntos devueltos (opcional)
}
