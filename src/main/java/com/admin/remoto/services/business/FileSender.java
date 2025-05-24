package com.admin.remoto.services.business;

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

    private final SessionManager sessionManager;

    @Autowired
    public FileSender(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void enviarArchivo(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("El archivo no existe o es null.");
        }

        if (sessionManager.getServidor() == null) {
            throw new IllegalStateException("No hay servidor seleccionado en la sesión.");
        }

        String host = sessionManager.getServidor().getDireccion();
        String puerto = sessionManager.getServidor().getPuerto();
        String targetUrl = "http://" + host + ":" + puerto + "/upload";
        System.out.println(targetUrl);

        HttpPost post = new HttpPost(targetUrl);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                .build();

        post.setEntity(entity);

        System.out.println(">>> [DEBUG] Content-Type del request: " + entity.getContentType());

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            client.execute(post, response -> {
                int statusCode = response.getCode();
                System.out.println("Respuesta del servidor: " + statusCode);
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Cuerpo de la respuesta: " + responseBody);
                return null;
            });
        }
    }
}
