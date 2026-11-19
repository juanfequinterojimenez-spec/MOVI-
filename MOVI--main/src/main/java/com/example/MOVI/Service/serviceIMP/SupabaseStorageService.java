package com.example.MOVI.Service.serviceIMP;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketPerfiles;

    @Value("${supabase.bucket.ofertas:ofertas}")
    private String bucketOfertas;

    @Value("${supabase.bucket.logos:logo-aliados}")
    private String bucketLogos;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Mantiene compatibilidad con lo que ya tienes (sube al bucket de perfiles)
    public String subirArchivo(MultipartFile file, String carpeta) {
        return subirEnBucket(file, carpeta, bucketPerfiles);
    }

    // 2. Específico para imágenes de recompensas/ofertas -> bucket "ofertas"
    public String subirImagenOferta(MultipartFile file, String carpeta) {
        return subirEnBucket(file, carpeta, bucketOfertas);
    }

    // 3. Específico para logos de comercios aliados -> bucket "logo-aliados"
    public String subirLogoAliado(MultipartFile file, String carpeta) {
        return subirEnBucket(file, carpeta, bucketLogos);
    }

    // Método base reutilizable para cualquier bucket de Supabase
    public String subirEnBucket(MultipartFile file, String carpeta, String bucketDestino) {
        try {
            // 1. Construir ruta única (evita doble barra si carpeta viene vacía o nula)
            String prefijo = (carpeta != null && !carpeta.isBlank()) ? carpeta + "/" : "";
            String nombreArchivo = prefijo + UUID.randomUUID() + "_" + file.getOriginalFilename();
            String urlUpload = supabaseUrl + "/storage/v1/object/" + bucketDestino + "/" + nombreArchivo;

            // 2. Configurar los headers requeridos por Supabase
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.valueOf(
                    file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
            ));
            headers.set("x-upsert", "true");

            // 3. Crear la petición HTTP con los bytes del archivo
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            // 4. Enviar la petición a Supabase Storage
            ResponseEntity<String> response = restTemplate.exchange(
                    urlUpload,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                // 5. Retornar la URL pública apuntando al bucket correspondiente
                return supabaseUrl + "/storage/v1/object/public/" + bucketDestino + "/" + nombreArchivo;
            } else {
                throw new RuntimeException("Error al subir la imagen a Supabase Storage (" + bucketDestino + "): " + response.getBody());
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudieron leer los bytes del archivo MultipartFile", e);
        }
    }
}


