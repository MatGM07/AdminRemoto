package com.admin.remoto.services.persistence;

import com.admin.remoto.models.Usuario;
import com.admin.remoto.repositories.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> obtenerPorNombre(String nombre) {
        return usuarioRepositorio.findByNombre(nombre);
    }

    public Usuario verificarCredenciales(String nombre, String contraseña) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(nombre, contraseña)
            );


            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            return usuarioRepositorio.findByNombre(userDetails.getUsername()).get();

        } catch (AuthenticationException ex) {
            return null;
        }
    }

    public boolean existeUsuario(String nombre) {
        return usuarioRepositorio.existsByNombre(nombre);
    }

    public Optional<Usuario> obtenerPorId(Long id){
        return usuarioRepositorio.findById(id);
    }

    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // esto obtiene el username (por defecto, el nombre de usuario)
        return usuarioRepositorio.findByNombre(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Optional<Usuario> encontrarPorNombre(String nombre) {
        return usuarioRepositorio.findByNombre(nombre);
    }
}