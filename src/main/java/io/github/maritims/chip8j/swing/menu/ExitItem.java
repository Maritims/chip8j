package io.github.maritims.chip8j.swing.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitItem extends JMenuItem {
    private final Runnable onExit;

    public ExitItem(String text, Runnable onExit) {
        super(text);

        this.onExit = onExit;

        addActionListener(this::onAction);
        setMnemonic('X');
    }

    private void onAction(ActionEvent e) {
        onExit.run();
    }
}
