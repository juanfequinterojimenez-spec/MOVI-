package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Config.JwtService;
import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.DTO.ResponseDTO.loginDTO;
import com.example.MOVI.DTO.ResponseDTO.registerRequest;
import com.example.MOVI.DTO.ResponseDTO.usuarioResponseDTO;
import com.example.MOVI.Entity.IdentidadExterna;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.ProveedorIdentidad;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.DuplicateResourceException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.IdentidadExternaRepository;
import com.example.MOVI.Repository.usuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;


@Service
@RequiredArgsConstructor
public class authServiceIMP implements authService {

    private final usuarioRepository usuarioRepository;
    private final IdentidadExternaRepository identidadExternaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SupabaseStorageService supabaseStorageService;

    //poner foto de perfil
    @Override
    @Transactional
    public authResponse registrar(registerRequest request, MultipartFile fotoDePerfil) {
        if (request == null) {
            throw new BusinessRuleException("Los datos del registro son obligatorios");
        }

        // ... (tus validaciones previas de campos obligatorios, email y documento) ...

        String email = request.correoElectronico().trim().toLowerCase();
        String documento = request.documentoIdentidad().trim();
        String tipoDocumento = request.tipoDocumento().trim().toUpperCase();

        if (!tipoDocumento.matches("^(CEDULA_CIUDADANIA|CEDULA_EXTRANJERIA|PPT|PEP|PASAPORTE)$")) {
            throw new BusinessRuleException("Tipo de documento inválido. Opciones permitidas: CEDULA_CIUDADANIA, CEDULA_EXTRANJERIA, PPT, PEP, PASAPORTE");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("El correo electrónico ya está registrado");
        }

        if (usuarioRepository.existsByDocumento(documento)) {
            throw new DuplicateResourceException("El documento de identidad ya está registrado");
        }

        // Subir foto de perfil a Supabase Storage si el usuario la adjuntó
        String urlFotoPerfil = null;
        if (fotoDePerfil != null && !fotoDePerfil.isEmpty()) {
            urlFotoPerfil = supabaseStorageService.subirArchivo(fotoDePerfil, "avatares");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setDocumento(documento);
        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.contrasena()));
        usuario.setPuntos(0);
        usuario.setRol(Rol.PASAJERO);
        usuario.setFotoPerfilURL(urlFotoPerfil); // <-- Guardar la URL generada por Supabase

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(guardado);

        return new authResponse(token, usuarioResponseDTO.desde(guardado));
    }


    // login
    @Override
@Transactional(readOnly = true)
public authResponse login(loginDTO request) {

    if (request == null ||
            request.correoElectronico() == null ||
            request.correoElectronico().isBlank() ||
            request.contrasena() == null ||
            request.contrasena().isBlank()) {

        throw new BusinessRuleException(
                "El correo y la contraseña son obligatorios"
        );
    }

    String email = request.correoElectronico()
            .trim()
            .toLowerCase();

    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException(
                    "Correo o contraseña incorrectos"
            ));

    if (!passwordEncoder.matches(
            request.contrasena(),
            usuario.getPassword())) {

        throw new BadCredentialsException(
                "Correo o contraseña incorrectos"
        );
    }

    String token = jwtService.generarToken(usuario);

    return new authResponse(
            token,
            usuarioResponseDTO.desde(usuario)
    );
}

// ==========================================
// LOGIN GOOGLE / MICROSOFT
// ==========================================

@Override
@Transactional
public authResponse loginOAuth2(
        String proveedor,
        String subject,
        String email,
        String nombre) {

    if (proveedor == null || proveedor.isBlank() ||
            subject == null || subject.isBlank() ||
            email == null || email.isBlank()) {

        throw new BusinessRuleException(
                "Los datos de autenticación externa son obligatorios"
        );
    }

    String emailNormalizado = email.trim().toLowerCase();

    ProveedorIdentidad proveedorIdentidad;

    try {
        proveedorIdentidad = ProveedorIdentidad.valueOf(
                proveedor.trim().toUpperCase()
        );
    } catch (IllegalArgumentException e) {
        throw new BusinessRuleException(
                "Proveedor de autenticación no permitido"
        );
    }

    // Buscar identidad externa
    IdentidadExterna identidad =
            identidadExternaRepository
                    .findByProveedorAndSubject(
                            proveedorIdentidad,
                            subject
                    )
                    .orElse(null);

    Usuario usuario;

    if (identidad != null) {

        // La identidad ya está vinculada
        usuario = identidad.getUsuario();

    } else {

        // Buscar usuario por correo
        usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElse(null);

        if (usuario != null) {

            throw new BusinessRuleException(
                    "Ya existe una cuenta MOVI con este correo. " +
                            "Inicia sesión con tu contraseña y realiza " +
                            "la vinculación de tu cuenta externa."
            );
        }

        // Crear usuario nuevo
        usuario = new Usuario();

        usuario.setNombreCompleto(
                nombre != null && !nombre.isBlank()
                        ? nombre.trim()
                        : "Usuario MOVI"
        );

        usuario.setEmail(emailNormalizado);

        // Documento provisional
        usuario.setDocumento(
                "PENDIENTE-" + UUID.randomUUID()
        );

        // Contraseña aleatoria encriptada
        usuario.setPassword(
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                )
        );

        usuario.setRol(Rol.PASAJERO);
        usuario.setPuntos(0);

        usuario = usuarioRepository.save(usuario);

        // Registrar identidad externa
        IdentidadExterna nuevaIdentidad =
                new IdentidadExterna();

        nuevaIdentidad.setProveedor(proveedorIdentidad);
        nuevaIdentidad.setSubject(subject);
        nuevaIdentidad.setUsuario(usuario);

        identidadExternaRepository.save(nuevaIdentidad);
    }

    // Generar JWT de MOVI
    String token = jwtService.generarToken(usuario);

    return new authResponse(
            token,
            usuarioResponseDTO.desde(usuario)
    );
}

// ==========================================
// CONSULTAR PERFIL
// ==========================================

@Override
@Transactional(readOnly = true)
public usuarioResponseDTO perfil(Long usuarioId) {

    Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Usuario no encontrado con id " + usuarioId
            ));

    return usuarioResponseDTO.desde(usuario);
   }


    @Override
    @Transactional
    public usuarioResponseDTO actualizarFotoPerfil(Long usuarioId, MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new BusinessRuleException("Selecciona una fotografía");
        }
        if (foto.getSize() > 5L * 1024 * 1024) {
            throw new BusinessRuleException("La fotografía debe pesar menos de 5 MB");
        }
        String tipo = foto.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") || tipo.equals("image/png") || tipo.equals("image/webp"))) {
            throw new BusinessRuleException("Formato no permitido. Utiliza JPG, PNG o WEBP");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String url = supabaseStorageService.subirArchivo(foto, "avatares/" + usuario.getId());
        usuario.setFotoPerfilURL(url);
        usuarioRepository.save(usuario);
        return usuarioResponseDTO.desde(usuario);
    }
}
