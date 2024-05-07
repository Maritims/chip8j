package io.github.maritims.chip8j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class Keypad {
    private static final Logger log = LoggerFactory.getLogger(Keypad.class);

    public KeypadButton getPressedButton() {
        return pressedButton;
    }

    public void setPressedButton(KeypadButton pressedButton) {
        log.info("Setting pressed button to {}", pressedButton);
        this.pressedButton = pressedButton;
    }

    public enum KeypadButton {
        ONE(0x1, HostKey.ONE),
        TWO(0x2, HostKey.TWO),
        THREE(0x3, HostKey.TWO),
        C(0xC, HostKey.FOUR),

        FOUR(0x4, HostKey.Q),
        FIVE(0x5, HostKey.W),
        SIX(0x6, HostKey.E),
        D(0xD, HostKey.R),

        SEVEN(0x7, HostKey.A),
        EIGHT(0x8, HostKey.S),
        NINE(0x9, HostKey.D),
        E(0xE, HostKey.F),

        A(0xA, HostKey.Z),
        ZERO(0x0, HostKey.X),
        B(0xB, HostKey.C),
        F(0xF, HostKey.V);

        KeypadButton(int cosmacVipKeyCode, HostKey hostKey) {
            this.cosmacVipKeyCode = cosmacVipKeyCode;
            this.hostKey          = hostKey;
        }

        private final int     cosmacVipKeyCode;
        private final HostKey hostKey;

        public int getCosmacVipKeyCode() {
            return cosmacVipKeyCode;
        }

        public HostKey getHostKey() {
            return hostKey;
        }
    }

    enum HostKey {
        ONE(49, KeypadButton.ONE),
        TWO(50, KeypadButton.TWO),
        THREE(51, KeypadButton.THREE),
        FOUR(52, KeypadButton.C),
        Q(81, KeypadButton.FOUR),
        W(87, KeypadButton.FIVE),
        E(69, KeypadButton.SIX),
        R(82, KeypadButton.D),
        A(66, KeypadButton.SEVEN),
        S(83, KeypadButton.EIGHT),
        D(68, KeypadButton.NINE),
        F(70, KeypadButton.E),
        Z(90, KeypadButton.A),
        X(89, KeypadButton.ZERO),
        C(67, KeypadButton.B),
        V(86, KeypadButton.F);

        HostKey(int asciiCode, KeypadButton keypadButton) {
            this.asciiCode    = asciiCode;
            this.keypadButton = keypadButton;
        }

        private final int          asciiCode;
        private final KeypadButton keypadButton;

        public int getAsciiCode() {
            return asciiCode;
        }

        public KeypadButton getKeypadButton() {
            return keypadButton;
        }

        public static Optional<HostKey> fromAsciiCode(int asciiCode) {
            return Arrays.stream(values())
                    .filter(value -> value.getAsciiCode() == asciiCode)
                    .findFirst();
        }
    }

    private KeypadButton pressedButton;
}