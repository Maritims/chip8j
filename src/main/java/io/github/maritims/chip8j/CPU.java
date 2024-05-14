package io.github.maritims.chip8j;

import io.github.maritims.chip8j.keypad.Keypad;
import io.github.maritims.chip8j.util.FunctionPointer;
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

    private final FunctionPointer[] table  = new FunctionPointer[0xF + 1];
    private final FunctionPointer[] table0 = new FunctionPointer[0xE + 1];
    private final FunctionPointer[] table8 = new FunctionPointer[0xE + 1];
    private final FunctionPointer[] tableE = new FunctionPointer[0xE + 1];
    private final FunctionPointer[] tableF = new FunctionPointer[0x65 + 1];
    private       int               x;
    private       int               nnn;
    private       int               nn;
    private       int               y;
    private       int               n;

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

        table[0x0] = this::table0;
        table[0x1] = this::OP_1nnn;
        table[0x2] = this::OP_2nnn;
        table[0x3] = this::OP_3xnn;
        table[0x4] = this::OP_4xnn;
        table[0x5] = this::OP_5xy0;
        table[0x6] = this::OP_6xnn;
        table[0x7] = this::OP_7xnn;
        table[0x8] = this::table8;
        table[0x9] = this::OP_9xy0;
        table[0xA] = this::OP_Annn;
        table[0xB] = this::OP_Bnnn;
        table[0xC] = this::OP_Cxnn;
        table[0xD] = this::OP_Dxyn;
        table[0xE] = this::tableE;
        table[0xF] = this::tableF;

        for (var i = 0; i <= 0xE; i++) {
            table0[i] = this::OP_NULL;
            table8[i] = this::OP_NULL;
            tableE[i] = this::OP_NULL;
        }

        table0[0x0] = this::OP_00E0;
        table0[0xE] = this::OP_00EE;

        table8[0x0] = this::OP_8xy0;
        table8[0x1] = this::OP_8xy1;
        table8[0x2] = this::OP_8xy2;
        table8[0x3] = this::OP_8xy3;
        table8[0x4] = this::OP_8xy4;
        table8[0x5] = this::OP_8xy5;
        table8[0x6] = this::OP_8xy6;
        table8[0x7] = this::OP_8xy7;
        table8[0xE] = this::OP_8xyE;

        tableE[0x1] = this::OP_ExA1;
        tableE[0xE] = this::OP_Ex9E;

        for (var i = 0; i <= 0x65; i++) {
            tableF[i] = this::OP_NULL;
        }

        tableF[0x07] = this::OP_Fx07;
        tableF[0x0A] = this::OP_Fx0A;
        tableF[0x15] = this::OP_Fx15;
        tableF[0x18] = this::OP_Fx18;
        tableF[0x1E] = this::OP_Fx1E;
        tableF[0x29] = this::OP_Fx29;
        tableF[0x33] = this::OP_Fx33;
        tableF[0x55] = this::OP_Fx55;
        tableF[0x65] = this::OP_Fx65;
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

    public boolean isPaused() {
        return isPaused;
    }

    void table0() {
        table0[opcode & 0x000F].run();
    }

    void table8() {
        table8[opcode & 0x000F].run();
    }

    void tableE() {
        tableE[opcode & 0x000F].run();
    }

    void tableF() {
        tableF[opcode & 0x00FF].run();
    }

    void OP_NULL() {
    }

    void OP_00E0() {
        Arrays.fill(pixels, 0);
        drawFlag = true;
    }

    void OP_00EE() {
        PC       = stack.pop() + 2;
        drawFlag = true;
    }

    void OP_1nnn() {
        PC = nnn;
    }

    void OP_2nnn() {
        stack.push(PC - 2);
        PC = nnn;
    }

    void OP_3xnn() {
        if (V[x] == nn) {
            PC += 2;
        }
    }

    void OP_4xnn() {
        if (V[x] != nn) {
            PC += 2;
        }
    }

    void OP_5xy0() {
        if (V[x] == V[y]) {
            PC += 2;
        }
    }

    void OP_6xnn() {
        V[x] = nn;
    }

    void OP_7xnn() {
        var result = V[x] + nn;
        V[x] = result >= 256 ? result - 256 : result;
    }

    void OP_8xy0() {
        V[x] = V[y];
    }

    void OP_8xy1() {
        V[x] |= V[y];
    }

    void OP_8xy2() {
        V[x] &= V[y];
    }

    void OP_8xy3() {
        V[x] ^= V[y];
    }

    void OP_8xy4() {
        var result = V[x] + V[y];
        V[x]   = result & 0x00FF;
        V[0xF] = result > 255 ? 1 : 0;
    }

    void OP_8xy5() {
        var result = V[x] - V[y];
        V[x] = result & 0xFF;
        // In Java, we can represent 0xFF as 1111111.
        // Any value above 0xFF will have 9 or more bits at the very least.
        // Any value below 0 will in Java always have 32 bits.
        // By comparing the lower 9 bits of the result with the lower 8 bits of the result we can understand if we have overflow/underflow or not.
        V[0xF] = (result & 0x1FF) == (result & 0xFF) ? 1 : 0;
    }

    void OP_8xy6() {
        var vx = V[x];
        V[x]   = vx >>> 1;
        V[0xF] = vx & 0x1;
    }

    void OP_8xy7() {
        V[x]   = (V[y] - V[x]) & 0xFF;
        V[0xF] = V[y] > V[x] ? 1 : 0;
    }

    void OP_8xyE() {
        var vx = V[x] << 1;
        V[x]   = vx & 0xFF;
        V[0xF] = (vx & 0x100) == 0x100 ? 1 : 0;
    }

    void OP_9xy0() {
        if (V[x] != V[y]) {
            PC += 2;
        }
    }

    void OP_Annn() {
        I = nnn;
    }

    void OP_Bnnn() {
        PC = V[0] + nnn;
    }

    void OP_Cxnn() {
        V[x] = new Random().nextInt(256) & nn;
    }

    void OP_Dxyn() {
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

    void OP_Ex9E() {
        if (keypad.isKeyPressed(V[x])) {
            PC += 2;
        }
    }

    void OP_ExA1() {
        if (!keypad.isKeyPressed(V[x])) {
            PC += 2;
        }
    }

    void OP_Fx07() {
        V[x] = delayTimer & 0xFF;
    }

    void OP_Fx0A() {
        isPaused = true;

        keypad.onNextKeyReleased(keypadKey -> {
            V[x]     = keypadKey.getCosmacVipKeyCode();
            isPaused = false;
        });
    }

    void OP_Fx15() {
        delayTimer = V[x];
    }

    void OP_Fx18() {
        soundTimer = V[x];
    }

    void OP_Fx1E() {
        I += V[x];
    }

    void OP_Fx29() {
        I        = V[x] * 5;
        drawFlag = true;
    }

    void OP_Fx33() {
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

    void OP_Fx55() {
        for (var i = 0; i <= x; i++) {
            memory[I + i] = V[i] & 0xFF;
        }
    }

    void OP_Fx65() {
        for (var i = 0; i <= x; i++) {
            V[i] = memory[I + i] & 0xFF;
        }
    }

    public void cycle() {
        if (isPaused) {
            return;
        }

        opcode = memory[PC] << 8 | (memory[PC + 1] & 0x00FF);
        x      = (opcode & 0x0F00) >>> 8;
        y      = (opcode & 0x00F0) >>> 4;
        n      = opcode & 0x000F;
        nn     = opcode & 0x00FF;
        nnn    = opcode & 0x0FFF;

        PC += 2;

        table[(opcode & 0xF000) >> 12].run();
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