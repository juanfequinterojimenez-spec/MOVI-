
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.ElegibilidadResponse;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.TipoInstitucion;
import com.example.MOVI.Repository.DominioInstitucionalRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ElegibilidadService {

    private final DominioInstitucionalRepository
            dominioRepository;

    public ElegibilidadResponse evaluar(Usuario usuario) {

        boolean correoVerificado =
                Boolean.TRUE.equals(
                        usuario.getCorreoVerificado()
                );

        if (!correoVerificado) {
            return personal(false);
        }

        String email = usuario.getEmail();

        if (email == null || email.isBlank()) {
            return personal(true);
        }

        String emailNormalizado =
                email.trim().toLowerCase(Locale.ROOT);

        int arroba = emailNormalizado.lastIndexOf('@');

        if (arroba <= 0 ||
                arroba == emailNormalizado.length() - 1 ||
                emailNormalizado.indexOf('@') != arroba) {
            return personal(true);
        }

        String dominio =
                emailNormalizado.substring(arroba + 1);

        return dominioRepository
                .findByDominioIgnoreCaseAndActivoTrueAndVerificadoTrue(
                        dominio
                )
                .map(institucion -> {

                    boolean universidad =
                            institucion.getTipo()
                                    == TipoInstitucion.UNIVERSIDAD;

                    boolean empresa =
                            institucion.getTipo()
                                    == TipoInstitucion.EMPRESA;

                    return new ElegibilidadResponse(
                            universidad
                                    ? "INSTITUCIONAL"
                                    : "EMPRESARIAL",
                            institucion.getNombreInstitucion(),
                            true,
                            true,
                            false,
                            empresa
                    );
                })
                .orElseGet(() -> personal(true));
    }

    private ElegibilidadResponse personal(
            boolean correoVerificado
    ) {
        return new ElegibilidadResponse(
                "PERSONAL",
                null,
                correoVerificado,
                false,
                false,
                false
        );
    }
}
