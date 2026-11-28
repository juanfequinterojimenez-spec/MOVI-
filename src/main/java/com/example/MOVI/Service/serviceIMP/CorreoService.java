
package com.example.MOVI.Service.serviceIMP;

import lombok.RequiredArgsConstructor;

import tools.jackson.databind.json.JsonMapper;

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
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private static final Logger log =
            Logger.getLogger(CorreoService.class.getName());

    private static final URI BREVO_URL =
            URI.create("https://api.brevo.com/v3/smtp/email");

    private final JsonMapper jsonMapper;

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

        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo de destino es obligatorio"
            );
        }

        if (codigo == null || !codigo.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "El código de verificación no es válido"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "BREVO_API_KEY no está configurada"
            );
        }

        if (correoRemitente == null ||
                correoRemitente.isBlank()) {

            throw new IllegalStateException(
                    "BREVO_SENDER_EMAIL no está configurado"
            );
        }

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
                            Map.of("email", destino.trim())
                    ),

                    "subject",
                    "MOVI+ | Código de verificación",

                    "textContent", contenido
            );

            String json =
                    jsonMapper.writeValueAsString(mensaje);

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(BREVO_URL)
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

            int estado = respuesta.statusCode();

            if (estado < 200 || estado >= 300) {

                log.warning(
                        "Brevo rechazó el correo. HTTP " +
                                estado
                );

                throw new IllegalStateException(
                        "Brevo rechazó el envío. HTTP " +
                                estado
                );
            }

            log.info(
                    "Brevo aceptó la solicitud de envío. HTTP " +
                            estado
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Se interrumpió el envío del correo",
                    e
            );

        } catch (IllegalStateException e) {

            throw e;

        } catch (Exception e) {

            log.severe(
                    "Error de comunicación con Brevo: " +
                            e.getClass().getSimpleName()
            );

            throw new IllegalStateException(
                    "No se pudo enviar el correo de verificación",
                    e
            );
        }
    }

    public void enviarCodigoRecuperacion(String destino, String codigo) {
        if (destino == null || destino.isBlank() || codigo == null || !codigo.matches("\\d{6}")) {
            throw new IllegalArgumentException("Solicitud de recuperación inválida");
        }
        if (apiKey == null || apiKey.isBlank() || correoRemitente == null || correoRemitente.isBlank()) {
            throw new IllegalStateException("El servicio de correo no está configurado");
        }
        String contenido = "MOVI+ - Recuperación de contraseña\n\n" +
            "Tu código para restablecer la contraseña es: " + codigo + "\n" +
            "Vence en 10 minutos. Si no lo solicitaste, ignora este mensaje.\n" +
            "Nunca compartas este código.";
        try {
            Map<String,Object> mensaje = Map.of(
                "sender", Map.of("name", nombreRemitente, "email", correoRemitente),
                "to", List.of(Map.of("email", destino.trim())),
                "subject", "MOVI+ | Recuperación de contraseña",
                "textContent", contenido
            );
            HttpRequest solicitud = HttpRequest.newBuilder().uri(BREVO_URL)
                .timeout(Duration.ofSeconds(20)).header("accept", "application/json")
                .header("api-key", apiKey).header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    jsonMapper.writeValueAsString(mensaje), StandardCharsets.UTF_8)).build();
            HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300)
                throw new IllegalStateException("No se pudo enviar el correo de recuperación");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envío interrumpido", e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo enviar el correo de recuperación", e);
        }
    }

}
