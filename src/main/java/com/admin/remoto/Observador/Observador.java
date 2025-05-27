package com.admin.remoto.Observador;

public interface Observador<E,D> {
    void actualizar(E evento, D Object);

}
