package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.ComprobanteEmailDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ComprobantePdfService pdfService;
    private final QrGeneratorService qrGeneratorService;
    private final JsonMapper jsonMapper;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String correoRemitente;

    @Value("${BREVO_SENDER_NAME:MOVI+}")
    private String nombreRemitente;

    private static final String BREVO_URL =
            "https://api.brevo.com/v3/smtp/email";

    // ==========================================
    // ENVIAR COMPROBANTE DE CANJE
    // ==========================================

    @Async
    public void enviarComprobanteCanje(
            ComprobanteEmailDTO datos
    ) {

        try {

            // 1. Generar código QR
            byte[] qrBytes =
                    qrGeneratorService.generarQrPng(
                            datos.codigoComprobante(),
                            220,
                            220
                    );

            // 2. Generar comprobante PDF
            byte[] pdfBytes =
                    pdfService.generarComprobanteCanje(
                            datos
                    );

            // 3. Preparar fecha de vencimiento
            String vencimientoTexto =
                    datos.fechaVencimiento() != null
                            ? datos.fechaVencimiento().format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy HH:mm"
                            )
                    )
                            : "Sin fecha límite";

            // 4. Proteger el contenido HTML
            String nombre = escapar(
                    datos.nombrePasajero()
            );

            String recompensa = escapar(
                    datos.tituloRecompensa()
            );

            String aliado = escapar(
                    datos.nombreAliado()
            );

            String codigo = escapar(
                    datos.codigoComprobante()
            );

            // 5. Construir correo HTML
            String cuerpoHtml = """
                <html>
                <body style="
                    font-family: Arial, sans-serif;
                    background-color: #f4f5fa;
                    padding: 24px;
                ">

                <div style="
                    max-width: 500px;
                    margin: auto;
                    background: white;
                    border-radius: 18px;
                    padding: 28px;
                    text-align: center;
                ">

                    <h1 style="
                        color: #e8161b;
                        margin-bottom: 8px;
                    ">
                        MOVI+
                    </h1>

                    <h2 style="color: #252545;">
                        ¡Canje exitoso!
                    </h2>

                    <p>Hola <strong>%s</strong></p>

                    <p>
                        Has redimido
                        <strong>%s puntos</strong>
                        por la siguiente recompensa:
                    </p>

                    <h3 style="color: #e8161b;">
                        %s
                    </h3>

                    <p>
                        Comercio aliado:
                        <strong>%s</strong>
                    </p>

                    <div style="
                        background: #f4f5fa;
                        border-radius: 12px;
                        padding: 18px;
                        margin: 20px 0;
                    ">

                        <p>Tu código de canje es:</p>

                        <h2 style="
                            color: #252545;
                            letter-spacing: 3px;
                        ">
                            %s
                        </h2>

                    </div>

                    <p>
                        Encontrarás tu código QR y el
                        comprobante PDF adjuntos a este correo.
                    </p>

                    <p>
                        Presenta el código QR en el comercio
                        aliado para reclamar tu beneficio.
                    </p>

                    <p style="
                        color: #d32f2f;
                        font-size: 13px;
                    ">
                        Válido hasta:
                        <strong>%s</strong>
                    </p>

                    <hr style="border: 1px solid #eeeeee;">

                    <p style="
                        color: #888888;
                        font-size: 12px;
                    ">
                        Gracias por utilizar MOVI+.
                    </p>

                </div>

                </body>
                </html>
                """.formatted(
                    nombre,
                    datos.puntosGastados(),
                    recompensa,
                    aliado,
                    codigo,
                    escapar(vencimientoTexto)
            );

            // 6. Convertir archivos a Base64
            String qrBase64 =
                    Base64.getEncoder()
                            .encodeToString(qrBytes);

            String pdfBase64 =
                    Base64.getEncoder()
                            .encodeToString(pdfBytes);

            // 7. Generar nombre seguro para archivos
            String codigoArchivo =
                    datos.codigoComprobante().replaceAll(
                            "[^a-zA-Z0-9_-]",
                            "_"
                    );

            // 8. Preparar adjuntos
            List<Map<String, String>> adjuntos = List.of(

                    Map.of(
                            "name",
                            "QR_" + codigoArchivo + ".png",
                            "content",
                            qrBase64
                    ),

                    Map.of(
                            "name",
                            "Comprobante_" +
                                    codigoArchivo + ".pdf",
                            "content",
                            pdfBase64
                    )
            );

            // 9. Construir solicitud Brevo
            Map<String, Object> mensaje = Map.of(

                    "sender",
                    Map.of(
                            "name", nombreRemitente,
                            "email", correoRemitente
                    ),

                    "to",
                    List.of(
                            Map.of(
                                    "email",
                                    datos.correoDestino()
                            )
                    ),

                    "subject",
                    "Tu cupón QR MOVI+ (" +
                            datos.codigoComprobante() + ")",

                    "htmlContent",
                    cuerpoHtml,

                    "attachment",
                    adjuntos
            );

            // 10. Convertir solicitud a JSON
            String json =
                    jsonMapper.writeValueAsString(
                            mensaje
                    );

            // 11. Crear solicitud HTTP
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(BREVO_URL))
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "api-key",
                                    apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    json,
                                                    StandardCharsets.UTF_8
                                            )
                            )
                            .build();

            // 12. Enviar a Brevo
            HttpResponse<String> respuesta =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            // 13. Validar respuesta
            int estado = respuesta.statusCode();

            if (estado >= 200 && estado < 300) {

                log.info(
                        "Brevo aceptó comprobante de canje {} para {}",
                        datos.codigoComprobante(),
                        datos.correoDestino()
                );

            } else {

                log.error(
                        "Brevo rechazó comprobante {}. HTTP {}: {}",
                        datos.codigoComprobante(),
                        estado,
                        respuesta.body()
                );

            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log.error(
                    "Envío de comprobante interrumpido: {}",
                    datos.codigoComprobante(),
                    e
            );

        } catch (Exception e) {

            log.error(
                    "Error enviando comprobante de canje {}",
                    datos.codigoComprobante(),
                    e
            );
        }
    }

    private String escapar(String valor) {

        if (valor == null) {
            return "";
        }

        return HtmlUtils.htmlEscape(valor);
    }
}

