package com.admin.remoto.swing;

import com.admin.remoto.SessionManager;
import com.admin.remoto.controller.ServidorListController;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.ServidorListService;
import com.admin.remoto.services.ServidorService;
import com.admin.remoto.services.UsuarioService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
@Component
public class ServidorListPanel extends JPanel {
    private final ServidorListController controller;
    private final ObjectProvider<AdministracionPanel> adminPanelProvider;
    private DefaultListModel<Servidor> listModel;
    private JList<Servidor> servidorList;
    private JTextField direccionField;
    private JButton addButton, deleteButton, connectButton, logoutButton;
    private JLabel statusLabel;
    private Runnable onLogoutRequested;
    private Runnable onConnectRequested;


    @Autowired
    public ServidorListPanel(ServidorListController controller,
                             ObjectProvider<AdministracionPanel> adminPanelProvider) {
        this.controller = controller;
        this.adminPanelProvider = adminPanelProvider;
        controller.setPanel(this);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top: input + logout
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        direccionField = new JTextField(20);
        addButton = new JButton("Agregar servidor");
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(new JLabel("Dirección (host:puerto):"), BorderLayout.WEST);
        inputPanel.add(direccionField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        logoutButton = new JButton("Cerrar sesión");
        logoutButton.addActionListener(e -> { if (onLogoutRequested != null) onLogoutRequested.run(); });
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: list
        listModel = new DefaultListModel<>();
        servidorList = new JList<>(listModel);
        servidorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(servidorList), BorderLayout.CENTER);

        // Bottom: actions + status
        deleteButton = new JButton("Eliminar"); deleteButton.setEnabled(false);
        connectButton = new JButton("Conectar"); connectButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(connectButton);
        buttonPanel.add(deleteButton);
        statusLabel = new JLabel(" "); statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addButton.addActionListener(e -> {
            String dir = direccionField.getText().trim();
            if (!dir.isEmpty()) {
                controller.agregarServidor(dir);
                direccionField.setText("");
            }
        });
        deleteButton.addActionListener(e -> {
            Servidor sel = servidorList.getSelectedValue();
            if (sel != null) controller.eliminarServidor(sel);
        });
        connectButton.addActionListener(e -> {
            Servidor sel = servidorList.getSelectedValue();
            if (sel != null) {
                controller.conectarServidor(sel);
                // cuando acabe el SwingWorker con éxito...
            }
        });
        servidorList.addListSelectionListener(e -> {
            boolean selected = !servidorList.isSelectionEmpty();
            deleteButton.setEnabled(selected);
            connectButton.setEnabled(selected);
        });
    }

    public void setOnLogoutRequested(Runnable callback) {
        this.onLogoutRequested = callback;
    }

    public void actualizarLista() {
        controller.cargarServidores();
    }

    public void mostrarServidores(List<Servidor> servidores) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            servidores.forEach(listModel::addElement);
        });
    }

    public void agregarServidorALista(Servidor servidor) {
        SwingUtilities.invokeLater(() -> listModel.addElement(servidor));
    }

    public void eliminarServidorDeLista(Servidor servidor) {
        SwingUtilities.invokeLater(() -> listModel.removeElement(servidor));
    }

    public void mostrarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setForeground(Color.BLACK);
        });
    }

    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setForeground(Color.RED);
        });
    }

    public void setLoadingState(boolean loading) {
        SwingUtilities.invokeLater(() -> {
            addButton.setEnabled(!loading);
            deleteButton.setEnabled(!loading && !servidorList.isSelectionEmpty());
            connectButton.setEnabled(!loading && !servidorList.isSelectionEmpty());
            statusLabel.setText(loading ? "Cargando..." : " ");
        });
    }

    public void setOnConnectRequested(Runnable callback) {
        this.onConnectRequested = callback;
    }

    public void abrirVentanaConexion(Servidor servidor) {
        SwingUtilities.invokeLater(() -> {
            AdministracionPanel adminPanel = adminPanelProvider.getObject();
            adminPanel.setOnVolverALista(() -> {
                JFrame adminFrame = (JFrame) SwingUtilities.getWindowAncestor(adminPanel);
                if (adminFrame != null) adminFrame.dispose();
                JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (mainFrame != null) mainFrame.setVisible(true);
            });
            adminPanel.iniciarConexion(servidor.getDireccion(), Integer.parseInt(servidor.getPuerto()));

            JFrame adminFrame = new JFrame("Control Remoto - " + servidor.getDireccion() + ":" + servidor.getPuerto());
            adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            adminFrame.getContentPane().add(adminPanel);
            adminFrame.pack();
            adminFrame.setLocationRelativeTo(null);
            adminFrame.setVisible(true);

            // Ocultar ventana principal
            JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (mainFrame != null) mainFrame.setVisible(false);
        });
    }

    public void iniciarAdministracion(Servidor servidor) {
        // Llamas al panel interno para conectar
        adminPanelProvider.getObject()
                .iniciarConexion(servidor.getDireccion(), Integer.parseInt(servidor.getPuerto()));
        // Y disparas el callback para cambiar de tarjeta
        if (onConnectRequested != null) onConnectRequested.run();
    }
}
