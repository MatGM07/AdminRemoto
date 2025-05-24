package com.admin.remoto.Observador;

import com.admin.remoto.models.Evento;

public interface Observable<E, D> {
    void agregarObservador(Observador<E,D> observador);
    void eliminarObservador(Observador<E,D> observador);
    void notificarObservadores(E evento, D data);
}
