package io.github.maritims.chip8j.swing.menu;

import javax.swing.*;
import java.util.Arrays;

public class FileMenu extends JMenu {
    public FileMenu(String text, JMenuItem... items) {
        super(text);

        setMnemonic('F');

        Arrays.stream(items).forEach(this::add);
    }
}
