package com.remoto.reportes.services;


import com.remoto.reportes.models.LogLote;
import com.remoto.reportes.repositories.LogLoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogLoteServiceImpl implements LogLoteService {

    private final LogLoteRepository logLoteRepository;

    @Autowired
    public LogLoteServiceImpl(LogLoteRepository logLoteRepository) {
        this.logLoteRepository = logLoteRepository;
    }

    @Override
    public LogLote guardarLote(LogLote logLote) {
        return logLoteRepository.save(logLote);
    }

    @Override
    public List<LogLote> obtenerTodos() {
        return logLoteRepository.findAll();
    }

    @Override
    public List<LogLote> obtenerPorSesionCliente(Long idSesion, Long idUsuario) {
        return logLoteRepository.findBySesionIdAndUsuarioId(idSesion, idUsuario);
    }
}

