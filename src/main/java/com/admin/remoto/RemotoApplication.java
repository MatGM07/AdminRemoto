package com.admin.remoto;

import com.admin.remoto.swing.LoginPanel;
import com.admin.remoto.swing.RegisterPanel;
import com.admin.remoto.swing.ServidorList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class RemotoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemotoApplication.class, args);
		System.setProperty("java.awt.headless", "false");
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
				LoginPanel loginPanel = new LoginPanel();
				ServidorList serverListPanel = new ServidorList();
				RegisterPanel registerPanel = new RegisterPanel();



				registerPanel.setOnBackToLogin(() -> {
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
				});
				registerPanel.setOnRegisterSuccess(() -> {
					cardLayout.show(mainPanel, "login");
					frame.setTitle("Control Remoto - Login");
				});
				loginPanel.setOnLoginSuccess(() -> {
					cardLayout.show(mainPanel, "servers");
					frame.setTitle("Control Remoto - Servidores");
				});
				loginPanel.setOnRegisterRequested(() -> {
					cardLayout.show(mainPanel, "register");
					frame.setTitle("Control Remoto - Registro");
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
