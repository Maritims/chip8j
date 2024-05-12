package io.github.maritims.chip8j.swing.debug;

import io.github.maritims.chip8j.Emulator;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import javax.swing.*;

public class StatusBar extends JPanel implements Observer {
    private final JLabel pauseLabel = new JLabel();

    public StatusBar() {
        var label = new JLabel("Paused: ");

        add(label);
        add(pauseLabel);
    }

    public void clear() {
        pauseLabel.setText("");
    }

    @Override
    public void update(Observable observable) {
        var emulator = (Emulator) observable;
        pauseLabel.setText(emulator.getCPU().isPaused() ? "Yes": "No");
    }
}
