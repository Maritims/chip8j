package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.CPU;
import org.jetbrains.annotations.NotNull;

public class Emulator {
    private                boolean isPoweredOn;
    private final @NotNull Display display;
    private final @NotNull CPU     cpu;
    private final          int     framesPerSecond = 60;
    private final          int     fpsInterval     = framesPerSecond / 1000;

    public Emulator(@NotNull CPU cpu, @NotNull Display display) {
        this.cpu     = cpu;
        this.display = display;
    }

    void loadProgram(byte[] program) {
        cpu.loadProgram(program);
    }

    public void powerOn() throws InterruptedException {
        isPoweredOn = true;

        while (isPoweredOn) {
            cpu.cycle();

            if (cpu.getState().getDrawFlag()) {
                display.draw(cpu.getState().getPixelBuffer());
            }

            Thread.sleep(100L);
        }
    }

    public void powerOff() {
        isPoweredOn = false;
    }
}