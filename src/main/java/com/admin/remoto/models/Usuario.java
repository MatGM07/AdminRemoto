package com.admin.remoto.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String contraseña;


    public Usuario(Long id, String nombre, String contraseña) {
        this.id = id;
        this.nombre = nombre;
        this.contraseña = contraseña;
    }

    public Usuario() {}

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
}
