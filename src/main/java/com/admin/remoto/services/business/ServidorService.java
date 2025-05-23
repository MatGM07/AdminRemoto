package com.admin.remoto.services.business;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.repositories.ServidorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServidorService {
    private final ServidorRepositorio servidorRepository;

    @Autowired
    public ServidorService(ServidorRepositorio servidorRepository) {
        this.servidorRepository = servidorRepository;
    }

    public List<Servidor> obtenerTodos() {
        return servidorRepository.findAll();
    }

    public Optional<Servidor> obtenerPorId(Long id) {
        return servidorRepository.findById(id);
    }

    public List<Servidor> obtenerPorUsuario(Usuario creador) {
        return servidorRepository.findByCreador(creador);
    }

    public Servidor guardar(Servidor servidor) {
        return servidorRepository.save(servidor);
    }

    public void eliminarPorId(Long id) {
        servidorRepository.deleteById(id);
    }
}

