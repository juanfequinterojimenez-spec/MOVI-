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

@Service
@RequiredArgsConstructor
public class aliadoServiceIMP implements aliadoService {

    private final usuarioRepository usuarioRepository;
    private final aliadoRepository aliadoRepository;
    private final recompensaRepository recompensaRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

     @Override
     @Transactional
    public Recompensa publicarOferta(String correoAliado, recompensaRequestDTO dto, MultipartFile imagen) {
        Aliado aliado = obtenerAliadoPorCorreo(correoAliado);

        // 2. Subir la imagen de la oferta a Supabase Storage (carpeta "ofertas")
        String urlImagenOferta = null;
        if (imagen != null && !imagen.isEmpty()) {
            urlImagenOferta = supabaseStorageService.subirArchivo(imagen, "ofertas");
        }

        // 3. Crear la entidad Recompensa incluyendo la URL
        Recompensa nuevaOferta = Recompensa.builder()
                .titulo(dto.titulo().trim())
                .descripcionRecompensa(dto.descripcion().trim())
                .costoPuntos(dto.costoPuntos())
                .stockDisponible(dto.stockDisponible())
                .imagenUrl(urlImagenOferta) // <-- Guardamos la URL generada
                .aliado(aliado)
                .activa(true)
                .build();

        // 4. Guardar y retornar
        return recompensaRepository.save(nuevaOferta);
    }

    @Override
    @Transactional
    public AliadoResponseDTO registrarAliado(AliadoRegistroDTO dto, MultipartFile logo) {
        String correo = dto.correo().toLowerCase().trim();
        // 1. Validar correo corporativo
        if (correo.endsWith("@gmail.com") ||
                correo.endsWith("@hotmail.com") ||
                correo.endsWith("@yahoo.com") ||
                correo.endsWith("@outlook.com")) {

            throw new BusinessRuleException(
                    "Los aliados deben registrarse utilizando el correo corporativo de su empresa."
            );
        }

        // 2. Verificar correo existente
        if (usuarioRepository.existsByEmail(correo)) {
            throw new BusinessRuleException(
                    "El correo electrónico ya se encuentra registrado."
            );
        }

        // 3. Validar categoría ANTES de guardar
        categoriaAliado categoriaEnum;

        try {
            categoriaEnum = categoriaAliado.valueOf(
                    dto.categoria().toUpperCase().trim()
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "La categoría especificada no es válida."
            );
        }

        // 4. Validar contraseña
        if (dto.password() == null || dto.password().isBlank()) {
            throw new BusinessRuleException("La contraseña es obligatoria.");
        }

        // 5. Subir logo a Supabase
        String logoUrl = null;

        if (logo != null && !logo.isEmpty()) {
            logoUrl = supabaseStorageService.subirArchivo(
                    logo,
                    "aliados-logos"
            );
        }

        // 6. Crear usuario
        Usuario nuevoUsuario = Usuario.builder()
                .email(correo)
                .password(passwordEncoder.encode(dto.password()))
                .rol(Rol.ALIADO)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 7. Crear perfil del aliado
        Aliado nuevoAliado = Aliado.builder()
                .usuario(usuarioGuardado)
                .nombreAliado(dto.nombreMarca().trim())
                .CategoriaAliado(categoriaEnum)
                .correoContacto(correo)
                .logoUrl(logoUrl)
                .build();

        Aliado aliadoGuardado = aliadoRepository.save(nuevoAliado);

        // 8. Generar JWT
        String tokenJwt = jwtService.generarToken(usuarioGuardado);

        return AliadoResponseDTO.desde(aliadoGuardado, tokenJwt);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Recompensa> obtenerOfertasPorAliado(String correoAliado) {
        Aliado aliado = obtenerAliadoPorCorreo(correoAliado);
        return recompensaRepository.findByAliado_Id(aliado.getId());
    }

    @Override
    @Transactional
    public void actualizarPerfil(String correoAliado, PerfilAliadoDto dto, MultipartFile logo) {
        Usuario usuario = usuarioRepository.findByEmail(correoAliado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario aliado no encontrado"));

        // 2. Buscar si el usuario ya tiene un perfil de Aliado creado, si no, crear la instancia
        Aliado aliado = aliadoRepository.findByUsuarioId(usuario.getId())
                .orElse(new Aliado());

        // 3. Aplicar los cambios SOBRE LA INSTANCIA (aliado en minúscula)
        aliado.setUsuario(usuario);
        aliado.setNombreAliado(dto.nombreMarca().trim());

        // 4. Convertir y asignar la categoría
        try {
            // Asegúrate de que enumCategoria coincida con el nombre exacto de tu Enum
            categoriaAliado enumCategoria = categoriaAliado.valueOf(dto.categoria().trim().toUpperCase());
            aliado.setCategoriaAliado(enumCategoria);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Categoría inválida. Debe ser una de las permitidas (ej. RESTAURANTE, CINE, ROPA, etc.)"
            );
        }

        // 5. Opcional: Subir el logo si lo incluyeron (requiere SupabaseStorageService)
        if (logo != null && !logo.isEmpty()) {
            // String urlLogo = supabaseStorageService.subirArchivo(logo, "logos-aliados");
            // aliado.setLogoUrl(urlLogo);
        }

        // 6. Guardar los cambios en la base de datos
        aliadoRepository.save(aliado);
    }

    private Aliado obtenerAliadoPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario aliado no encontrado"));

        return aliadoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se ha configurado el perfil de la marca para este usuario"));
    }
}


