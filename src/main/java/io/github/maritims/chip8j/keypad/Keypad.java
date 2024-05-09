package io.github.maritims.chip8j.keypad;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Keypad {
    private final Map<KeypadKey, Boolean> keys = Arrays.stream(KeypadKey.values()).collect(Collectors.toMap(keypadKey -> keypadKey, keypadKey -> false));
    private       Consumer<KeypadKey>     onNextKeyPressEventHandler;

    public boolean isKeyPressed(int cosmacVipKeyCode) {
        return KeypadKey.fromCosmacVipKeyCode(cosmacVipKeyCode)
                .map(keys::get)
                .orElse(false);
    }

    public void setOnNextKeyPressEventHandler(Consumer<KeypadKey> onNextKeyPressEventHandler) {
        this.onNextKeyPressEventHandler = onNextKeyPressEventHandler;
    }

    public void onKeyPressed(KeypadKey pressedKey) {
        keys.put(pressedKey, true);

        if(onNextKeyPressEventHandler != null) {
            onNextKeyPressEventHandler.accept(pressedKey);
            onNextKeyPressEventHandler = null;
        }
    }

    public void onKeyReleased(KeypadKey releasedKey) {
        keys.put(releasedKey, false);
    }
}
