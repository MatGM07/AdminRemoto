package com.admin.remoto.models;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String contraseña;

    @OneToMany(mappedBy = "creador", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Servidor> servidoresCreados = new HashSet<>();

    public Usuario() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contrasena) {
        this.contraseña = contrasena;
    }

    public Set<Servidor> getServidoresCreados() {
        return servidoresCreados;
    }

    public void setServidoresCreados(Set<Servidor> servidoresCreados) {
        this.servidoresCreados = servidoresCreados;
    }

    // Métodos helper para manejar la relación (opcional pero recomendado)
    public void agregarServidor(Servidor servidor) {
        servidor.setUsuario(this);
        this.servidoresCreados.add(servidor);
    }

    public void removerServidor(Servidor servidor) {
        servidor.setUsuario(null);
        this.servidoresCreados.remove(servidor);
    }
}