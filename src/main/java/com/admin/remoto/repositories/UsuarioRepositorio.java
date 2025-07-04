package com.admin.remoto.repositories;

import com.admin.remoto.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}