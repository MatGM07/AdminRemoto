package com.admin.remoto.Observador;

import com.admin.remoto.models.Evento;

public interface Observable {
    void agregarObservador(Observador observador);
    void eliminarObservador(Observador observador);
    void notificarObservadores(Evento evento);
}
