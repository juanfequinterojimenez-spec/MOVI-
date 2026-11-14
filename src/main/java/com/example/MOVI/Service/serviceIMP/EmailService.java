package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.ComprobanteEmailDTO;
import com.example.MOVI.Entity.Canje;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ComprobantePdfService pdfService;
    private final QrGeneratorService qrGeneratorService;

    @Async
    public void enviarComprobanteCanje(ComprobanteEmailDTO datos) {
        try {
            byte[] qrBytes = qrGeneratorService.generarQrPng(datos.codigoComprobante(), 220, 220);
            byte[] pdfBytes = pdfService.generarComprobanteCanje(datos);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(datos.correoDestino());
            helper.setSubject("Tu cupón QR MOVI+ (" + datos.codigoComprobante() + ")");

            String vencimientoTexto = (datos.fechaVencimiento() != null)
                    ? datos.fechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Sin fecha límite";

            String cuerpoHtml = """
                    <div style="font-family: Arial, sans-serif; color: #333; max-width: 480px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; padding: 20px; text-align: center;">
                        <h2 style="color: #1976d2; margin-bottom: 8px;">¡Canje exitoso en MOVI+!</h2>
                        <p>Hola <strong>%s</strong>, has redimido <strong>%d puntos</strong> por:</p>
                        <p style="font-size: 16px; color: #00796b; margin: 4px 0;"><strong>%s</strong></p>
                        <p style="font-size: 13px; color: #555; margin-top: 0;">Comercio aliado: <strong>%s</strong></p>
                        <p style="margin-top: 16px;">Muestra este código QR en el establecimiento para reclamar tu beneficio:</p>
                        <div style="margin: 16px 0;">
                            <img src="cid:qrComprobante" alt="Código QR de Canje" style="width: 200px; height: 200px;" />
                        </div>
                        <p style="font-size: 18px; font-family: monospace; background: #f0f4f8; padding: 10px; border-radius: 6px; letter-spacing: 2px;">
                            <strong>%s</strong>
                        </p>
                        <p style="font-size: 12px; color: #d32f2f;">Válido hasta: <strong>%s</strong></p>
                        <p style="font-size: 12px; color: #777;">También encontrarás este comprobante adjunto en formato PDF.</p>
                    </div>
                    """.formatted(
                    datos.nombrePasajero(),
                    datos.puntosGastados(),
                    datos.tituloRecompensa(),
                    datos.nombreAliado(),
                    datos.codigoComprobante(),
                    vencimientoTexto
            );

            helper.setText(cuerpoHtml, true);

            // 1. Imagen QR incrustada dentro del HTML del correo (cid:qrComprobante)
            helper.addInline("qrComprobante", new ByteArrayResource(qrBytes), "image/png");

            // 2. PDF adjunto con el QR y el detalle completo
            helper.addAttachment(
                    "Comprobante_" + datos.codigoComprobante() + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);
            log.info("Correo con QR y PDF enviado correctamente a: {}", datos.correoDestino());
        } catch (Exception e) {
            log.error("Error enviando correo del comprobante {}: {}", datos.codigoComprobante(), e.getMessage());
        }
    }
}
