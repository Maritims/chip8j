package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.CPU;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Objects;

public class Application {
    public static void main(String[] args) throws InterruptedException, IOException {
        var keypad       = new Keypad();
        var cpu          = new CPU(64, 32, keypad);
        var display      = new Display(64, 32, 10);
        var debugDisplay = new DebugDisplay(64, 32, 10);
        var container    = new JPanel();
        var frame        = new JFrame();

        cpu.registerObserver(debugDisplay);

        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(display);
        container.add(debugDisplay);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("CHIP-8");
        frame.add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Keypad.HostKey
                        .fromAsciiCode(e.getKeyChar())
                        .map(Keypad.HostKey::getKeypadButton)
                        .ifPresent(keypad::setPressedButton);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                Keypad.HostKey
                        .fromAsciiCode(e.getKeyChar())
                        .map(Keypad.HostKey::getKeypadButton)
                        .ifPresent(keypadButton -> keypad.setPressedButton(null));
            }
        });
        frame.setVisible(true);

        var emulator = new Emulator(cpu, display);
        //var rom = "1-chip8-logo.ch8";
        //var rom = "2-ibm-logo.ch8";
        //var rom = "3-corax+.ch8";
        var rom = "4-flags.ch8";
        //var rom = "5-quirks.ch8";
        //var rom = "BC_TEST.ch8";
        var is      = Application.class.getClassLoader().getResourceAsStream(rom);
        var program = Objects.requireNonNull(is).readAllBytes();
        emulator.loadProgram(program);
        emulator.powerOn();
    }
}
