package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.HostKey;
import io.github.maritims.chip8j.keypad.Keypad;
import io.github.maritims.chip8j.swing.StatusPanel;
import io.github.maritims.chip8j.swing.menu.MenuBar;
import io.github.maritims.chip8j.swing.menu.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

public class Emulator extends JFrame implements KeyListener {
    private static final Logger log = LoggerFactory.getLogger(Emulator.class);

    private final Display                 display;
    private final Keypad                  keypad;
    private final AtomicReference<byte[]> program;
    private final StatusPanel             statusPanel;
    private final JLabel                  fpsLabel;
    private       SwingWorker<Void, Void> timerRegisterWorker;
    private       SwingWorker<Void, int[]> cpuWorker;

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

        var togglePower = new TogglePowerItem(
                "Power on",
                () -> (timerRegisterWorker == null || timerRegisterWorker.isCancelled() || timerRegisterWorker.isDone()) && (cpuWorker == null || cpuWorker.isCancelled() || cpuWorker.isDone()),
                this::powerOn,
                () -> {
                    timerRegisterWorker.cancel(true);
                    cpuWorker.cancel(true);
                });
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

        var southContainer = new JPanel();
        southContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
        southContainer.add(statusPanel);

        fpsLabel = new JLabel();
        southContainer.add(fpsLabel);

        add(display, BorderLayout.CENTER);
        add(southContainer, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    void powerOn() {
        var cpu = new CPU(64, 32, keypad).loadProgram(program.get());
        cpu.registerObservers(statusPanel);

        timerRegisterWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    cpu.updateTimers();
                    Thread.sleep(1000 / 60);
                }
                return null;
            }

            @Override
            protected void done() {
                timerRegisterWorker = null;
            }
        };

        cpuWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                var cycles = 600 / 60;

                while (!isCancelled()) {
                    for (var cycle = 0; cycle < cycles; cycle++) {
                        cpu.cycle();
                    }

                    if (cpu.getDrawFlag()) {
                        display.render(cpu.getPixels());
                        cpu.setDrawFlag(false);
                        fpsLabel.setText("FPS: " + 0);
                    }

                    var totalMs = (double) 1000 / 60;
                    var ms      = (int) totalMs;
                    var ns      = (int) ((totalMs % 1) * 1_000_000);

                    Thread.sleep(ms, ns);
                }
                return null;
            }

            @Override
            protected void done() {
                display.clear();
                keypad.onNextKeyReleased(null);
                cpuWorker = null;
            }
        };

        timerRegisterWorker.execute();
        cpuWorker.execute();
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