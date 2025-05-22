package com.remoto.reportes.services;

import com.remoto.reportes.models.Usuario;
import com.remoto.reportes.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean registrarUsuario(Usuario usuario) {
        if (usuarioRepository.findByNombre(usuario.getNombre()).isPresent()) {
            return false;
        }
        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));
        usuarioRepository.save(usuario);
        return true;
    }

    @Override
    public boolean existeUsuario(String username) {
        return usuarioRepository.findByNombre(username) != null;
    }
}
