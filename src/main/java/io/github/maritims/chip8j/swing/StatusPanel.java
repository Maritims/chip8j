package io.github.maritims.chip8j.swing;

import io.github.maritims.chip8j.CPU;
import io.github.maritims.chip8j.util.Observable;
import io.github.maritims.chip8j.util.Observer;

import javax.swing.*;

public class StatusPanel extends JPanel implements Observer {
    private final JLabel messageLabel;

    public StatusPanel() {
        var label = new JLabel("Status: ");
        messageLabel = new JLabel("");

        add(label);
        add(messageLabel);
    }

    public void setMessageLabel(String message) {
        messageLabel.setText(message);
    }

    @Override
    public void update(Observable observable) {
        var cpu = (CPU) observable;
        setMessageLabel(String.format("%04X", cpu.getOpcode()));
    }
}
