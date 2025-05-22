package com.admin.remoto.services;

import com.admin.remoto.models.LogLote;

import java.time.LocalDateTime;
import java.util.List;

public interface LogLoteService {
    LogLote guardarLote(LogLote logLote);
    List<LogLote> obtenerTodos();
    List<LogLote> obtenerPorCliente(Long clienteId);
}
