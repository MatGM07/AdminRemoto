package com.admin.remoto.websocket;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class WebSocketClientApplication implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        // Si necesitas subprotocolos o auth, añádelos a `headers`

        CompletableFuture<WebSocketSession> future =
                client.execute(
                        new TextWebSocketHandler() {
                            @Override
                            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                                System.out.println("Conectado al servidor JavaFX!");
                                // Ejemplo de envío
                                session.sendMessage(new TextMessage(
                                        "{\"sender\":\"SpringBoot\",\"content\":\"¡Hola!\",\"timestamp\":\""
                                                + java.time.LocalDateTime.now() + "\"}"
                                ));
                            }

                            @Override
                            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                                System.out.println("Recibido: " + message.getPayload());
                            }
                        },
                        headers,
                        URI.create("ws://26.149.149.83:8081/ws")
                );

        future
                .thenAccept(session -> {
                    // Ya conectado: puedes guardar `session` para más envíos
                })
                .exceptionally(ex -> {
                    System.err.println("Falló la conexión: " + ex.getMessage());
                    return null;
                });
    }
}