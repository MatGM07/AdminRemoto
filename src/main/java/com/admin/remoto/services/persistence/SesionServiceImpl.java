package com.admin.remoto.services.persistence;


import com.admin.remoto.models.Sesion;
import com.admin.remoto.repositories.SesionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SesionServiceImpl implements SesionService {

    private final SesionRepositorio sesionRepository;

    @Autowired
    public SesionServiceImpl(SesionRepositorio sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Override
    public List<Sesion> obtenerTodas() {
        return sesionRepository.findAll();
    }

    @Override
    public Optional<Sesion> obtenerPorId(Long id) {
        return sesionRepository.findById(id);
    }

    @Override
    public List<Sesion> obtenerPorUsuarioId(Long usuarioId) {
        return sesionRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Sesion actualizar(Long id, Sesion sesionActualizada) {
        return sesionRepository.findById(id).map(sesionExistente -> {
            sesionExistente.setFechaHoraInicio(sesionActualizada.getFechaHoraInicio());
            sesionExistente.setFechaHoraFin(sesionActualizada.getFechaHoraFin());
            sesionExistente.setUsuario(sesionActualizada.getUsuario());
            sesionExistente.setServidor(sesionActualizada.getServidor());
            return sesionRepository.save(sesionExistente);
        }).orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + id));
    }

    @Override
    public Sesion guardar(Sesion sesion) {
        return sesionRepository.save(sesion);
    }

    @Override
    public void eliminar(Long id) {
        sesionRepository.deleteById(id);
    }
}
