package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Keypad {
    private final Map<KeypadKey, Boolean> keys = Arrays.stream(KeypadKey.values()).collect(Collectors.toMap(keypadKey -> keypadKey, keypadKey -> false));

    public boolean isKeyPressed(int cosmacVipKeyCode) {
        return KeypadKey.fromCosmacVipKeyCode(cosmacVipKeyCode)
                .map(keys::get)
                .orElse(false);
    }

    public void onKeyPressed(KeypadKey pressedKey) {
        keys.put(pressedKey, true);
    }

    public void onKeyReleased(KeypadKey releasedKey) {
        keys.put(releasedKey, false);
    }
}
