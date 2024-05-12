package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.Keypad;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

public class CPU {
    private final int[]             memory;
    private final int[]             display;
    private       boolean           drawFlag;
    private       int               PC;
    private       int               I;
    private final Stack<Integer>    stack;
    private       int               delayTimer;
    private       int               soundTimer;
    private final int[]             V;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Keypad            keypad;
    private final Consumer<Boolean> onPauseEventHandler;
    private       int               programLength;

    private int opcode;
    private int x;
    private int y;
    private int n;
    private int nn;
    private int nnn;

    public CPU(int columns, int rows, Keypad keypad, Consumer<Boolean> onPauseEventHandler) {
        this.onPauseEventHandler = onPauseEventHandler;
        this.display             = new int[columns * rows];
        this.memory              = new int[4096];
        this.PC                  = 0x200;
        this.stack               = new Stack<>();
        this.V                   = new int[16];
        this.keypad              = keypad;

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

    // region Getters
    public int[] getDisplay() {
        return display;
    }

    public boolean getDrawFlag() {
        return drawFlag;
    }

    public int[] getMemory() {
        return memory;
    }

    public List<Integer> getOpcodes() {
        var opcodes = new ArrayList<Integer>();
        for (var i = 0x200; i < (0x200 + programLength); i += 2) {
            opcodes.add(memory[i]);
        }
        return opcodes;
    }

    public int getOpcode() {
        return opcode;
    }

    public int getX() {
        return x;
    }

    public int getPC() {
        return PC;
    }

    public int getI() {
        return I;
    }

    public int getV(int position) {
        return V[position];
    }

    public int getVLength() {
        return V.length;
    }

    public int getDelayTimer() {
        return delayTimer;
    }

    public int getSoundTimer() {
        return soundTimer;
    }
    // endregion

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    void setDelayTimer(int delayTimer) {
        this.delayTimer = delayTimer;
    }

    void updateDelayTimer(IntUnaryOperator func) {
        setDelayTimer(func.applyAsInt(getDelayTimer()));
    }

    void setSoundTimer(int soundTimer) {
        this.soundTimer = soundTimer;
    }

    void updateSoundTimer(IntUnaryOperator func) {
        setSoundTimer(func.applyAsInt(getSoundTimer()));
    }
    // endregion

    protected int fetch() {
        return memory[PC] << 8 | (memory[PC + 1] & 0x00FF);
    }

    void execute(int rawOpcode) {
        PC += 2;
        opcode = rawOpcode;
        x      = (rawOpcode & 0x0F00) >>> 8;
        y      = (rawOpcode & 0x00F0) >>> 4;
        n      = rawOpcode & 0x000F;
        nn     = rawOpcode & 0x00FF;
        nnn    = rawOpcode & 0x0FFF;

        switch (opcode) {
            case 0x00E0 -> {
                Arrays.fill(display, 0);
                setDrawFlag(true);
            }
            case 0x00EE -> {
                PC = stack.pop() + 2;
                setDrawFlag(true);
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

                            var locationInPixelBuffer = targetX + (targetY * 64);
                            if (locationInPixelBuffer == 1) {
                                V[0xF] = 1;
                            }

                            display[locationInPixelBuffer] ^= 1;
                        }

                        // Move all bits one step to the left in preparation for checking if the next bit is set.
                        pixel <<= 1;
                    }

                    setDrawFlag(true);
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
            case 0xF007 -> V[x] = delayTimer;
            case 0xF00A -> {
                keypad.setOnNextKeyPressEventHandler(keypadKey -> {
                    V[x] = keypadKey.getCosmacVipKeyCode();
                    onPauseEventHandler.accept(false);
                });

                onPauseEventHandler.accept(true);
            }
            case 0xF015 -> delayTimer = V[x];
            case 0xF018 -> soundTimer = V[x];
            case 0xF01E -> I += V[x];
            case 0xF029 -> {
                I = V[x] * 5;
                setDrawFlag(true);
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

    public void loadProgram(byte[] program) {
        this.programLength = program.length;
        for (var i = 0; i < program.length; i++) {
            memory[0x200 + i] = program[i] & 0x00FF;
        }
    }

    public void cycle() {
        var opcode = fetch();
        execute(opcode);

        if (getDelayTimer() > 0) {
            updateDelayTimer(dt -> dt - 1);
        }

        if (getSoundTimer() > 0) {
            updateSoundTimer(st -> st - 1);
        }
    }
}