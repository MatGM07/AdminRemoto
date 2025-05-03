package com.admin.remoto.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ServidorList extends JPanel {
    private JTextField serverAddressField;
    private JButton addServerButton;
    private JList<ServerInfo> serverList;
    private DefaultListModel<ServerInfo> listModel;
    private JButton connectButton;
    private JButton deleteButton;
    private JLabel statusLabel;

    // Para guardar los servidores entre sesiones
    private static final String SERVERS_FILE = "servers.dat";

    public ServidorList() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior para agregar servidores
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        serverAddressField = new JTextField(20);
        addServerButton = new JButton("Agregar servidor");
        topPanel.add(new JLabel("Dirección del servidor:"), BorderLayout.WEST);
        topPanel.add(serverAddressField, BorderLayout.CENTER);
        topPanel.add(addServerButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Lista de servidores
        listModel = new DefaultListModel<>();
        serverList = new JList<>(listModel);
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setCellRenderer(new ServerListCellRenderer());
        JScrollPane scrollPane = new JScrollPane(serverList);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        connectButton = new JButton("Conectar");
        deleteButton = new JButton("Eliminar");
        buttonPanel.add(connectButton);
        buttonPanel.add(deleteButton);

        // Panel inferior con botones y mensajes
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Cargar servidores guardados
        loadSavedServers();

        // Event listeners
        addServerButton.addActionListener(e -> addServer());

        deleteButton.addActionListener(e -> deleteSelectedServer());

        connectButton.addActionListener(e -> connectToSelectedServer());

        // Habilitar doble clic para conectar
        serverList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    connectToSelectedServer();
                }
            }
        });

        // Actualizar estado de los botones cuando cambia la selección
        serverList.addListSelectionListener(e -> {
            boolean hasSelection = !serverList.isSelectionEmpty();
            connectButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        // Estado inicial de los botones
        connectButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void addServer() {
        String address = serverAddressField.getText().trim();
        if (address.isEmpty()) {
            statusLabel.setText("Por favor, introduce una dirección de servidor");
            return;
        }

        // Validar formato básico
        if (!address.contains(":")) {
            address += ":8081"; // Puerto por defecto
        }

        // Comprobar si ya existe
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.getElementAt(i).getAddress().equals(address)) {
                statusLabel.setText("Este servidor ya está en la lista");
                return;
            }
        }

        // Agregar a la lista
        ServerInfo serverInfo = new ServerInfo(address, "Servidor");
        listModel.addElement(serverInfo);
        serverAddressField.setText("");
        statusLabel.setText("Servidor agregado: " + address);

        // Guardar la lista actualizada
        saveServerList();
    }

    private void deleteSelectedServer() {
        int selectedIndex = serverList.getSelectedIndex();
        if (selectedIndex != -1) {
            ServerInfo server = listModel.getElementAt(selectedIndex);
            listModel.remove(selectedIndex);
            statusLabel.setText("Servidor eliminado: " + server.getAddress());
            saveServerList();
        }
    }

    private void connectToSelectedServer() {
        int selectedIndex = serverList.getSelectedIndex();
        if (selectedIndex != -1) {
            ServerInfo server = listModel.getElementAt(selectedIndex);
            statusLabel.setText("Conectando a " + server.getAddress() + "...");

            // Intentar abrir la ventana de envío de comandos
            try {
                String[] parts = server.getAddress().split(":");
                String host = parts[0];
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8081;

                // Crear y mostrar la ventana de envío de comandos
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                currentFrame.dispose();

                // Pasar la información del servidor a la ventana de envío
                new AdministracionPanel(host, port);
            } catch (Exception ex) {
                statusLabel.setText("Error al conectar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void loadSavedServers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SERVERS_FILE))) {
            ArrayList<ServerInfo> servers = (ArrayList<ServerInfo>) ois.readObject();
            for (ServerInfo server : servers) {
                listModel.addElement(server);
            }
            statusLabel.setText("Servidores cargados: " + servers.size());
        } catch (FileNotFoundException e) {
            // Es normal si es la primera vez que se ejecuta
            statusLabel.setText("No se encontraron servidores guardados");
        } catch (Exception e) {
            statusLabel.setText("Error al cargar servidores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveServerList() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERVERS_FILE))) {
            ArrayList<ServerInfo> servers = new ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) {
                servers.add(listModel.getElementAt(i));
            }
            oos.writeObject(servers);
        } catch (Exception e) {
            statusLabel.setText("Error al guardar servidores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Clase para representar la información de un servidor
    public static class ServerInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String address;
        private String name;

        public ServerInfo(String address, String name) {
            this.address = address;
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name + " (" + address + ")";
        }
    }

    // Renderizador personalizado para la lista de servidores
    private static class ServerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof ServerInfo) {
                ServerInfo server = (ServerInfo) value;
                setText(server.getName() + " (" + server.getAddress() + ")");
                setIcon(UIManager.getIcon("FileView.computerIcon"));
            }

            return c;
        }
    }
}
