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
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    public String subirArchivo(MultipartFile file, String carpeta) {
        try {
            // 1. Generar un nombre único para evitar colisiones
            String nombreArchivo = carpeta + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            String urlUpload = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + nombreArchivo;

            // 2. Configurar los headers requeridos por Supabase
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.valueOf(file.getContentType()));
            headers.set("x-upsert", "true"); // Sobrescribir si ya existe

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
                // 5. Retornar la URL pública del archivo almacenado
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + nombreArchivo;
            } else {
                throw new RuntimeException("Error al subir la imagen a Supabase Storage: " + response.getBody());
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudieron leer los bytes del archivo MultipartFile", e);
        }
    }
}


