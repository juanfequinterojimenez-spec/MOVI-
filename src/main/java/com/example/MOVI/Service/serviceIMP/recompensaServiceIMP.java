package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.OfertaResponseDTO;
import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Enum.categoriaAliado;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.aliadoRepository;
import com.example.MOVI.Repository.recompensaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class recompensaServiceIMP implements recompensaService {

    private final recompensaRepository recompensaRepository;
    private final aliadoRepository aliadoRepository;

    // Listar las recompensas asociadas a un aliado
    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> listarPorAliado(Long idAliado) {

        if (idAliado == null || idAliado <= 0) {
            throw new BusinessRuleException(
                    "El ID del aliado no es válido"
            );
        }

        aliadoRepository.findById(idAliado)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aliado no encontrado con id " + idAliado
                        )
                );

        return recompensaRepository.findByAliado_Id(idAliado)
                .stream()
                .map(this::convertirAOferta)
                .toList();
    }

    // Consultar recompensas activas con filtro opcional
    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> obtenerCatalogoOferta(
            String filtroCategoria
    ) {

        List<Recompensa> recompensas;

        if (filtroCategoria == null || filtroCategoria.isBlank()) {
            recompensas = recompensaRepository.findByActivaTrue();
        } else {
            try {
                categoriaAliado categoriaEnum =
                        categoriaAliado.valueOf(
                                filtroCategoria.trim()
                                        .toUpperCase(Locale.ROOT)
                        );

                recompensas =
                        recompensaRepository
                                .findByActivaTrueAndAliado_CategoriaAliado(
                                        categoriaEnum
                                );

            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException(
                        "Categoría de búsqueda inválida"
                );
            }
        }

        return recompensas.stream()
                .map(this::convertirAOferta)
                .toList();
    }

    // Alias para el endpoint /ofertas
    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> obtenerCatalogoOfertas(
            String filtroCategoria
    ) {
        return obtenerCatalogoOferta(filtroCategoria);
    }

    // Conversión de entidad JPA a DTO
    private OfertaResponseDTO convertirAOferta(Recompensa recompensa) {

        OfertaResponseDTO.AliadoResumenDTO aliado =
                new OfertaResponseDTO.AliadoResumenDTO(
                        recompensa.getAliado().getNombreAliado()
                );

        return new OfertaResponseDTO(
                recompensa.getIdRecompensa(),
                recompensa.getTitulo(),
                recompensa.getDescripcionRecompensa(),
                recompensa.getStockDisponible(),
                recompensa.getCostoPuntos(),
                recompensa.getImagenUrl(),
                recompensa.getActiva(),
                aliado
        );
    }
}
