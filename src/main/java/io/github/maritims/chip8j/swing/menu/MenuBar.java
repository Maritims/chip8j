package io.github.maritims.chip8j.swing.menu;

import javax.swing.*;
import java.util.Arrays;

public class MenuBar extends JMenuBar {
    public MenuBar(JMenu... menus) {
        Arrays.stream(menus).forEach(this::add);
    }
}
