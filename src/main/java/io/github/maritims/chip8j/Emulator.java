package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.HostKey;
import io.github.maritims.chip8j.keypad.Keypad;

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
import java.util.concurrent.atomic.AtomicReference;

public class Emulator extends JFrame implements KeyListener {
    private final Display display;
    private final CPU     cpu;
    private final Keypad                   keypad;
    private       SwingWorker<Void, int[]> worker;

    public Emulator() {
        display = new Display(64, 32, 10);
        keypad  = new Keypad();
        cpu     = new CPU(64, 32, display, keypad);

        var program = new AtomicReference<byte[]>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("CHIP-8");
        setLocationRelativeTo(null);
        addKeyListener(this);
        setLayout(new BorderLayout());

        var menuBar     = new JMenuBar();
        var fileMenu    = new JMenu("File");
        var loadRom     = new JMenuItem("Load ROM");
        var togglePower = new JMenuItem("Power on");
        var exit        = new JMenuItem("Exit");

        loadRom.setMnemonic('O');
        loadRom.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        loadRom.addActionListener(e -> {
            var fileChooser = new JFileChooser("/home/martin/IdeaProjects/chip8j/src/main/resources");
            //var fileChooser = new JFileChooser("C:\\users\\marit\\IdeaProjects\\chip8j\\src\\main\\resources");
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
                    program.set(Files.readAllBytes(path));
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
                powerOn(program.get());
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
        container.add(display);

        setJMenuBar(menuBar);
        add(container, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    void powerOn(byte[] program) {
        if (worker != null && !worker.isDone() && !worker.isCancelled()) {
            return;
        }

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                cpu.loadProgram(program);

                while (!isCancelled()) {
                    cpu.cycle();

                    try {
                        Thread.sleep(1L, 10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                return null;
            }

            @Override
            protected void done() {
                worker = null;
                display.clear();
            }

            @Override
            protected void process(List<int[]> chunks) {
                chunks.forEach(display::render);
            }
        };
        worker.execute();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        HostKey.fromAsciiCode(e.getKeyChar())
                .map(HostKey::getKeypadKey)
                .ifPresent(keypad::onKeyPressed);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        HostKey.fromAsciiCode(e.getKeyChar())
                .map(HostKey::getKeypadKey)
                .ifPresent(keypad::onKeyReleased);
    }
}