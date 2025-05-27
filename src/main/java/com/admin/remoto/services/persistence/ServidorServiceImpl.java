package com.admin.remoto.services.persistence;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.repositories.ServidorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServidorServiceImpl implements ServidorService {

    private final ServidorRepositorio servidorRepository;

    @Autowired
    public ServidorServiceImpl(ServidorRepositorio servidorRepository) {
        this.servidorRepository = servidorRepository;
    }

    @Override
    public List<Servidor> obtenerTodos() {
        return servidorRepository.findAll();
    }

    @Override
    public Optional<Servidor> obtenerPorId(Long id) {
        return servidorRepository.findById(id);
    }

    @Override
    public List<Servidor> obtenerPorUsuario(Usuario creador) {
        return servidorRepository.findByCreador(creador);
    }

    @Override
    public Servidor guardar(Servidor servidor) {
        return servidorRepository.save(servidor);
    }

    @Override
    public void eliminarPorId(Long id) {
        servidorRepository.deleteById(id);
    }
}
