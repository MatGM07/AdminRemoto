package com.admin.remoto.services.persistence;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.repositories.ServidorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface ServidorService {
    List<Servidor> obtenerTodos();
    Optional<Servidor> obtenerPorId(Long id);
    List<Servidor> obtenerPorUsuario(Usuario creador);
    Servidor guardar(Servidor servidor);
    void eliminarPorId(Long id);
}

