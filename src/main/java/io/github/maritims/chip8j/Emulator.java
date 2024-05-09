package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.HostKey;
import io.github.maritims.chip8j.keypad.Keypad;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Emulator implements Observable {
    private final @NotNull CPU             cpu;
    private final @NotNull Keypad          keypad;
    private final @NotNull Consumer<int[]> onDrawEventHandler;
    private                boolean         isPoweredOn;

    public Emulator(@NotNull Consumer<int[]> onDrawEventHandler) {
        this.keypad             = new Keypad();
        this.cpu                = new CPU(64, 32, this.keypad);
        this.onDrawEventHandler = onDrawEventHandler;
    }

    public CPU getCPU() {
        return cpu;
    }

    public boolean isPoweredOn() {
        return isPoweredOn;
    }

    public void onKeyPressed(char asciiCode) {
        HostKey.fromAsciiCode(asciiCode)
                .map(HostKey::getKeypadKey)
                .ifPresent(keypad::onKeyPressed);
    }

    public void onKeyReleased(char asciiCode) {
        HostKey.fromAsciiCode(asciiCode)
                .map(HostKey::getKeypadKey)
                .ifPresent(keypad::onKeyReleased);
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

        notifyObservers();
    }

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(observer -> observer.update(this));
    }
}