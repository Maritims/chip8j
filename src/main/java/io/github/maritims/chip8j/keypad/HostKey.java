package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Optional;

public enum HostKey {
    ONE((char) 49, KeypadKey.ONE),
    TWO((char) 50, KeypadKey.TWO),
    THREE((char) 51, KeypadKey.THREE),
    FOUR((char) 52, KeypadKey.C),
    Q((char) 113, KeypadKey.FOUR),
    W((char) 119, KeypadKey.FIVE),
    E((char) 101, KeypadKey.SIX),
    R((char) 114, KeypadKey.D),
    A((char) 97, KeypadKey.SEVEN),
    S((char) 115, KeypadKey.EIGHT),
    D((char) 100, KeypadKey.NINE),
    F((char) 102, KeypadKey.E),
    Z((char) 122, KeypadKey.A),
    X((char) 120, KeypadKey.ZERO),
    C((char) 99, KeypadKey.B),
    V((char) 118, KeypadKey.F);

    HostKey(char asciiCode, KeypadKey keypadButton) {
        this.asciiCode    = asciiCode;
        this.keypadButton = keypadButton;
    }

    private final char      asciiCode;
    private final KeypadKey keypadButton;

    public int getAsciiCode() {
        return asciiCode;
    }

    public KeypadKey getKeypadKey() {
        return keypadButton;
    }

    public static Optional<HostKey> fromAsciiCode(char asciiCode) {
        return Arrays.stream(values())
                .filter(value -> value.getAsciiCode() == asciiCode)
                .findFirst();
    }
}
