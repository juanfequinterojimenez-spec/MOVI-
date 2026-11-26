
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Repository.recompensaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRecompensaAprobacionService {

    private final recompensaRepository recompensaRepository;

    @Transactional
    public void aprobar(Long idRecompensa) {

        if (idRecompensa == null || idRecompensa <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la recompensa no es válido."
            );
        }

        Recompensa recompensa = recompensaRepository
                .findById(idRecompensa)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la recompensa solicitada."
                        )
                );

        if (Boolean.TRUE.equals(recompensa.getActiva())) {
            return;
        }

        recompensa.setActiva(true);

        recompensaRepository.save(recompensa);
    }

    @Transactional
    public void desactivar(Long idRecompensa) {

        if (idRecompensa == null || idRecompensa <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la recompensa no es válido."
            );
        }

        Recompensa recompensa = recompensaRepository
                .findById(idRecompensa)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la recompensa solicitada."
                        )
                );

        recompensa.setActiva(false);

        recompensaRepository.save(recompensa);
    }
}

