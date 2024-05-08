package io.github.maritims.chip8j.gui;

import io.github.maritims.chip8j.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GUI implements KeyListener {
    private static final Logger log = LoggerFactory.getLogger(GUI.class);

    private final Emulator emulator;

    public GUI(Emulator emulator) {
        this.emulator = emulator;

        var frame       = new JFrame();
        var menuBar     = new JMenuBar();
        var fileMenu    = new JMenu("File");
        var loadRom     = new JMenuItem("Load ROM");
        var togglePower = new JMenuItem("Power on");

        emulator.setOnLoadProgramEventHandler(() -> togglePower.setEnabled(true));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("CHIP-8");
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(this);

        loadRom.setMnemonic('O');
        loadRom.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        loadRom.addActionListener(e -> {
            var fileChooser = new JFileChooser("C:\\users\\marit\\ideaprojects\\chip8j\\src\\main\\resources");
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
            var result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                emulator.loadProgram(Path.of(fileChooser.getSelectedFile().getAbsolutePath()));
            }
        });

        togglePower.setEnabled(false);
        togglePower.setMnemonic('T');
        togglePower.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        togglePower.addActionListener(e -> {
            if(emulator.isPoweredOn()) {
                emulator.powerOff();
                togglePower.setText("Power on");
                togglePower.setEnabled(true);
            } else {
                emulator.powerOn();
                togglePower.setText("Power off");
            }
        });

        fileMenu.setMnemonic('F');
        fileMenu.add(loadRom);
        fileMenu.add(togglePower);

        menuBar.add(fileMenu);

        var display   = new DisplayPanel(64, 32, 10);
        var container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(display);

        frame.setJMenuBar(menuBar);
        frame.add(container);
        frame.pack();
        frame.setVisible(true);

        var worker = new SwingWorker<Void, Boolean>() {
            @Override
            protected Void doInBackground() throws Exception {
                emulator.powerOn();
                publish(false);
            }

            @Override
            protected void process(List<Boolean> chunks) {
            }
        };
        worker.run();

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        emulator.onKeyToggle(e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        emulator.onKeyToggle(e.getKeyChar());
    }
}