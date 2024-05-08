package io.github.maritims.chip8j.keypad;

public enum KeypadKey {
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

    KeypadKey(int cosmacVipKeyCode, HostKey hostKey) {
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
