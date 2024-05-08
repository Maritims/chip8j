package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.CPU;
import io.github.maritims.chip8j.keypad.HostKey;
import io.github.maritims.chip8j.keypad.Keypad;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class Emulator {
    private static final Logger log = LoggerFactory.getLogger(Emulator.class);

    private final @NotNull CPU             cpu;
    private final @NotNull Keypad          keypad;
    private @Nullable      Consumer<int[]> onDrawEventHandler;
    private @Nullable      Runnable        onLoadProgramEventHandler;
    private                boolean         isPoweredOn;

    public Emulator(@NotNull CPU cpu, @NotNull Keypad keypad) {
        this.cpu    = cpu;
        this.keypad = keypad;
    }

    public void setOnLoadProgramEventHandler(@Nullable Runnable onLoadProgram) {
        this.onLoadProgramEventHandler = onLoadProgram;
    }

    public boolean isPoweredOn() {
        return isPoweredOn;
    }

    public void loadProgram(Path path) {
        try {
            loadProgram(Files.readAllBytes(path));
        } catch (IOException e) {
            log.error("Unable to read file", e);
        }
    }

    public void loadProgram(byte[] program) {
        cpu.loadProgram(program);
        if (onLoadProgramEventHandler != null) {
            onLoadProgramEventHandler.run();
        }
    }

    public void onKeyToggle(char asciiCode) {
        HostKey.fromAsciiCode(asciiCode).map(HostKey::getKeypadKey).ifPresent(keypad::onKeyToggle);
    }

    public void powerOn() {
        isPoweredOn = true;
        run();
    }

    public void powerOff() {
        isPoweredOn = false;
    }

    private void run() {
        while (isPoweredOn) {
            cpu.cycle();

            if (cpu.getDrawFlag() && onDrawEventHandler != null) {
                onDrawEventHandler.accept(cpu.getDisplay());
            }

            try {
                Thread.sleep(1L, 10000);
            } catch (InterruptedException e) {
                powerOff();
                log.error("Unable to sleep. Powering down emulator", e);
            }
        }
    }

    public void setOnDrawEventHandler(Consumer<int[]> onDrawEventHandler) {
        this.onDrawEventHandler = onDrawEventHandler;
    }
}