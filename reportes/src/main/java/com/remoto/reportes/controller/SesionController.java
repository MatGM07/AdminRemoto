package com.remoto.reportes.controller;

import com.remoto.reportes.models.Sesion;
import com.remoto.reportes.services.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SesionController {

    @Autowired
    private final SessionManager sessionManager;
    @Autowired
    private final SesionService sesionService;

    public SesionController(SessionManager sessionManager, SesionService sesionService){
        this.sessionManager = sessionManager;
        this.sesionService = sesionService;
    }

    @GetMapping("/sesiones")
    public String obtenerSesiones(Model model){
        List<Sesion> sesiones = sesionService.obtenerPorUsuarioId(sessionManager.getUsuario().getId());
        model.addAttribute("sesiones",sesiones);

        return "sesiones";
    }
}
