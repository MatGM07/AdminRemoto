package com.admin.remoto.services.business;

import com.admin.remoto.models.Servidor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileSender {

    public FileSender() {
    }

    public void enviarArchivo(File file, Servidor servidor) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("El archivo no existe o es null.");
        }
        if (servidor == null
                || servidor.getDireccion() == null
                || servidor.getPuerto() == null) {
            throw new IllegalStateException("El servidor no está bien definido.");
        }

        String host = servidor.getDireccion();
        String puerto = servidor.getPuerto();
        String targetUrl = "http://" + host + ":" + puerto + "/upload";

        HttpPost post = new HttpPost(targetUrl);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                .build();

        post.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            client.execute(post, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Respuesta del servidor (" + targetUrl + "): " + statusCode);
                System.out.println("Cuerpo de la respuesta: " + responseBody);
                return null;
            });
        }
    }
}
