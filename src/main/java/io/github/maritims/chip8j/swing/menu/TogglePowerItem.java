package io.github.maritims.chip8j.swing.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class TogglePowerItem extends JMenuItem {
    private final Supplier<Boolean> isOff;
    private final Runnable          whenPowerOn;
    private final Runnable          whenPowerOff;

    public TogglePowerItem(String text, Supplier<Boolean> isOff, Runnable whenPowerOn, Runnable whenPowerOff) {
        super(text);

        this.isOff        = isOff;
        this.whenPowerOn  = whenPowerOn;
        this.whenPowerOff = whenPowerOff;

        addActionListener(this::onAction);
        setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        setEnabled(false);
        setMnemonic('T');
    }

    private void onAction(ActionEvent e) {
        if (isOff.get()) {
            setText("Power off");
            whenPowerOn.run();
        } else {
            whenPowerOff.run();
            setText("Power on");
        }
    }
}
