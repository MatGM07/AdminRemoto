package com.admin.remoto.repositories;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServidorRepositorio extends JpaRepository<Servidor, Long> {
    List<Servidor> findByCreador(Usuario usuario);
}
