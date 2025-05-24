package com.admin.remoto.Observador;

import com.admin.remoto.models.Evento;

public interface Observador<E,D> {
    void actualizar(E evento, D Object);

}
