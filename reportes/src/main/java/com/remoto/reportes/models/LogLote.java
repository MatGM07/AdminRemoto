package com.remoto.reportes.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "logs")
public class LogLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestampInicio;
    private LocalDateTime timestampFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id")
    private Sesion sesion;


    @Lob
    @Column(columnDefinition = "TEXT") // o JSON si tu MySQL lo permite
    private String contenidoJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestampInicio() {
        return timestampInicio;
    }

    public void setTimestampInicio(LocalDateTime timestampInicio) {
        this.timestampInicio = timestampInicio;
    }

    public LocalDateTime getTimestampFin() {
        return timestampFin;
    }

    public void setTimestampFin(LocalDateTime timestampFin) {
        this.timestampFin = timestampFin;
    }


    public String getContenidoJson() {
        return contenidoJson;
    }

    public void setContenidoJson(String contenidoJson) {
        this.contenidoJson = contenidoJson;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Sesion getSesion() {
        return sesion;
    }

    public void setSesion(Sesion sesion) {
        this.sesion = sesion;
    }
}
