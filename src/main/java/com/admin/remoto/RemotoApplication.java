package com.admin.remoto;

import com.admin.remoto.ui.AppInicio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class RemotoApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		ConfigurableApplicationContext context = SpringApplication.run(RemotoApplication.class, args);
		SwingUtilities.invokeLater(() -> context.getBean(AppInicio.class).start());
	}
}