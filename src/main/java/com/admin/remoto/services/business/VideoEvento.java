package com.admin.remoto.services.business;

import com.admin.remoto.models.Sesion;
import com.admin.remoto.websocket.Evento;


public class VideoEvento extends Evento {
    public enum TipoVideo { GUARDADO }

    private final Long videoId;
    private final TipoVideo tipoVideo;
    private final Sesion sesion;  // ✅ Nueva propiedad

    public VideoEvento(Long videoId, Sesion sesion) {
        super(Evento.Tipo.BINARY, videoId);
        this.videoId = videoId;
        this.tipoVideo = TipoVideo.GUARDADO;
        this.sesion = sesion;
    }

    public Long getVideoId() {
        return videoId;
    }

    public TipoVideo getTipoVideo() {
        return tipoVideo;
    }

    public Sesion getSesion() {
        return sesion;
    }
}