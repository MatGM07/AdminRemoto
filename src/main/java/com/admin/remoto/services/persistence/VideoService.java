package com.admin.remoto.services.persistence;


import com.admin.remoto.Observador.Observable;
import com.admin.remoto.models.Video;
import com.admin.remoto.services.connection.Evento;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService extends Observable<Evento, Void> {
    /**
     * Persiste en BD y disco el archivo de video recibido.
     *
     * @param file MultipartFile con el video subido.
     * @return la entidad Video que se guardó en la BD.
     * @throws Exception si ocurre algún problema al leer o guardar.
     */
    Video saveVideo(MultipartFile file) throws Exception;


}
