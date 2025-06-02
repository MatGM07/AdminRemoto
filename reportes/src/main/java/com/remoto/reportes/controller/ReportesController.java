package com.remoto.reportes.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.remoto.reportes.models.LogEntry;
import com.remoto.reportes.models.LogLote;
import com.remoto.reportes.models.Usuario;
import com.remoto.reportes.models.Video;
import com.remoto.reportes.services.LogLoteService;
import com.remoto.reportes.services.VideoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Controller
public class ReportesController {

    private SessionManager sessionManager;
    private LogLoteService logLoteService;
    private VideoService videoService;


    public ReportesController(SessionManager sessionManager, LogLoteService logLoteService, VideoService videoService){
        this.sessionManager = sessionManager;
        this.logLoteService = logLoteService;
        this.videoService = videoService;
    }

    @GetMapping("/reportes/{idSesion}")
    public String mostrarReportes(@PathVariable Long idSesion, Model model){
        Usuario current = sessionManager.getUsuario();

        List<LogLote> logs = logLoteService.obtenerPorSesionCliente(idSesion,current.getId());

        ObjectMapper mapper = new ObjectMapper();
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

        List<Video> videos = videoService.obtenerPorSesionId(idSesion);
        if (!videos.isEmpty()) {
            Video video = videos.get(0);
            String base64Video = Base64.getEncoder().encodeToString(video.getData());
            model.addAttribute("videoBase64", base64Video);
        }

        model.addAttribute("logsParseados", logsParseados);
        return "reportes";
    }
}