package com.admin.remoto.services.persistence;

import com.admin.remoto.models.LogLote;
import com.admin.remoto.repositories.LogLoteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogLoteServiceImpl implements LogLoteService {

    private final LogLoteRepositorio logLoteRepository;

    @Autowired
    public LogLoteServiceImpl(LogLoteRepositorio logLoteRepository) {
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
    public List<LogLote> obtenerPorCliente(Long clienteId) {
        return logLoteRepository.findByUsuarioId(clienteId);
    }
}
