package com.example.MOVI.Service.serviceIMP;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private final ObjectMapper objectMapper;

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String correoRemitente;

    @Value("${BREVO_SENDER_NAME:MOVI+}")
    private String nombreRemitente;

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void enviarCodigo(String destino, String codigo) {

        String contenido = """
                Hola,

                Bienvenido a MOVI+.

                Tu código de verificación es:

                %s

                Este código vence en 10 minutos.

                Si no solicitaste esta verificación,
                puedes ignorar este correo.

                Equipo MOVI+
                """.formatted(codigo);

        try {
            Map<String, Object> mensaje = Map.of(
                    "sender", Map.of(
                            "name", nombreRemitente,
                            "email", correoRemitente
                    ),
                    "to", List.of(
                            Map.of("email", destino)
                    ),
                    "subject",
                    "MOVI+ | Código de verificación",
                    "textContent", contenido
            );

            String json = objectMapper
                    .writeValueAsString(mensaje);

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.brevo.com/v3/smtp/email"
                    ))
                    .timeout(Duration.ofSeconds(20))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header(
                            "content-type",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    json,
                                    StandardCharsets.UTF_8
                            )
                    )
                    .build();

            HttpResponse<String> respuesta =
                    cliente.send(
                            solicitud,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (respuesta.statusCode() < 200 ||
                    respuesta.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Brevo rechazó el envío. HTTP "
                                + respuesta.statusCode()
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Se interrumpió el envío del correo",
                    e
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo enviar el correo de verificación",
                    e
            );
        }
    }
}
