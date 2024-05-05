package io.github.maritims.chip8j.cpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

enum Opcode {
    CLS(0x00E0, state -> {
        state.clearPixelBuffer();
        state.setDrawFlag(true);
    }),
    RET(0x00EE, state -> {
        state.updateProgramCounter(pc -> state.popFromStack() + 2);
        state.setDrawFlag(true);
    }),
    JP_NNN(0x1000, state -> state.setProgramCounter(state.getNnn())),
    CALL_NNN(0x2000, state -> {
        state.pushToStack(state.getProgramCounter() - 2);
        state.setProgramCounter(state.getNnn());
    }),
    SE_VX_BYTE(0x3000, state -> {
        if (state.getRegisters().get(state.getX()) == state.getNn()) {
            state.updateProgramCounter(pc -> pc + 2);
        }
    }),
    SNE_VX_BYTE(0x4000, state -> {
        if (state.getRegisters().get(state.getX()) != state.getNn()) {
            state.updateProgramCounter(pc -> pc + 2);
        }
    }),
    SE_VX_VY(0x5000, state -> {
        var vx = state.getRegisters().get(state.getX());
        var vy = state.getRegisters().get(state.getY());
        if (vx == vy) {
            state.updateProgramCounter(pc -> pc + 2);
        }
    }),
    LD_VX_BYTE(0x6000, state -> state.getRegisters().set(state.getX(), state.getNn())),
    ADD_VX_BYTE(0x7000, state -> state.getRegisters().apply(state.getX(), vx -> {
        var result = vx + state.getNn();
        return result >= 256 ? result - 256 : result;
    })),
    LD_VX_VY(0x8000, state -> state.getRegisters().set(state.getX(), state.getRegisters().get(state.getY()))),
    OR_VX_VY(0x8001, state -> state.getRegisters().apply(state.getX(), vx -> vx | state.getRegisters().get(state.getY()))),
    AND_VX_VY(0x8002, state -> state.getRegisters().apply(state.getX(), vx -> vx & state.getRegisters().get(state.getY()))),
    XOR_VX_VY(0x8003, state -> state.getRegisters().apply(state.getX(), vx -> vx ^ state.getRegisters().get(state.getY()))),
    ADD_VX_VY(0x8004, state -> state.getRegisters().apply(state.getX(), vx -> {
        var result = vx + state.getRegisters().get(state.getY());
        state.getRegisters().set(0xF, result >= 256 ? 1 : 0);
        return result & 0xFF;
    })),
    SUB_VX_VY(0x8005, state -> state.getRegisters().apply(state.getX(), vx -> {
        if (vx > state.getRegisters().get(state.getY())) {
            state.getRegisters().set(0xF, 1);
        } else if (state.getRegisters().get(state.getY()) > vx) {
            state.getRegisters().set(0xF, 0);
        }
        return (vx - state.getRegisters().get(state.getY())) & 0xFF;
    })),
    SHR_VX_VY(0x8006, state -> {
        state.getRegisters().set(0xF, (state.getRegisters().get(state.getX()) & 0b0000_0001) == 0 ? 0 : 1);
        state.getRegisters().apply(state.getX(), vx -> vx / 2);
    }),
    SUBN_VX_VY(0x8007, state -> {
        if (state.getRegisters().get(state.getY()) > state.getRegisters().get(state.getX())) {
            state.getRegisters().set(0xF, 1);
        } else if (state.getRegisters().get(state.getX()) > state.getRegisters().get(state.getY())) {
            state.getRegisters().set(0xF, 0);
        }
        state.getRegisters().apply(state.getX(), vx -> (state.getRegisters().get(state.getY()) - vx) & 0xFF);
    }),
    SHL_VX_VY(0x800E, state -> {
        state.getRegisters().set(0xF, (state.getRegisters().get(state.getX()) & 0b1000_0000) == 0 ? 0 : 1);
        state.getRegisters().apply(state.getX(), vx -> (vx * 2) & 0xFF);
    }),
    SNE_VX_VY(0x9000, state -> {
        if (state.getRegisters().get(state.getX()) != state.getRegisters().get(state.getY())) {
            state.updateProgramCounter(pc -> pc + 2);
        }
    }),
    LD_I_ADDR(0xA000, state -> state.setIndex(state.getNnn())),
    JP_V0_ADDR(0xB000, state -> state.setProgramCounter(state.getRegisters().get(state.getX()) + state.getNn())),
    RND_VX_BYTE(0xC000, state -> state.getRegisters().set(state.getX(), new Random().nextInt(256) & state.getNn())),
    DRW_VX_VY_N(0xD000, state -> {
        var vx = state.getRegisters().get(state.getX());
        var vy = state.getRegisters().get(state.getY());

        state.getRegisters().set(0xF, 0);
        for (var row = 0; row < state.getN(); row++) {
            var pixel = state.getByteFromMemory(state.getIndex() + row);

            for (var column = 0; column < 8; column++) {
                var isPixelSet = (pixel & 0b1000_0000) > 0;

                if (isPixelSet) {
                    var targetX = vx + column;
                    var targetY = vy + row;

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
                        state.getRegisters().set(0xF, 1);
                    }

                    state.updatePixelInBuffer(locationInPixelBuffer, pixelInBuffer -> pixelInBuffer ^ 1);
                }

                // Move all bits one step to the left in preparation for checking if the next bit is set.
                pixel <<= 1;
            }

            state.setDrawFlag(true);
        }
    }),
    SKP_VX(0xE09E, state -> {
    }),
    SKNP_VX(0xE0A1, state -> {
    }),
    LD_VX_DT(0xF007, state -> state.getRegisters().set(state.getX(), state.getDelayTimer())),
    LD_VX_K(0xF00A, state -> {
        throw new UnsupportedOperationException("Not implemented yet");
    }),
    LD_DT_VX(0xF015, state -> state.setDelayTimer(state.getRegisters().get(state.getX()))),
    LD_ST_VX(0xF018, state -> state.setSoundTimer(state.getRegisters().get(state.getX()))),
    ADD_I_VX(0xF01E, state -> state.updateIndex(i -> i + state.getRegisters().get(state.getX()))),
    LD_F_VX(0xF029, state -> {
        state.updateIndex(i -> state.getRegisters().get(state.getX()) * 5);
        state.setDrawFlag(true);
    }),
    LD_B_VX(0xF033, state -> {
        var number = state.getRegisters().get(state.getX());
        var digits = new LinkedList<Integer>();
        while (number > 0) {
            var digit = number % 10;
            digits.push(digit);
            number /= 10;
        }
        for (var i = 0; i < digits.size(); i++) {
            state.setByteInMemory(state.getIndex() + i, digits.get(i).byteValue());
        }
    }),
    LD_I_VX(0xF055, state -> {
        for (var i = 0; i <= state.getX(); i++) {
            state.setByteInMemory(state.getIndex() + i, state.getRegisters().get(i));
        }
    }),
    LD_VX_I(0xF065, state -> {
        for (var i = 0; i <= state.getX(); i++) {
            state.getRegisters().set(i, state.getByteFromMemory(state.getIndex() + i) & 0xFF);
        }
    });

    private static final Logger log = LoggerFactory.getLogger(Opcode.class);

    private final int             bytes;
    private final Consumer<State> consumer;

    Opcode(int bytes, Consumer<State> consumer) {
        this.bytes      = bytes;
        this.consumer   = consumer;
    }

    void execute(State state) {
        log.info("Executing opcode: {}", String.format("%04X", state.getRawOpcode() & 0xFFFF));
        this.consumer.accept(state);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    static Optional<Opcode> byMask(int bytes, int mask) {
        return Arrays.stream(values())
                .filter(value -> value.bytes == (bytes & mask))
                .findFirst();
    }
}