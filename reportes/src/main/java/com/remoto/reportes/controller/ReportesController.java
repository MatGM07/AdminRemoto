package com.remoto.reportes.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.remoto.reportes.models.LogEntry;
import com.remoto.reportes.models.LogLote;
import com.remoto.reportes.models.Usuario;
import com.remoto.reportes.services.LogLoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class ReportesController {

    private SessionManager sessionManager;
    private LogLoteService logLoteService;

    public ReportesController(SessionManager sessionManager, LogLoteService logLoteService){
        this.sessionManager = sessionManager;
        this.logLoteService = logLoteService;
    }

    @GetMapping("/reportes")
    public String mostrarReportes(Model model){
        Usuario current = sessionManager.getUsuario();
        List<LogLote> logs = logLoteService.obtenerPorCliente(current.getId());

        ObjectMapper mapper = new ObjectMapper();
        // Configurar ObjectMapper para trabajar con LocalDateTime
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        List<List<LogEntry>> logsParseados = new ArrayList<>();

        for (LogLote logLote : logs) {
            try {
                List<LogEntry> entries = mapper.readValue(logLote.getContenidoJson(),
                        new TypeReference<List<LogEntry>>() {});
                logsParseados.add(entries);
            } catch (Exception e) {
                e.printStackTrace();
                logsParseados.add(Collections.emptyList());
            }
        }

        model.addAttribute("logsParseados", logsParseados);
        return "reportes";
    }
}