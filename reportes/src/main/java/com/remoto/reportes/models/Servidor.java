package com.remoto.reportes.models;

import jakarta.persistence.*;

@Entity
@Table(name = "servidores")
public class Servidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String direccion;
    private String puerto;

    @ManyToOne
    @JoinColumn(name = "usuario_id") // Clave foránea que referencia al usuario creador
    private Usuario creador;

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

    public Usuario getUsuario() {
        return creador;
    }

    public void setUsuario(Usuario creador) {
        this.creador = creador;
    }

    @Override
    public String toString() {
        return "Servidor: " + direccion + ":" + puerto;
    }
}
