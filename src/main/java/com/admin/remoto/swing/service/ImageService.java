package com.admin.remoto.swing.service;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class ImageService {

    public static BufferedImage decodeImage(ByteBuffer buffer) throws IOException {
        byte[] imageData = new byte[buffer.remaining()];
        buffer.get(imageData);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            return ImageIO.read(bis);
        }
    }

    public static void displayImage(JLabel imageLabel, BufferedImage img, Dimension panelSize) {
        int maxWidth = panelSize.width - 40;
        int maxHeight = panelSize.height - 100;

        Image scaled = img.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setPreferredSize(new Dimension(maxWidth, maxHeight));
        imageLabel.revalidate();
    }
}
