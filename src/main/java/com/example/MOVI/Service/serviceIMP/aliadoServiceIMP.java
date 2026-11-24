
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Config.JwtService;
import com.example.MOVI.DTO.ResponseDTO.AliadoRegistroDTO;
import com.example.MOVI.DTO.ResponseDTO.AliadoResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.PerfilAliadoDto;
import com.example.MOVI.DTO.ResponseDTO.recompensaRequestDTO;
import com.example.MOVI.Entity.Aliado;
import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Enum.categoriaAliado;
import com.example.MOVI.Enum.EstadoAliado;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.aliadoRepository;
import com.example.MOVI.Repository.recompensaRepository;
import com.example.MOVI.Repository.usuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class aliadoServiceIMP implements aliadoService {

    private final usuarioRepository usuarioRepository;
    private final aliadoRepository aliadoRepository;
    private final recompensaRepository recompensaRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ==========================================
    // 1. PUBLICAR RECOMPENSA CON IMAGEN
    // ==========================================

    @Override
    @Transactional
    public Recompensa publicarOferta(
            String correoAliado,
            recompensaRequestDTO dto,
            MultipartFile imagen
    ) {

        Aliado aliado = obtenerAliadoPorCorreo(correoAliado);

        if (dto == null) {
            throw new BusinessRuleException(
                    "Los datos de la recompensa son obligatorios."
            );
        }

        if (dto.titulo() == null ||
                dto.titulo().isBlank()) {

            throw new BusinessRuleException(
                    "El título de la recompensa es obligatorio."
            );
        }

        if (dto.descripcion() == null ||
                dto.descripcion().isBlank()) {

            throw new BusinessRuleException(
                    "La descripción es obligatoria."
            );
        }

        if (dto.costoPuntos() == null ||
                dto.costoPuntos() <= 0) {

            throw new BusinessRuleException(
                    "Los puntos requeridos deben ser mayores a cero."
            );
        }

        if (dto.stockDisponible() != null &&
                dto.stockDisponible() < 0) {

            throw new BusinessRuleException(
                    "El stock no puede ser negativo."
            );
        }

        // Subir imagen del cupón a Supabase Storage
        String urlImagenOferta = null;

        if (imagen != null && !imagen.isEmpty()) {

            validarImagen(imagen);

            urlImagenOferta =
                    supabaseStorageService.subirArchivo(
                            imagen,
                            "ofertas"
                    );
        }

        // Crear recompensa asociada al aliado
        Recompensa nuevaOferta = Recompensa.builder()
                .titulo(dto.titulo().trim())
                .descripcionRecompensa(
                        dto.descripcion().trim()
                )
                .costoPuntos(dto.costoPuntos())
                .stockDisponible(dto.stockDisponible())
                .imagenURL(urlImagenOferta)
                .aliado(aliado)

                // Requiere aprobación administrativa
                .activa(false)
                .build();

        return recompensaRepository.save(nuevaOferta);
    }

    // ==========================================
    // 2. REGISTRAR ALIADO CON LOGO
    // ==========================================

    @Override
    @Transactional
    public AliadoResponseDTO registrarAliado(
            AliadoRegistroDTO dto,
            MultipartFile logo
    ) {

        if (dto == null ||
                dto.correo() == null ||
                dto.correo().isBlank()) {

            throw new BusinessRuleException(
                    "El correo corporativo es obligatorio."
            );
        }

        String correo = dto.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (correo.endsWith("@gmail.com") ||
                correo.endsWith("@hotmail.com") ||
                correo.endsWith("@yahoo.com") ||
                correo.endsWith("@outlook.com")) {

            throw new BusinessRuleException(
                    "Los aliados deben registrarse con un " +
                            "correo corporativo."
            );
        }

        if (usuarioRepository.existsByEmail(correo)) {

            throw new BusinessRuleException(
                    "El correo ya está registrado."
            );
        }

        if (dto.nombreMarca() == null ||
                dto.nombreMarca().isBlank()) {

            throw new BusinessRuleException(
                    "El nombre de la marca es obligatorio."
            );
        }

        if (dto.password() == null ||
                dto.password().isBlank()) {

            throw new BusinessRuleException(
                    "La contraseña es obligatoria."
            );
        }

        categoriaAliado categoriaEnum;

        try {

            categoriaEnum = categoriaAliado.valueOf(
                    dto.categoria()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException |
                 NullPointerException e) {

            throw new BusinessRuleException(
                    "La categoría del aliado no es válida."
            );
        }

        // Subir logo del aliado
        String logoUrl = null;

        if (logo != null && !logo.isEmpty()) {

            validarImagen(logo);

            logoUrl = supabaseStorageService.subirArchivo(
                    logo,
                    "aliados-logos"
            );
        }

        // Crear usuario asociado al aliado
        Usuario nuevoUsuario = Usuario.builder()
                .email(correo)
                .password(
                        passwordEncoder.encode(dto.password())
                )
                .rol(Rol.ALIADO)
                .build();

        Usuario usuarioGuardado =
                usuarioRepository.save(nuevoUsuario);

        // Crear perfil de marca
        Aliado nuevoAliado = Aliado.builder()
                .usuario(usuarioGuardado)
                .nombreAliado(dto.nombreMarca().trim())
                .CategoriaAliado(categoriaEnum)
                .correoContacto(correo)
                .logoURL(logoUrl)
                .estado(EstadoAliado.PENDIENTE)
                .build();

        Aliado aliadoGuardado =
                aliadoRepository.save(nuevoAliado);

        String tokenJwt =
                jwtService.generarToken(usuarioGuardado);

        return AliadoResponseDTO.desde(
                aliadoGuardado,
                tokenJwt
        );
    }

    // ==========================================
    // 3. LISTAR PUBLICACIONES DEL ALIADO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Recompensa> obtenerOfertasPorAliado(
            String correoAliado
    ) {

        Aliado aliado =
                obtenerAliadoPorCorreo(correoAliado);

        return recompensaRepository.findByAliado_Id(
                aliado.getId()
        );
    }

    // ==========================================
    // 4. ACTUALIZAR PERFIL Y LOGO
    // ==========================================

    @Override
    @Transactional
    public void actualizarPerfil(
            String correoAliado,
            PerfilAliadoDto dto,
            MultipartFile logo
    ) {

        Aliado aliado =
                obtenerAliadoPorCorreo(correoAliado);

        if (dto == null ||
                dto.nombreMarca() == null ||
                dto.nombreMarca().isBlank()) {

            throw new BusinessRuleException(
                    "El nombre de la marca es obligatorio."
            );
        }

        aliado.setNombreAliado(
                dto.nombreMarca().trim()
        );

        categoriaAliado categoria;

        try {

            categoria = categoriaAliado.valueOf(
                    dto.categoria()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException |
                 NullPointerException e) {

            throw new BusinessRuleException(
                    "La categoría seleccionada no es válida."
            );
        }

        aliado.setCategoriaAliado(categoria);

        // Guardar un nuevo logo, si se adjuntó
        if (logo != null && !logo.isEmpty()) {

            validarImagen(logo);

            String urlLogo =
                    supabaseStorageService.subirArchivo(
                            logo,
                            "aliados-logos"
                    );

            // Respeta el nombre del atributo logoUrl
            aliado.setLogoURL(urlLogo);
        }

        aliadoRepository.save(aliado);
    }

    // ==========================================
    // 5. OBTENER ALIADO DESDE SU JWT
    // ==========================================

    private Aliado obtenerAliadoPorCorreo(
            String correo
    ) {

        if (correo == null || correo.isBlank()) {
            throw new BusinessRuleException(
                    "No se pudo identificar al aliado."
            );
        }

        Usuario usuario = usuarioRepository
                .findByEmail(correo.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario aliado no encontrado"
                        )
                );

        return aliadoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un perfil de marca " +
                                        "para este usuario"
                        )
                );
    }

    // ==========================================
    // 6. VALIDAR ARCHIVOS DE IMAGEN
    // ==========================================

    private void validarImagen(
            MultipartFile imagen
    ) {

        if (imagen == null || imagen.isEmpty()) {
            throw new BusinessRuleException(
                    "La imagen está vacía."
            );
        }

        // Máximo 5 MB
        if (imagen.getSize() > 5L * 1024 * 1024) {
            throw new BusinessRuleException(
                    "La imagen no puede superar los 5 MB."
            );
        }

        String tipo = imagen.getContentType();

        if (tipo == null ||
                !(tipo.equals("image/jpeg") ||
                        tipo.equals("image/png") ||
                        tipo.equals("image/webp"))) {

            throw new BusinessRuleException(
                    "Solo se permiten imágenes JPG, PNG o WEBP."
            );
        }
    }
}
