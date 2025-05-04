package com.admin.remoto.swing;

import com.admin.remoto.SessionManager;
import com.admin.remoto.controller.ServidorListController;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.ServidorListService;
import com.admin.remoto.services.ServidorService;
import com.admin.remoto.services.UsuarioService;
import jakarta.annotation.PostConstruct;
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
    private DefaultListModel<Servidor> listModel;
    private JList<Servidor> servidorList;
    private JLabel statusLabel;

    private final ServidorListController controller;
    private Runnable onLogoutRequested;

    @Autowired
    public ServidorListPanel(ServidorListController controller) {
        this.controller = controller;
        initComponents();
        controller.setServidorListPanel(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());

        // Panel de entrada
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        JTextField direccionField = new JTextField(20);
        JButton addButton = new JButton("Agregar servidor");
        inputPanel.add(new JLabel("Dirección (host:puerto):"), BorderLayout.WEST);
        inputPanel.add(direccionField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        // Botón de logout
        JButton logoutButton = new JButton("Cerrar sesión");
        logoutButton.addActionListener(e -> {
            if (onLogoutRequested != null) {
                onLogoutRequested.run();
            }
        });

        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Lista de servidores
        listModel = new DefaultListModel<>();
        servidorList = new JList<>(listModel);
        servidorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(servidorList);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior
        JButton connectButton = new JButton("Conectar");
        JButton deleteButton = new JButton("Eliminar");
        connectButton.setEnabled(false);
        deleteButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(connectButton);
        buttonPanel.add(deleteButton);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addButton.addActionListener(e -> {
            String input = direccionField.getText().trim();
            if (!input.isEmpty()) {
                controller.agregarServidor(input);
                direccionField.setText("");
            }
        });

        deleteButton.addActionListener(e -> {
            Servidor seleccionado = servidorList.getSelectedValue();
            if (seleccionado != null) {
                controller.eliminarServidor(seleccionado);
            }
        });

        servidorList.addListSelectionListener(e -> {
            boolean seleccionado = !servidorList.isSelectionEmpty();
            connectButton.setEnabled(seleccionado);
            deleteButton.setEnabled(seleccionado);
        });

        connectButton.addActionListener(e -> {
            Servidor seleccionado = servidorList.getSelectedValue();
            if (seleccionado != null) {
                controller.conectarAServidor(seleccionado);
            }
        });
    }

    // Métodos para el controlador
    public void mostrarServidores(List<Servidor> servidores) {
        listModel.clear();
        servidores.forEach(listModel::addElement);
    }

    public void agregarServidorALista(Servidor servidor) {
        listModel.addElement(servidor);
    }

    public void eliminarServidorDeLista(Servidor servidor) {
        listModel.removeElement(servidor);
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

    public void abrirVentanaConexion() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.dispose();
        // Aquí iría la lógica para abrir la nueva ventana de conexión
    }

    public void setOnLogoutRequested(Runnable callback) {
        this.onLogoutRequested = callback;
    }

    public void actualizarLista() {
        controller.cargarServidores();
    }
}