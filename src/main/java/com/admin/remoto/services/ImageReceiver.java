package com.admin.remoto.services;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Component
public class ImageReceiver {
    public BufferedImage fromBuffer(ByteBuffer buffer) throws IOException {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return ImageIO.read(new ByteArrayInputStream(data));
    }
}
