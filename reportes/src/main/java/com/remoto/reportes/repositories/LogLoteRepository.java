package com.remoto.reportes.repositories;

import com.remoto.reportes.models.LogLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogLoteRepository extends JpaRepository<LogLote,Long> {
    List<LogLote> findByUsuarioId(Long UsuarioId);
}
