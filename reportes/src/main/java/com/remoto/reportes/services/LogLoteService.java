package com.remoto.reportes.services;

import com.remoto.reportes.models.LogLote;

import java.util.List;

public interface LogLoteService {
    LogLote guardarLote(LogLote logLote);
    List<LogLote> obtenerTodos();
    List<LogLote> obtenerPorCliente(Long clienteId);

}

