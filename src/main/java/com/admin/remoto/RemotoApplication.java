package com.admin.remoto;

import com.admin.remoto.swing.LoginPanel;
import com.admin.remoto.swing.RegisterPanel;
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
				// Establecer Look and Feel
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Crear JFrame principal
				JFrame frame = new JFrame("Control Remoto - Login");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				// Panel de tarjetas para cambiar entre las diferentes pantallas
				CardLayout cardLayout = new CardLayout();
				JPanel mainPanel = new JPanel(cardLayout);

				// Crear paneles
				LoginPanel loginPanel = context.getBean(LoginPanel.class);
				RegisterPanel registerPanel = context.getBean(RegisterPanel.class);

				ServidorListPanel serverListPanel = context.getBean(ServidorListPanel.class);


				serverListPanel.setOnLogoutRequested(() -> {
					// Limpiar campos de login
					loginPanel.resetFields();
					// Mostrar panel de login
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
					// Cerrar sesión de Spring Security si es necesario
					SecurityContextHolder.clearContext();
				});



				registerPanel.setOnBackToLogin(() -> {
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
				});
				registerPanel.setOnRegisterSuccess(() -> {
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
				});
				loginPanel.setOnLoginSuccess(() -> {
					serverListPanel.actualizarServidores(); // Actualizar la lista al hacer login
					cardLayout.show(mainPanel, "servers");
					frame.setTitle("Control Remoto - Servidores");
				});
				loginPanel.setOnRegisterRequested(() -> {
					registerPanel.resetFields();
					cardLayout.show(mainPanel, "register");
					frame.setTitle("Control Remoto - Registro");
				});

				serverListPanel.setOnLogoutRequested(() -> {
					// Limpiar campos de login
					loginPanel.resetFields();
					// Mostrar panel de login
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
					// Aquí podrías también limpiar la sesión si es necesario
				});

				mainPanel.add(loginPanel, "login");
				mainPanel.add(serverListPanel, "servers");
				mainPanel.add(registerPanel, "register");

				cardLayout.show(mainPanel, "login");

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
