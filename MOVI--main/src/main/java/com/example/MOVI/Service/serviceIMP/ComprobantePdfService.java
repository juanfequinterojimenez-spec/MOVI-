package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.ComprobanteEmailDTO;
import com.example.MOVI.Entity.Canje;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ComprobantePdfService {

    private final QrGeneratorService qrGeneratorService;

    public byte[] generarComprobanteCanje(ComprobanteEmailDTO datos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, out);
            document.open();

            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(25, 118, 210));
            Font subtituloFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.DARK_GRAY);
            Font textoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font codigoFont = new Font(Font.COURIER, 15, Font.BOLD, new Color(0, 121, 107));

            // 1. Encabezado
            Paragraph titulo = new Paragraph("MOVI+ | Cupón de Canje", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            // 2. Imagen QR con el codigoComprobante
            byte[] qrBytes = qrGeneratorService.generarQrPng(datos.codigoComprobante(), 180, 180);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            // 3. Código legible debajo del QR
            Paragraph codigo = new Paragraph(datos.codigoComprobante(), codigoFont);
            codigo.setAlignment(Element.ALIGN_CENTER);
            document.add(codigo);
            document.add(new Paragraph(" "));

            // 4. Formatear fechas desde datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String fecha = (datos.fechaCanje() != null)
                    ? datos.fechaCanje().format(formatter)
                    : "Reciente";

            String vencimiento = (datos.fechaVencimiento() != null)
                    ? datos.fechaVencimiento().format(formatter)
                    : "Sin vencimiento";

            // 5. Tabla de detalles
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{40f, 60f});

            agregarFila(tabla, "Pasajero:", datos.nombrePasajero(), subtituloFont, textoFont);
            agregarFila(tabla, "Recompensa:", datos.tituloRecompensa(), subtituloFont, textoFont);
            agregarFila(tabla, "Comercio Aliado:", datos.nombreAliado(), subtituloFont, textoFont);
            agregarFila(tabla, "Puntos Redimidos:", datos.puntosGastados() + " pts", subtituloFont, textoFont);
            agregarFila(tabla, "Fecha de Canje:", fecha, subtituloFont, textoFont);
            agregarFila(tabla, "Válido hasta:", vencimiento, subtituloFont, textoFont);
            agregarFila(tabla, "Estado:", datos.estado(), subtituloFont, textoFont);

            document.add(tabla);
            document.add(new Paragraph(" "));

            Paragraph nota = new Paragraph(
                    "Presenta este código QR o el código alfanumérico al aliado para validar tu beneficio.",
                    new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY)
            );
            nota.setAlignment(Element.ALIGN_CENTER);
            document.add(nota);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante PDF con QR", e);
        }
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor, Font fontLabel, Font fontVal) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(etiqueta, fontLabel));
        cellLabel.setPadding(5);
        cellLabel.setBackgroundColor(new Color(245, 247, 250));

        PdfPCell cellVal = new PdfPCell(new Phrase(valor, fontVal));
        cellVal.setPadding(5);

        tabla.addCell(cellLabel);
        tabla.addCell(cellVal);
    }
}