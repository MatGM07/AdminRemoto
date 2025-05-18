package com.admin.remoto;

import com.admin.remoto.swing.AdministracionPanel;
import com.admin.remoto.swing.LoginPanel;

import com.admin.remoto.swing.ServidorListPanel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class RemotoApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		ConfigurableApplicationContext context = SpringApplication.run(RemotoApplication.class, args);

		SwingUtilities.invokeLater(() -> {
			try {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Crear JFrame principal
				JFrame frame = new JFrame("Control Remoto - Login");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				// Panel de tarjetas para cambiar entre pantallas
				CardLayout cardLayout = new CardLayout();
				JPanel mainPanel = new JPanel(cardLayout);

				// Obtener instancias de los paneles
				LoginPanel loginPanel = context.getBean(LoginPanel.class);
				ServidorListPanel serverListPanel = context.getBean(ServidorListPanel.class);
				AdministracionPanel administracionPanel = context.getBean(AdministracionPanel.class);

				// Configurar callbacks de navegación
				loginPanel.setOnLoginSuccess(() -> {
					serverListPanel.actualizarLista();
					cardLayout.show(mainPanel, "servers");
					frame.setTitle("Control Remoto - Servidores");
				});


				serverListPanel.setOnLogoutRequested(() -> {
					loginPanel.resetFields();
					SecurityContextHolder.clearContext();
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
				});

				serverListPanel.setOnConnectRequested(() -> {
					// Mostrar administración en el mismo frame
					cardLayout.show(mainPanel, "admin");
					frame.setTitle("Control Remoto - Administración");
				});

				administracionPanel.setOnVolverALista(() -> {
					cardLayout.show(mainPanel, "servers");
					frame.setTitle("Control Remoto - Servidores");
				});

				// Configurar conexión con panel de administración
				// Esto se manejará a través del controller, no directamente aquí
				// El controller llamará a abrirVentanaConexion() cuando sea necesario

				// Agregar paneles principales
				mainPanel.add(loginPanel, "login");
				mainPanel.add(serverListPanel, "servers");
				mainPanel.add(administracionPanel, "admin");

				// Mostrar panel inicial
				cardLayout.show(mainPanel, "login");

				// Configurar frame principal
				frame.getContentPane().add(mainPanel);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Error al iniciar la aplicación: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}