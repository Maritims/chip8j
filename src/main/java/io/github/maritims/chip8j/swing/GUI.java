package io.github.maritims.chip8j.swing;

import io.github.maritims.chip8j.Emulator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GUI extends JFrame implements KeyListener {
    private final DisplayPanel             displayPanel;
    private final DebugPanel               debugPanel;
    private       Emulator                 emulator;
    private       SwingWorker<Void, int[]> worker;
    private       byte[]                   program;

    public GUI() {
        this.displayPanel = new DisplayPanel(64, 32, 10);
        this.debugPanel   = new DebugPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("CHIP-8");
        setLocationRelativeTo(null);
        addKeyListener(this);

        var menuBar     = new JMenuBar();
        var fileMenu    = new JMenu("File");
        var loadRom     = new JMenuItem("Load ROM");
        var togglePower = new JMenuItem("Power on");
        var exit        = new JMenuItem("Exit");

        loadRom.setMnemonic('O');
        loadRom.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        loadRom.addActionListener(e -> {
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
                try {
                    program = Files.readAllBytes(path);
                    togglePower.setEnabled(true);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        togglePower.setEnabled(false);
        togglePower.setMnemonic('T');
        togglePower.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        togglePower.addActionListener(e -> {
            if (worker == null) {
                togglePower.setText("Power off");
                start();
            } else {
                worker.cancel(true);
                togglePower.setText("Power on");
            }
        });

        exit.setMnemonic('X');
        exit.addActionListener(e -> dispose());

        fileMenu.setMnemonic('F');
        fileMenu.add(loadRom);
        fileMenu.add(togglePower);
        fileMenu.add(exit);

        menuBar.add(fileMenu);

        var container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(displayPanel);
        container.add(debugPanel);

        setJMenuBar(menuBar);
        add(container);
        pack();
        setVisible(true);
    }

    void start() {
        if (worker != null && !worker.isDone() && !worker.isCancelled()) {
            return;
        }

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                emulator = new Emulator(this::publish)
                        .loadProgram(program)
                        .powerOn();
                emulator.registerObserver(debugPanel);

                while (!isCancelled() && emulator.isPoweredOn()) {
                    emulator.update();
                }

                displayPanel.clear();
                emulator = null;
                worker   = null;

                return null;
            }

            @Override
            protected void process(List<int[]> chunks) {
                displayPanel.draw(chunks.get(0));
            }
        };
        worker.execute();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (emulator != null) {
            emulator.onKeyPressed(e.getKeyChar());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (emulator != null) {
            emulator.onKeyReleased(e.getKeyChar());
        }
    }
}