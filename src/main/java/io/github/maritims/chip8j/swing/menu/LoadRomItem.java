package io.github.maritims.chip8j.swing.menu;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class LoadRomItem extends JMenuItem {
    private final Consumer<Path> onFileChosen;

    public LoadRomItem(String text, Consumer<Path> onFileChosen) {
        super(text);

        this.onFileChosen = onFileChosen;

        addActionListener(this::onAction);
        setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        setMnemonic('O');
    }

    private void onAction(ActionEvent e) {
        //var fileChooser = new JFileChooser("/home/martin/IdeaProjects/chip8j/src/main/resources");
        var fileChooser = new JFileChooser("C:\\users\\marit\\IdeaProjects\\chip8j\\src\\main\\resources");
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".ch8");
            }

            @Override
            public String getDescription() {
                return "Chip-8 programs";
            }
        });
        var result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            var path = Path.of(fileChooser.getSelectedFile().getAbsolutePath());
            onFileChosen.accept(path);
        }
    }
}
