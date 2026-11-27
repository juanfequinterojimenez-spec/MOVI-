package com.example.MOVI.Repository;

import com.example.MOVI.Entity.SesionPersistente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface SesionPersistenteRepository extends JpaRepository<SesionPersistente, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SesionPersistente> findByTokenHash(String tokenHash);
}
