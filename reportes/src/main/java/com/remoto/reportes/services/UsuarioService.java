package com.remoto.reportes.services;

import com.remoto.reportes.models.Usuario;

public interface UsuarioService {
    boolean registrarUsuario(Usuario usuario);
    boolean existeUsuario(String username);
}
