package com.admin.remoto.swing;

import com.admin.remoto.controller.AdministracionController;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.services.business.EscaladoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AdministracionPanel extends JPanel {
    private final JLabel imageLabel = new JLabel();
    private final JTextPane logArea = new JTextPane();
    private final JButton backButton = new JButton("Volver a la lista de servidores");
    private final JButton transferFileButton = new JButton("Transferir archivo");
    private final JCheckBox autoScrollCheckBox = new JCheckBox("Auto-scroll logs", true);
    private Servidor currentServidor;

    private final AdministracionController controller;
    private Runnable onVolverALista;

    @Autowired
    public AdministracionPanel(AdministracionController controller) {
        this.controller = controller;
        initComponents();
        controller.setAdministracionPanel(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane imagePane = new JScrollPane(imageLabel);
        imagePane.setPreferredSize(new Dimension(1024, 768));
        add(imagePane, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setFont(new Font("Arial", Font.PLAIN, 12));
        logArea.setBackground(new Color(43, 45, 48));
        logArea.setForeground(Color.WHITE);

        JScrollPane logPane = new JScrollPane(logArea);
        logPane.setPreferredSize(new Dimension(500, 0));
        add(logPane, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(backButton, BorderLayout.WEST);
        bottomPanel.add(autoScrollCheckBox, BorderLayout.EAST);
        bottomPanel.add(transferFileButton, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> volverAListaServidores());
        transferFileButton.addActionListener(e -> controller.seleccionarYTransferirArchivo(this));
    }

    /**
     * Llama a controller.conectarAServidor y luego al callback con true/false.
     */
    public void iniciarConexion(Servidor servidor, Consumer<Boolean> callback) {
        this.currentServidor = servidor;
        controller.conectarAServidor(servidor, exito -> {
            if (!exito) {
                mostrarError("No se pudo conectar a " + servidor.getDireccion() + ":" + servidor.getPuerto());
                callback.accept(false);
            } else {
                callback.accept(true);
            }
        });
    }

    /**
     * Actualiza la imagen en pantalla (desde EDT).
     */
    public void actualizarImagen(BufferedImage img) {
        SwingUtilities.invokeLater(() -> {
            if (img == null) return;

            int panelWidth = imageLabel.getWidth();
            int panelHeight = imageLabel.getHeight();
            if (panelWidth <= 0) panelWidth = 800;
            if (panelHeight <= 0) panelHeight = 600;

            double imgRatio = (double) img.getWidth() / img.getHeight();
            double panelRatio = (double) panelWidth / panelHeight;

            int scaledWidth, scaledHeight;
            if (imgRatio > panelRatio) {
                scaledWidth = panelWidth;
                scaledHeight = (int) (panelWidth / imgRatio);
            } else {
                scaledHeight = panelHeight;
                scaledWidth = (int) (panelHeight * imgRatio);
            }

            BufferedImage scaled = EscaladoService.scaleImage(img, scaledWidth, scaledHeight);
            imageLabel.setIcon(new ImageIcon(scaled));
            revalidate();
        });
    }

    /**
     * Agrega un renglón de log en el JTextPane (desde EDT).
     */
    public void log(String prefix, String msg) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = logArea.getStyledDocument();
            Style style = logArea.addStyle("default", null);

            if ("ERROR".equals(prefix)) {
                StyleConstants.setForeground(style, Color.RED);
            } else if ("INFO".equals(prefix)) {
                StyleConstants.setForeground(style, Color.WHITE);
            } else {
                StyleConstants.setForeground(style, Color.LIGHT_GRAY);
            }

            try {
                doc.insertString(doc.getLength(), String.format("[%s] %s%n", prefix, msg), style);
                if (autoScrollCheckBox.isSelected()) {
                    logArea.setCaretPosition(doc.getLength());
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void mostrarMensaje(String mensaje) {
        log("INFO", mensaje);
    }

    public void mostrarError(String mensaje) {
        log("ERROR", mensaje);
    }

    public void volverAListaServidores() {
        if (currentServidor != null) {
            controller.guardarLoteLogs();
            String host = currentServidor.getDireccion();
            int port = Integer.parseInt(currentServidor.getPuerto());
            controller.desconectar(host, port);
        }
        if (onVolverALista != null) onVolverALista.run();
    }

    public void setOnVolverALista(Runnable callback) {
        this.onVolverALista = callback;
    }

    // ——— Estos métodos se invocan desde AdministracionController.actualizar() ———

    /**
     * Recibe un mensaje de texto JSON parseado y el raw. El controlador llama a esto
     * cuando llega un Evento.TEXT. Debe mostrar el log o lo que corresponda.
     */

    /**
     * Recibe una BufferedImage. El controlador llama a esto cuando llega un Evento.BINARY.
     */
    public void recibirImagen(BufferedImage img) {
        if (img != null) {
            actualizarImagen(img);
        } else {
            mostrarError("Imagen recibida no válida");
        }
    }
}