package com.admin.remoto;

import com.admin.remoto.swing.AdministracionPanel;
import com.admin.remoto.swing.AppInicio;
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
		SwingUtilities.invokeLater(() -> context.getBean(AppInicio.class).start());
	}
}