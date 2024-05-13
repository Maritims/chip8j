package io.github.maritims.chip8j.keypad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Keypad {
    private static final Logger log = LoggerFactory.getLogger(Keypad.class);

    private final Map<KeypadKey, Boolean> keys = Arrays.stream(KeypadKey.values()).collect(Collectors.toMap(keypadKey -> keypadKey, keypadKey -> false));
    private       Consumer<KeypadKey>     onNextKeyReleased;

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
        if(onNextKeyReleased != null) {
            log.info("Triggering event handler for {}", releasedKey);
            onNextKeyReleased.accept(releasedKey);
            onNextKeyReleased = null;
        }
    }

    public void onNextKeyReleased(Consumer<KeypadKey> onNextKeyReleased) {
        log.info("Setting event handler");
        this.onNextKeyReleased = onNextKeyReleased;
    }
}
