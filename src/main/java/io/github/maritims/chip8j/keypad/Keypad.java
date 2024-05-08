package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Keypad {
    private final Map<KeypadKey, Boolean> keys = Arrays.stream(KeypadKey.values()).collect(Collectors.toMap(keypadKey -> keypadKey, keypadKey -> false));

    public void onKeyToggle(KeypadKey pressedKey) {
        keys.put(pressedKey, !keys.get(pressedKey));
    }
}
