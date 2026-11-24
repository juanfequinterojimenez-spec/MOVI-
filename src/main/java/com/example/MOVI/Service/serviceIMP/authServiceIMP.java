package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Config.JwtService;
import com.example.MOVI.DTO.ResponseDTO.*;
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

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class authServiceIMP implements authService {

    private final usuarioRepository usuarioRepository;
    private final IdentidadExternaRepository identidadExternaRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final SupabaseStorageService supabaseStorageService;
    private final VerificacionCorreoService verificacionCorreoService;

    @Override
    @Transactional
    public RegistroPendienteResponse registrar(registerRequest request, MultipartFile fotoDePerfil) {
        if (request == null) {
            throw new BusinessRuleException(
                    "Los datos del registro son obligatorios"
            );
        }

        // Validar campos obligatorios
        if (
                request.nombreCompleto() == null ||
                        request.nombreCompleto().isBlank() ||
                        request.correoElectronico() == null ||
                        request.correoElectronico().isBlank() ||
                        request.contrasena() == null ||
                        request.contrasena().isBlank() ||
                        request.tipoDocumento() == null ||
                        request.tipoDocumento().isBlank() ||
                        request.documentoIdentidad() == null ||
                        request.documentoIdentidad().isBlank()
        ) {
            throw new BusinessRuleException(
                    "Todos los campos son obligatorios"
            );
        }

        // Normalizar datos
        String nombre = request.nombreCompleto().trim();

        String email = request.correoElectronico()
                .trim()
                .toLowerCase(Locale.ROOT);

        String documento = request.documentoIdentidad().trim();

        String tipoDocumento = request.tipoDocumento()
                .trim()
                .toUpperCase(Locale.ROOT);

        // Validar correo
        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new BusinessRuleException(
                    "Ingresa un correo electrónico válido"
            );
        }

        // Validar tipo de documento
        if (!tipoDocumento.matches(
                "^(CEDULA_CIUDADANIA|CEDULA_EXTRANJERIA|PPT|PEP|PASAPORTE)$"
        )) {
            throw new BusinessRuleException(
                    "Tipo de documento inválido"
            );
        }

        // Evitar correos duplicados
        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "El correo electrónico ya está registrado"
            );
        }

        // Evitar documentos duplicados
        if (usuarioRepository.existsByDocumento(documento)) {
            throw new DuplicateResourceException(
                    "El documento de identidad ya está registrado"
            );
        }

        // Validar fotografía opcional
        String urlFotoPerfil = null;

        if (fotoDePerfil != null && !fotoDePerfil.isEmpty()) {

            validarFotografia(fotoDePerfil);

            urlFotoPerfil = supabaseStorageService.subirArchivo(
                    fotoDePerfil,
                    "avatares"
            );
        }

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setNombreCompleto(nombre);
        usuario.setEmail(email);
        usuario.setDocumento(documento);
        usuario.setTipoDocumento(tipoDocumento);

        usuario.setPassword(
                passwordEncoder.encode(request.contrasena())
        );

        usuario.setRol(Rol.PASAJERO);
        usuario.setPuntos(0);
        usuario.setActivo(true);
        usuario.setFotoPerfilURL(urlFotoPerfil);

        // La cuenta todavía no está verificada
        usuario.setCorreoVerificado(false);

        Usuario guardado = usuarioRepository.save(usuario);

        // Generar código y enviarlo mediante Brevo
        verificacionCorreoService.enviarCodigo(guardado);

        // No devolver JWT hasta verificar el correo
        return new RegistroPendienteResponse(
                "Te enviamos un código de verificación a tu correo",
                guardado.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public authResponse login(loginDTO request) {
        if (
                request == null ||
                        request.correoElectronico() == null ||
                        request.correoElectronico().isBlank() ||
                        request.contrasena() == null ||
                        request.contrasena().isBlank()
        ) {
            throw new BusinessRuleException(
                    "El correo y la contraseña son obligatorios"
            );
        }

        String email = request.correoElectronico()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Correo o contraseña incorrectos"
                ));

        if (!passwordEncoder.matches(
                request.contrasena(),
                usuario.getPassword()
        )) {
            throw new BadCredentialsException(
                    "Correo o contraseña incorrectos"
            );
        }

        // Verificar si el administrador bloqueó la cuenta
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessRuleException(
                    "Tu cuenta se encuentra deshabilitada"
            );
        }

        // No permitir login sin verificar correo
        if (!Boolean.TRUE.equals(
                usuario.getCorreoVerificado()
        )) {
            throw new BusinessRuleException(
                    "Debes verificar tu correo electrónico antes de iniciar sesión"
            );
        }

        String token = jwtService.generarToken(usuario);

        return new authResponse(
                token,
                usuarioResponseDTO.desde(usuario)
        );
    }

    @Override
    @Transactional
    public OAuth2Resultado loginOAuth2(String proveedor, String subject, String email, String nombre, boolean correoVerificado) {
        if (
                proveedor == null ||
                        proveedor.isBlank() ||
                        subject == null ||
                        subject.isBlank() ||
                        email == null ||
                        email.isBlank()
        ) {
            throw new BusinessRuleException(
                    "Los datos de autenticación externa son obligatorios"
            );
        }

        String emailNormalizado = email
                .trim()
                .toLowerCase(Locale.ROOT);

        ProveedorIdentidad proveedorIdentidad;

        try {
            proveedorIdentidad = ProveedorIdentidad.valueOf(
                    proveedor.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Proveedor de autenticación no permitido"
            );
        }

        // Buscar la identidad externa vinculada
        IdentidadExterna identidad =
                identidadExternaRepository
                        .findByProveedorAndSubject(
                                proveedorIdentidad,
                                subject
                        )
                        .orElse(null);

        Usuario usuario;

        if (identidad != null) {

            // Usuario OAuth existente
            usuario = identidad.getUsuario();

            // Solo actualizar si el proveedor acredita
            // de forma confiable la verificación del correo
            if (correoVerificado &&
                    !Boolean.TRUE.equals(
                            usuario.getCorreoVerificado()
                    )) {
                usuario.setCorreoVerificado(true);
                usuarioRepository.save(usuario);
            }

        } else {

            // Buscar cuenta con el mismo correo
            usuario = usuarioRepository
                    .findByEmail(emailNormalizado)
                    .orElse(null);

            if (usuario != null) {
                throw new BusinessRuleException(
                        "Ya existe una cuenta MOVI+ con este correo. " +
                                "Inicia sesión con tu contraseña y vincula " +
                                "tu cuenta externa."
                );
            }

            // Crear usuario OAuth
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
            usuario.setActivo(true);

            // La verificación proviene del proveedor
            usuario.setCorreoVerificado(correoVerificado);

            usuario = usuarioRepository.save(usuario);

            // Vincular identidad externa
            IdentidadExterna nuevaIdentidad =
                    new IdentidadExterna();

            nuevaIdentidad.setProveedor(proveedorIdentidad);
            nuevaIdentidad.setSubject(subject);
            nuevaIdentidad.setUsuario(usuario);

            identidadExternaRepository.save(
                    nuevaIdentidad
            );
        }


        // No permitir acceso a cuentas bloqueadas
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessRuleException(
                    "Tu cuenta se encuentra deshabilitada"
            );
        }

// Si necesita verificar el correo, enviar código
        if (!Boolean.TRUE.equals(
                usuario.getCorreoVerificado()
        )) {

            verificacionCorreoService.enviarCodigo(usuario);

            // No generar JWT todavía
            return OAuth2Resultado.pendiente(
                    usuario.getEmail()
            );
        }

// Cuenta verificada: generar JWT normalmente
        String token = jwtService.generarToken(usuario);

        authResponse respuesta = new authResponse(
                token,
                usuarioResponseDTO.desde(usuario)
        );

        return OAuth2Resultado.autenticado(
                usuario.getEmail(),
                respuesta
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

    // ==========================================
    // ACTUALIZAR FOTOGRAFÍA
    // ==========================================
    @Override
    @Transactional
    public usuarioResponseDTO actualizarFotoPerfil(Long usuarioId, MultipartFile foto) {
        validarFotografia(foto);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String urlFoto = supabaseStorageService.subirArchivo(
                foto, "avatares/" + usuario.getId()
        );
        usuario.setFotoPerfilURL(urlFoto);
        usuarioRepository.save(usuario);
        return usuarioResponseDTO.desde(usuario);
    }

    private void validarFotografia(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new BusinessRuleException("Selecciona una fotografía");
        }
        if (foto.getSize() > 5L * 1024 * 1024) {
            throw new BusinessRuleException("La fotografía no puede superar los 5 MB");
        }
        String tipo = foto.getContentType();
        if (tipo == null || !(tipo.equals("image/jpeg") ||
                tipo.equals("image/png") || tipo.equals("image/webp"))) {
            throw new BusinessRuleException("Formato no permitido. Utiliza JPG, PNG o WEBP");
        }
    }
}

