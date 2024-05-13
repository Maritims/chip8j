package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.Keypad;
import io.github.maritims.chip8j.util.Observable;
import io.github.maritims.chip8j.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CPU implements Observable {
    private static final Logger log = LoggerFactory.getLogger(CPU.class);

    private final int[]          memory = new int[4096];
    private final Stack<Integer> stack  = new Stack<>();
    private final int[]          V      = new int[16];
    private final int[]          pixels;
    private final Keypad         keypad;
    private       int            PC     = 0x200;
    private       boolean        drawFlag;
    private       int            I;
    private       int            delayTimer;
    private       int            soundTimer;
    private       int            opcode;
    private       boolean        isPaused;

    public CPU(int columns, int rows, Keypad keypad) {
        this.pixels = new int[columns * rows];
        this.keypad = keypad;

        var fontSet = new int[]{
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        System.arraycopy(fontSet, 0, memory, 0, fontSet.length);
    }

    private void decodeAndExecute() {
        if (isPaused) {
            return;
        }

        opcode = memory[PC] << 8 | (memory[PC + 1] & 0x00FF);
        notifyObservers();
        PC += 2;

        var x   = (opcode & 0x0F00) >>> 8;
        var y   = (opcode & 0x00F0) >>> 4;
        var n   = opcode & 0x000F;
        var nn  = opcode & 0x00FF;
        var nnn = opcode & 0x0FFF;

        switch (opcode) {
            case 0x00E0 -> {
                Arrays.fill(pixels, 0);
                drawFlag = true;
            }
            case 0x00EE -> {
                PC       = stack.pop() + 2;
                drawFlag = true;
            }
        }

        switch (opcode & 0xF000) {
            case 0x1000 -> PC = nnn;
            case 0x2000 -> {
                stack.push(PC - 2);
                PC = nnn;
            }
            case 0x3000 -> {
                if (V[x] == nn) {
                    PC += 2;
                }
            }
            case 0x4000 -> {
                if (V[x] != nn) {
                    PC += 2;
                }
            }
            case 0x5000 -> {
                if (V[x] == V[y]) {
                    PC += 2;
                }
            }
            case 0x6000 -> V[x] = nn;
            case 0x7000 -> {
                var result = V[x] + nn;
                V[x] = result >= 256 ? result - 256 : result;
            }
        }

        switch (opcode & 0xF00F) {
            case 0x8000 -> V[x] = V[y];
            case 0x8001 -> V[x] |= V[y];
            case 0x8002 -> V[x] &= V[y];
            case 0x8003 -> V[x] ^= V[y];
            case 0x8004 -> {
                var result = V[x] + V[y];
                V[x]   = result & 0x00FF;
                V[0xF] = result > 255 ? 1 : 0;
            }
            case 0x8005 -> {
                var result = V[x] - V[y];
                V[x] = result & 0xFF;
                // In Java, we can represent 0xFF as 1111111.
                // Any value above 0xFF will have 9 or more bits at the very least.
                // Any value below 0 will in Java always have 32 bits.
                // By comparing the lower 9 bits of the result with the lower 8 bits of the result we can understand if we have overflow/underflow or not.
                V[0xF] = (result & 0x1FF) == (result & 0xFF) ? 1 : 0;
            }
            case 0x8006 -> {
                var vx = V[x];
                V[x]   = vx >>> 1;
                V[0xF] = vx & 0x1;
            }
            case 0x8007 -> {
                V[x]   = (V[y] - V[x]) & 0xFF;
                V[0xF] = V[y] > V[x] ? 1 : 0;
            }
            case 0x800E -> {
                var vx = V[x] << 1;
                V[x]   = vx & 0xFF;
                V[0xF] = (vx & 0x100) == 0x100 ? 1 : 0;
            }
            case 0x9000 -> {
                if (V[x] != V[y]) {
                    PC += 2;
                }
            }
        }

        switch (opcode & 0xF000) {
            case 0xA000 -> I = nnn;
            case 0xB000 -> PC = V[0] + nnn;
            case 0xC000 -> V[x] = new Random().nextInt(256) & nn;
            case 0xD000 -> {
                V[0xF] = 0;

                for (var row = 0; row < n; row++) {
                    var pixel = memory[I + row];

                    for (var column = 0; column < 8; column++) {
                        var isPixelSet = (pixel & 0b1000_0000) > 0;

                        if (isPixelSet) {
                            var targetX = V[x] + column;
                            var targetY = V[y] + row;

                            if (targetX > 64) {
                                targetX -= 64;
                            } else if (targetX < 0) {
                                targetX += 64;
                            }

                            if (targetY > 32) {
                                targetY -= 32;
                            } else if (targetY < 0) {
                                targetY += 32;
                            }

                            var pixelLocation = targetX + (targetY * 64);
                            if (pixelLocation == 1) {
                                V[0xF] = 1;
                            }

                            pixels[pixelLocation] ^= 1;
                        }

                        // Move all bits one step to the left in preparation for checking if the next bit is set.
                        pixel <<= 1;
                    }

                    drawFlag = true;
                }
            }
        }

        switch (opcode & 0xF0FF) {
            case 0xE09E -> {
                if (keypad.isKeyPressed(V[x])) {
                    PC += 2;
                }
            }
            case 0xE0A1 -> {
                if (!keypad.isKeyPressed(V[x])) {
                    PC += 2;
                }
            }
            case 0xF007 -> V[x] = delayTimer & 0xFF;
            case 0xF00A -> {
                isPaused = true;

                keypad.onNextKeyReleased(keypadKey -> {
                    V[x]     = keypadKey.getCosmacVipKeyCode();
                    isPaused = false;
                });
            }
            case 0xF015 -> delayTimer = V[x];
            case 0xF018 -> soundTimer = V[x];
            case 0xF01E -> I += V[x];
            case 0xF029 -> {
                I        = V[x] * 5;
                drawFlag = true;
            }
            case 0xF033 -> {
                var number = V[x];
                var digits = new LinkedList<Integer>();
                while (number > 0) {
                    var digit = number % 10;
                    digits.push(digit);
                    number /= 10;
                }
                for (var i = 0; i < digits.size(); i++) {
                    memory[I + i] = digits.get(i).byteValue();
                }
            }
            case 0xF055 -> {
                for (var i = 0; i <= x; i++) {
                    memory[I + i] = V[i] & 0xFF;
                }
            }
            case 0xF065 -> {
                for (var i = 0; i <= x; i++) {
                    V[i] = memory[I + i] & 0xFF;
                }
            }
        }
    }

    public int getOpcode() {
        return opcode;
    }

    public boolean getDrawFlag() {
        return drawFlag;
    }

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    public int[] getPixels() {
        return pixels;
    }

    public void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }

        if (soundTimer > 0) {
            soundTimer--;
        }
    }

    public CPU loadProgram(byte[] program) {
        for (var i = 0; i < program.length; i++) {
            memory[0x200 + i] = program[i] & 0x00FF;
        }
        return this;
    }

    public void cycle() {
        decodeAndExecute();
    }

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void registerObservers(Observer... observers) {
        this.observers.addAll(Arrays.asList(observers));
    }

    @Override
    public void removeObservers(Observer... observers) {
        this.observers.removeAll(Arrays.asList(observers));
    }

    @Override
    public void notifyObservers() {
        observers.forEach(observer -> observer.update(this));
    }
}