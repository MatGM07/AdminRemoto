package com.admin.remoto.grabacion;

public class ConfiguracionGrabacion {
    // Parámetros configurables
    private String rutaArchivo;
    private int duracionEnSegundos;
    private int frameRate;

    // Constructor
    public ConfiguracionGrabacion(String rutaArchivo, int duracionEnSegundos, int frameRate) {
        this.rutaArchivo = rutaArchivo;
        this.duracionEnSegundos = duracionEnSegundos;
        this.frameRate = frameRate;
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
}



/*      ASI DEBE IR EL MAIN, TRY CATCH PARA LAS EXCEPCIONES :3

       // Configuración centralizada
        ConfiguracionGrabacion config = new ConfiguracionGrabacion(
            "videos_cliente/grabacion_pantalla.mp4", // Ruta + nombre de archivo
            10,                      // Duración en segundos
            10                       // Frame rate (FPS)
        );

        try {
            System.out.println("Iniciando grabación...");
            File videoGrabado = GrabadorPantalla.grabarPantalla(config);
            System.out.println("Grabación guardada en: " + videoGrabado.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error al grabar:");
            e.printStackTrace();
        }
 
  
 */