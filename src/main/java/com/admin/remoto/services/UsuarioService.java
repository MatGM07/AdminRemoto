package com.admin.remoto.services;

import com.admin.remoto.models.Usuario;
import com.admin.remoto.repositories.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public void registrarUsuario(Usuario usuario) {

        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));
        usuarioRepositorio.save(usuario);
    }

    public Optional<Usuario> encontrarPorNombre(String nombre) {
        return usuarioRepositorio.findByNombre(nombre);
    }
}
