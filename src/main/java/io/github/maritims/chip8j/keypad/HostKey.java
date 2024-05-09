package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Optional;

public enum HostKey {
    ONE((char) 49, KeypadKey.ONE),
    TWO((char) 50, KeypadKey.TWO),
    THREE((char) 51, KeypadKey.THREE),
    FOUR((char) 52, KeypadKey.C),
    Q((char) 81, KeypadKey.FOUR),
    W((char) 87, KeypadKey.FIVE),
    E((char) 69, KeypadKey.SIX),
    R((char) 82, KeypadKey.D),
    A((char) 66, KeypadKey.SEVEN),
    S((char) 83, KeypadKey.EIGHT),
    D((char) 68, KeypadKey.NINE),
    F((char) 70, KeypadKey.E),
    Z((char) 90, KeypadKey.A),
    X((char) 89, KeypadKey.ZERO),
    C((char) 67, KeypadKey.B),
    V((char) 86, KeypadKey.F);

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
