package com.example.MOVI.Repository;

import com.example.MOVI.Entity.SesionPersistente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SesionPersistenteRepository extends JpaRepository<SesionPersistente, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SesionPersistente> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update SesionPersistente s set s.revocadaEn = :ahora where s.usuario.id = :id and s.revocadaEn is null")
    int revocarTodasDeUsuario(@Param("id") Long id, @Param("ahora") Instant ahora);
}
