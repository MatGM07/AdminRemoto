package com.admin.remoto.controllers;

import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.UsuarioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario) {
        try {
            usuarioServicio.registrarUsuario(usuario);
            return "redirect:/login";  // Redirige a login después del registro
        } catch (IllegalArgumentException e) {
            // Aquí se maneja el caso si el correo ya existe (se podría mostrar un mensaje)
            return "redirect:/registro?error";  // Redirige de nuevo al registro si hay error
        }
    }

    @GetMapping("/login")
    public String mostrarFormularioDeLogin() {
        return "login";
    }

    @GetMapping("/chat")
    public String mostrarChat() {
        return "chat";
    }

}
