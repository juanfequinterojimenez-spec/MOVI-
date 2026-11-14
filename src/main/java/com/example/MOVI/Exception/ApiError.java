package com.example.MOVI.Exception;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> detalles
) {}
