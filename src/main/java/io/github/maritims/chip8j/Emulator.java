package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.HostKey;
import io.github.maritims.chip8j.keypad.Keypad;
import io.github.maritims.chip8j.swing.StatusPanel;
import io.github.maritims.chip8j.swing.menu.ExitItem;
import io.github.maritims.chip8j.swing.menu.FileMenu;
import io.github.maritims.chip8j.swing.menu.LoadRomItem;
import io.github.maritims.chip8j.swing.menu.MenuBar;
import io.github.maritims.chip8j.swing.menu.TogglePowerItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Emulator extends JFrame implements KeyListener {
    private final Display                  display;
    private final Keypad                   keypad;
    private final AtomicReference<byte[]>  program;
    private final StatusPanel              statusPanel;
    private       SwingWorker<Void, int[]> worker;

    public Emulator() {
        display     = new Display(64, 32, 10);
        keypad      = new Keypad();
        program     = new AtomicReference<>();
        statusPanel = new StatusPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("CHIP-8");
        setLocationRelativeTo(null);
        addKeyListener(this);
        setLayout(new BorderLayout());

        var togglePower = new TogglePowerItem("Power on", () -> worker == null, this::powerOn, () -> worker.cancel(true));
        var loadRom = new LoadRomItem("Load ROM", (path) -> {
            try {
                program.set(Files.readAllBytes(path));
                togglePower.setEnabled(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        var exit     = new ExitItem("Exit", this::dispose);
        var fileMenu = new FileMenu("File", loadRom, togglePower, exit);
        setJMenuBar(new MenuBar(fileMenu));

        add(display, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    void powerOn() {
        if (worker != null && !worker.isDone() && !worker.isCancelled()) {
            return;
        }

        var cpu = new CPU(64, 32, keypad).loadProgram(program.get());
        cpu.registerObservers(statusPanel);

        var updateTimerRegisters = new Timer(1000 / 60, e -> {
            cpu.updateTimers();
            if (cpu.getDrawFlag()) {
                display.render(cpu.getPixels());
                cpu.setDrawFlag(false);
            }
        });
        var cycleCpu             = new Timer(1, e -> cpu.cycle());
        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                while (!isCancelled()) {
                    cpu.cycle();

                    // Render game.
                    if (cpu.getDrawFlag()) {
                        publish(cpu.getPixels());
                        cpu.setDrawFlag(false);
                    }

                    Thread.sleep(1L);
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
        //worker.execute();
        updateTimerRegisters.start();
        cycleCpu.start();
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