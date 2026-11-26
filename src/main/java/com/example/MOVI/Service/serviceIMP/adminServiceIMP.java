
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.AdminEstadisticasDTO;
import com.example.MOVI.DTO.ResponseDTO.AdminUsuarioDTO;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.EstadoCanje;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.canjeRepository;
import com.example.MOVI.Repository.usuarioRepository;
import com.example.MOVI.Repository.viajeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class adminServiceIMP implements adminService {

    private final usuarioRepository usuarioRepository;
    private final viajeRepository viajeRepository;
    private final canjeRepository canjeRepository;

    private static final Locale LOCALE_NUMERICO =
            Locale.forLanguageTag("es-CO");

    @Override
    @Transactional(readOnly = true)
    public AdminEstadisticasDTO obtenerEstadisticas() {

        long viajesSimulados = viajeRepository.count();

        Long puntosEmitidos =
                viajeRepository.sumarPuntosGenerados();

        if (puntosEmitidos == null) {
            puntosEmitidos = 0L;
        }

        long canjesRealizados =
                canjeRepository.countByEstado(
                        EstadoCanje.UTILIZADO
                );

        NumberFormat formato =
                NumberFormat.getIntegerInstance(
                        LOCALE_NUMERICO
                );

        return new AdminEstadisticasDTO(
                formato.format(viajesSimulados),
                formato.format(puntosEmitidos),
                formato.format(canjesRealizados),
                0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUsuarioDTO> listarUsuarios() {

        return usuarioRepository.findAll()
                .stream()
                .map(AdminUsuarioDTO::desde)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleBloqueoUsuario(Long usuarioId) {

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado con id "
                                        + usuarioId
                        )
                );

        boolean activoActual =
                Boolean.TRUE.equals(usuario.getActivo());

        usuario.setActivo(!activoActual);

        usuarioRepository.save(usuario);
    }
}
