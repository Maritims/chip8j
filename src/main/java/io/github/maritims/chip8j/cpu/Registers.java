package io.github.maritims.chip8j.cpu;

import java.util.function.IntUnaryOperator;

public class Registers {
    private final int[] V = new int[16];

    public int size() {
        return V.length;
    }

    public int get(int position) {
        return V[position];
    }

    public void set(int position, int value) {
        if(value < 0) {
            throw new IllegalArgumentException("Negative value being placed in register: " + value);
        }

        V[position] = value;
    }

    public void apply(int position, IntUnaryOperator func) {
        V[position] = func.applyAsInt(V[position]);
    }
}
