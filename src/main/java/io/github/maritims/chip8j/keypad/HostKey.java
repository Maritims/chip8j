package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Optional;

public enum HostKey {
    ONE(49, KeypadKey.ONE),
    TWO(50, KeypadKey.TWO),
    THREE(51, KeypadKey.THREE),
    FOUR(52, KeypadKey.C),
    Q(81, KeypadKey.FOUR),
    W(87, KeypadKey.FIVE),
    E(69, KeypadKey.SIX),
    R(82, KeypadKey.D),
    A(66, KeypadKey.SEVEN),
    S(83, KeypadKey.EIGHT),
    D(68, KeypadKey.NINE),
    F(70, KeypadKey.E),
    Z(90, KeypadKey.A),
    X(89, KeypadKey.ZERO),
    C(67, KeypadKey.B),
    V(86, KeypadKey.F);

    HostKey(int asciiCode, KeypadKey keypadButton) {
        this.asciiCode    = asciiCode;
        this.keypadButton = keypadButton;
    }

    private final int       asciiCode;
    private final KeypadKey keypadButton;

    public int getAsciiCode() {
        return asciiCode;
    }

    public KeypadKey getKeypadKey() {
        return keypadButton;
    }

    public static Optional<HostKey> fromAsciiCode(int asciiCode) {
        return Arrays.stream(values())
                .filter(value -> value.getAsciiCode() == asciiCode)
                .findFirst();
    }
}
