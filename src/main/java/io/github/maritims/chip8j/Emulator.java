package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.HostKey;
import io.github.maritims.chip8j.keypad.Keypad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Emulator {
    private final @NotNull CPU             cpu;
    private final @NotNull Keypad          keypad;
    private final @NotNull Consumer<int[]> onDrawEventHandler;
    private                boolean         isPoweredOn;

    public Emulator(@NotNull Consumer<int[]> onDrawEventHandler) {
        this.keypad             = new Keypad();
        this.cpu                = new CPU(64, 32, this.keypad);
        this.onDrawEventHandler = onDrawEventHandler;
    }

    public boolean isPoweredOn() {
        return isPoweredOn;
    }

    public void onKeyToggle(char asciiCode) {
        HostKey.fromAsciiCode(asciiCode).map(HostKey::getKeypadKey).ifPresent(keypad::onKeyToggle);
    }

    public Emulator loadProgram(byte[] program) {
        cpu.loadProgram(program);
        return this;
    }

    public Emulator powerOn() {
        isPoweredOn = true;
        return this;
    }

    public void update() {
        cpu.cycle();

        if (cpu.getDrawFlag()) {
            onDrawEventHandler.accept(cpu.getDisplay());
            cpu.setDrawFlag(false);
        }
    }
}