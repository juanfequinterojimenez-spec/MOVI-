package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Entity.Recompensa;

import java.util.List;

public interface recompensaService {
    List<Recompensa> listarPorAliado(Long idAliado);
    List<Recompensa> obtenerCatalogo(String filtroCategoria);
}
