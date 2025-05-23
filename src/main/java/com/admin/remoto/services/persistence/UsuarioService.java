package com.admin.remoto.services.persistence;

import com.admin.remoto.models.Usuario;

import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> obtenerPorNombre(String nombre);
    Usuario verificarCredenciales(String nombre, String contraseña);
    boolean existeUsuario(String nombre);
    Optional<Usuario> obtenerPorId(Long id);
    Usuario getUsuarioActual();
    Optional<Usuario> encontrarPorNombre(String nombre);
}