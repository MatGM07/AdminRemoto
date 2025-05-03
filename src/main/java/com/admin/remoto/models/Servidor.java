package com.admin.remoto.models;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "servidores")
public class Servidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String direccion;
    private String puerto;

    public Servidor(Long id, String direccion, String puerto, Set<Usuario> usuarios) {
        this.id = id;
        this.direccion = direccion;
        this.puerto = puerto;
        this.usuarios = usuarios;
    }

    public Servidor() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    @ManyToMany
    @JoinTable(
            name = "usuario_servidor",
            joinColumns = @JoinColumn(name = "servidor_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> usuarios = new HashSet<>();
}
