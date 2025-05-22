package com.admin.remoto.repositories;

import com.admin.remoto.models.LogLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogLoteRepositorio extends JpaRepository<LogLote, Long> {
    List<LogLote> findByUsuarioId(Long UsuarioId);
}
