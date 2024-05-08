package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.CPU;
import io.github.maritims.chip8j.gui.DisplayPanel;
import io.github.maritims.chip8j.gui.GUI;
import io.github.maritims.chip8j.keypad.Keypad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var keypad   = new Keypad();
        var cpu      = new CPU(64, 32, keypad);
        var display  = new DisplayPanel(64, 32, 10);
        var emulator = new Emulator(cpu, keypad);
        var gui      = new GUI(emulator);

        //var rom = "1-chip8-logo.ch8";
        //var rom = "2-ibm-logo.ch8";
        //var rom = "3-corax+.ch8";
        //var rom = "4-flags.ch8";
        //var rom = "5-quirks.ch8";
        //var rom = "BC_TEST.ch8";
    }
}
