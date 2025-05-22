package com.remoto.reportes.controller;

import com.remoto.reportes.models.Usuario;
import com.remoto.reportes.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private final UsuarioService usuarioService;

    public RegisterController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, Model model) {
        if (!usuarioService.registrarUsuario(usuario)) {
            model.addAttribute("error", "El nombre de usuario ya está en uso");
            return "registro";
        }

        return "redirect:/login?registroExitoso";
    }
}
