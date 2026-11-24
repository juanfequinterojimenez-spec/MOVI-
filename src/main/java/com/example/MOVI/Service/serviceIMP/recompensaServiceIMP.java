
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.OfertaResponseDTO;
import com.example.MOVI.Entity.Aliado;
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

    // ==========================================
    // 1. RECOMPENSAS DE UN ALIADO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> listarPorAliado(
            Long idAliado
    ) {

        if (idAliado == null || idAliado <= 0) {

            throw new BusinessRuleException(
                    "El ID del aliado no es válido."
            );
        }

        aliadoRepository.findById(idAliado)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aliado no encontrado con ID " +
                                        idAliado
                        )
                );

        return recompensaRepository
                .findByAliado_Id(idAliado)
                .stream()
                .map(this::convertirAOferta)
                .toList();
    }

    // ==========================================
    // 2. CATÁLOGO DE OFERTAS ACTIVAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> obtenerCatalogoOferta(
            String filtroCategoria
    ) {

        List<Recompensa> recompensas;

        if (filtroCategoria == null ||
                filtroCategoria.isBlank()) {

            recompensas =
                    recompensaRepository.findByActivaTrue();

        } else {

            categoriaAliado categoriaEnum;

            try {

                categoriaEnum = categoriaAliado.valueOf(
                        filtroCategoria
                                .trim()
                                .toUpperCase(Locale.ROOT)
                );

            } catch (IllegalArgumentException e) {

                throw new BusinessRuleException(
                        "Categoría de búsqueda inválida."
                );
            }

            recompensas = recompensaRepository
                    .findByActivaTrueAndAliado_CategoriaAliado(
                            categoriaEnum
                    );
        }

        return recompensas
                .stream()
                .map(this::convertirAOferta)
                .toList();
    }

    // ==========================================
    // 3. ALIAS PARA /api/recompensas/ofertas
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> obtenerCatalogoOfertas(
            String filtroCategoria
    ) {

        return obtenerCatalogoOferta(filtroCategoria);
    }

    // ==========================================
    // 4. CONVERTIR RECOMPENSA A DTO
    // ==========================================

    private OfertaResponseDTO convertirAOferta(
            Recompensa recompensa
    ) {

        Aliado aliadoEntidad = recompensa.getAliado();

        OfertaResponseDTO.AliadoResumenDTO aliado =
                new OfertaResponseDTO.AliadoResumenDTO(

                        // Nombre de la marca
                        aliadoEntidad.getNombreAliado(),

                        // Logo cargado por el aliado
                        aliadoEntidad.getLogoURL()
                );

        return new OfertaResponseDTO(

                recompensa.getIdRecompensa(),

                recompensa.getTitulo(),

                recompensa.getDescripcionRecompensa(),

                recompensa.getStockDisponible(),

                recompensa.getCostoPuntos(),

                // Imagen del cupón o producto
                recompensa.getImagenURL(),

                recompensa.getActiva(),

                // Marca y logo
                aliado
        );
    }
}
