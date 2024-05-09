package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Optional;

public enum KeypadKey {
    ONE(0x1),
    TWO(0x2),
    THREE(0x3),
    C(0xC),

    FOUR(0x4),
    FIVE(0x5),
    SIX(0x6),
    D(0xD),

    SEVEN(0x7),
    EIGHT(0x8),
    NINE(0x9),
    E(0xE),

    A(0xA),
    ZERO(0x0),
    B(0xB),
    F(0xF);

    KeypadKey(int cosmacVipKeyCode) {
        this.cosmacVipKeyCode = cosmacVipKeyCode;
    }

    private final int cosmacVipKeyCode;

    public int getCosmacVipKeyCode() {
        return cosmacVipKeyCode;
    }

    public static Optional<KeypadKey> fromCosmacVipKeyCode(int cosmacVipKeyCode) {
        return Arrays.stream(values())
                .filter(keypadKey -> keypadKey.getCosmacVipKeyCode() == cosmacVipKeyCode)
                .findFirst();
    }
}
