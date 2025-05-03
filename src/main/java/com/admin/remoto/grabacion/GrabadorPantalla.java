package com.admin.remoto.grabacion;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

public class GrabadorPantalla {
    public static File grabarPantalla(String rutaArchivo, int duracionEnSegundos) throws Exception {
        Rectangle areaPantalla = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        FFmpegFrameRecorder grabador = new FFmpegFrameRecorder(rutaArchivo, areaPantalla.width, areaPantalla.height);
        grabador.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
        grabador.setFormat("mp4");
        grabador.setFrameRate(10);
        grabador.start();

        Robot robot = new Robot();
        Java2DFrameConverter convertidor = new Java2DFrameConverter();

        long tiempoFin = System.currentTimeMillis() + (duracionEnSegundos * 1000);

        while (System.currentTimeMillis() < tiempoFin) {
            BufferedImage captura = robot.createScreenCapture(areaPantalla);
            org.bytedeco.javacv.Frame cuadro = convertidor.convert(captura);
            grabador.record(cuadro);
            Thread.sleep(100);
        }

        grabador.stop();
        grabador.release();

        return new File(rutaArchivo);
    }
}
