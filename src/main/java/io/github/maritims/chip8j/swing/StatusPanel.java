package io.github.maritims.chip8j.swing;

import io.github.maritims.chip8j.Emulator;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

class StatusPanel extends JPanel implements Observer {
    private final JLabel textLabel;

    StatusPanel(int width, int height) {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(width, height));

        var statusLabel = new JLabel("Status:");
        textLabel = new JLabel("");

        add(statusLabel);
        add(textLabel);
    }

    void setText(String text) {
        textLabel.setText(text);
    }

    @Override
    public void update(Observable observable) {
        var emulator = (Emulator) observable;
        var text = new ArrayList<String>();

        if(emulator.isBlocking()) {
            text.add("Blocking");
        }

        text.add("Opcode: " + String.format("%04X", emulator.getCPU().getOpcode()));

        setText(String.join(", ", text));
    }
}
