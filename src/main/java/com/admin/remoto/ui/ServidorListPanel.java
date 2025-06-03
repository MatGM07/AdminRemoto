package com.admin.remoto.ui;

import com.admin.remoto.Observador.Observador;
import com.admin.remoto.controller.ServidorListController;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.services.panel.NavigationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Component
public class ServidorListPanel extends JPanel implements Observador<String, Object> {
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
    private NavigationService navigationService;

    @Autowired
    public ServidorListPanel(ServidorListController controller,
                             ObjectProvider<AdministracionPanel> adminPanelProvider) {
        this.controller = controller;
        this.adminPanelProvider = adminPanelProvider;
        controller.agregarObservador(this);
        initUI();
    }


    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        direccionField = new JTextField(20);
        addButton = new JButton("Agregar servidor");
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(new JLabel("Dirección (host:puerto):"), BorderLayout.WEST);
        inputPanel.add(direccionField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        logoutButton = new JButton("Cerrar sesión");
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        servidorList = new JList<>(listModel);
        servidorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(servidorList), BorderLayout.CENTER);

        deleteButton = new JButton("Eliminar"); deleteButton.setEnabled(false);
        connectButton = new JButton("Conectar"); connectButton.setEnabled(false);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(connectButton);
        buttonPanel.add(deleteButton);
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);


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

        logoutButton.addActionListener(e -> {
            if (onLogoutRequested != null) onLogoutRequested.run();
        });

        connectButton.addActionListener(e -> {
            Servidor sel = servidorList.getSelectedValue();
            if (sel != null) controller.conectarServidor(sel);
        });

        servidorList.addListSelectionListener(e -> {
            boolean selected = !servidorList.isSelectionEmpty();
            deleteButton.setEnabled(selected);
            connectButton.setEnabled(selected);
        });
    }

    @Override
    public void actualizar(String evento, Object dato) {
        SwingUtilities.invokeLater(() -> {
            switch (evento) {
                case "LOADING" -> setLoadingState((Boolean) dato);
                case "CARGA_EXITOSA" -> mostrarServidores((List<Servidor>) dato);
                case "CARGA_ERROR", "AGREGAR_ERROR", "ELIMINAR_ERROR" -> mostrarError((String) dato);
                case "SERVIDOR_AGREGADO" -> {
                    agregarServidorALista((Servidor) dato);
                    mostrarMensaje("Servidor agregado: " + ((Servidor) dato).getDireccion());
                }
                case "SERVIDOR_ELIMINADO" -> eliminarServidorDeLista((Servidor) dato);
                case "MENSAJE" -> mostrarMensaje((String) dato);
                case "CONECTAR_SERVIDOR" -> abrirVentanaConexion((Servidor) dato);
            }
        });
    }

    public void setOnLogoutRequested(Runnable callback) {
        this.onLogoutRequested = callback;
    }

    public void setOnConnectRequested(Runnable callback) {
        this.onConnectRequested = callback;
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

    public void abrirVentanaConexion(Servidor servidor) {
        navigationService.abrirVentanaConexion(this, servidor, adminPanelProvider::getObject);
    }
}