package com.admin.remoto.grabacion;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfiguracionGrabacion {
    // Parámetros configurables
    private String rutaArchivo;
    private int duracionEnSegundos;
    private int frameRate;

    // Constructor con generación automática de nombre
    public ConfiguracionGrabacion(String directorio, int duracionEnSegundos, int frameRate) {
        this.rutaArchivo = generarRutaConTimestamp(directorio);
        this.duracionEnSegundos = duracionEnSegundos;
        this.frameRate = frameRate;
    }

    // Método privado para generar la ruta con timestamp
    private String generarRutaConTimestamp(String directorio) {
        // Asegurar que el directorio termine con /
        if (!directorio.endsWith("/") && !directorio.endsWith("\\")) {
            directorio += "/";
        }
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return directorio + "grabacion_" + timestamp + ".mp4";
    }

    // Getters (opcional: setters si necesitas modificar valores después)
    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public int getDuracionEnSegundos() {
        return duracionEnSegundos;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public void setDuracionEnSegundos(int duracionEnSegundos) {
        this.duracionEnSegundos = duracionEnSegundos;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }
}



/*      ASI DEBE IR EL MAIN, TRY CATCH PARA LAS EXCEPCIONES :3

       // 1. Configuración simple
        ConfiguracionGrabacion config = new ConfiguracionGrabacion(
            "videos_clientes",  // Solo el directorio
            10,                // Duración en segundos
            15                 // FPS
        );

        // 2. Crear carpeta si no existe
        new File("videos_clientes").mkdirs();

        // 3. Iniciar grabación
        try {
            System.out.println("Iniciando grabación en: " + config.getRutaArchivo());
            File video = GrabadorPantalla.grabarPantalla(config.getRutaArchivo(), config.getDuracionEnSegundos());
            System.out.println("¡Grabación guardada en:\n" + video.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error al grabar:");
            e.printStackTrace();
        }
            
 */