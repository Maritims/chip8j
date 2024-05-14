package io.github.maritims.chip8j.swing;

import io.github.maritims.chip8j.CPU;
import io.github.maritims.chip8j.util.Observable;
import io.github.maritims.chip8j.util.Observer;

import javax.swing.*;

public class StatusPanel extends JPanel implements Observer {
    private final JLabel messageLabel;

    public StatusPanel() {
        messageLabel = new JLabel("");
        add(messageLabel);


        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    public void setMessageLabel(String message) {
        messageLabel.setText(message);
    }

    @Override
    public void update(Observable observable) {
        var cpu = (CPU) observable;
        setMessageLabel(
                "Status: " + String.format("%04X", cpu.getOpcode()) + " - " +
                "Paused: " + cpu.isPaused()
        );
    }
}
