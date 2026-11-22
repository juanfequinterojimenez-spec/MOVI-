package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.OfertaResponseDTO;
import com.example.MOVI.Entity.Recompensa;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface recompensaService {

        List<OfertaResponseDTO> listarPorAliado(Long idAliado);

        List<OfertaResponseDTO> obtenerCatalogoOferta(
                String filtroCategoria
        );

        List<OfertaResponseDTO> obtenerCatalogoOfertas(
                String filtroCategoria
        );
}

