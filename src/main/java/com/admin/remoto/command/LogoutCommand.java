package com.admin.remoto.command;

import com.admin.remoto.swing.LoginPanel;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.*;
import java.awt.*;

public class LogoutCommand implements Command {
    private final LoginPanel loginPanel;
    private final JPanel mainPanel;
    private final CardLayout layout;
    private final JFrame frame;

    public LogoutCommand(LoginPanel loginPanel, JPanel mainPanel, CardLayout layout, JFrame frame) {
        this.loginPanel = loginPanel;
        this.mainPanel = mainPanel;
        this.layout = layout;
        this.frame = frame;
    }

    @Override
    public void execute() {
        loginPanel.resetFields();
        SecurityContextHolder.clearContext();
        layout.show(mainPanel, "login");
        frame.setTitle("Control Remoto - Login");
    }
}
