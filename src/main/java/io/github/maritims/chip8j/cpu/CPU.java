package io.github.maritims.chip8j.cpu;

import io.github.maritims.chip8j.Keypad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class CPU {
    private static final Logger log = LoggerFactory.getLogger(CPU.class);

    private final State  state;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Keypad keypad;

    public CPU(State state, Keypad keypad) {
        this.state  = state;
        this.keypad = keypad;
    }

    public State getState() {
        return state;
    }

    protected int fetch() {
        return state.getNextOpcode();
    }

    void execute(int rawOpcode) {
        state.updateProgramCounter(pc -> pc + 2);

        var x   = (rawOpcode & 0x0F00) >>> 8;
        var y   = (rawOpcode & 0x00F0) >>> 4;
        var n   = rawOpcode & 0x000F;
        var nn  = rawOpcode & 0x00FF;
        var nnn = rawOpcode & 0x0FFF;

        var opcode = Stream.of(0xF0FF, 0xF00F, 0xF000)
                .map(mask -> Opcode.byMask(rawOpcode, mask))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow();
        state.updateInstruction(rawOpcode, opcode, x, y, n, nn, nnn);

        opcode.execute(state);
    }

    public void loadProgram(byte[] program) {
        for (var i = 0; i < program.length; i++) {
            state.setByteInMemory(0x200 + i, program[i] & 0xFF);
        }
    }

    public void cycle() {
        var opcode = fetch();
        execute(opcode);

        if (state.getDelayTimer() > 0) {
            state.updateDelayTimer(dt -> dt - 1);
        }

        if (state.getSoundTimer() > 0) {
            state.updateSoundTimer(st -> st - 1);
        }

        state.notifyObservers();
    }
}