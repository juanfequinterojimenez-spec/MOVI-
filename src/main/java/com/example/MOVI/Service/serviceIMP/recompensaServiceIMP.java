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

    @Override
    @Transactional(readOnly = true)
    public List<Recompensa> listarPorAliado(Long idAliado) {

        if (idAliado == null || idAliado <= 0) {
            throw new BusinessRuleException(
                    "El ID del aliado no es válido"
            );
        }

        aliadoRepository.findById(idAliado)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aliado no encontrado con id " + idAliado
                ));

        return recompensaRepository.findByAliado_Id(idAliado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recompensa> obtenerCatalogo(String filtroCategoria) {

        if (filtroCategoria == null ||
                filtroCategoria.isBlank()) {
            return recompensaRepository.findByActivaTrue();
        }

        try {
            categoriaAliado categoriaEnum =
                    categoriaAliado.valueOf(
                            filtroCategoria.trim()
                                    .toUpperCase(Locale.ROOT)
                    );

            return recompensaRepository
                    .findByActivaTrueAndAliado_CategoriaAliado(
                            categoriaEnum
                    );

        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Categoría de búsqueda inválida"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> obtenerCatalogoOfertas(
            String filtroCategoria
    ) {

        List<Recompensa> recompensas =
                obtenerCatalogo(filtroCategoria);

        return recompensas.stream()
                .map(this::convertirAOferta)
                .toList();
    }

    private OfertaResponseDTO convertirAOferta(
            Recompensa recompensa
    ) {

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
