package io.github.maritims.chip8j;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EmulatorTest {

    public static Stream<Arguments> onKeyPressed() {
        return Stream.of(
                Arguments.of('1', 0x1),
                Arguments.of('2', 0x2),
                Arguments.of('3', 0x3),
                Arguments.of('4', 0xC),

                Arguments.of('Q', 0x4),
                Arguments.of('W', 0x5),
                Arguments.of('E', 0x6),
                Arguments.of('R', 0xD),

                Arguments.of('A', 0x7),
                Arguments.of('S', 0x8),
                Arguments.of('D', 0x9),
                Arguments.of('F', 0xE),

                Arguments.of('Z', 0xA),
                Arguments.of('X', 0x0),
                Arguments.of('C', 0xB),
                Arguments.of('V', 0xF)
        );
    }

    @ParameterizedTest
    @MethodSource
    void onKeyPressed(char asciiCode, int expectedCosmacVipKeyCode) {
        // arrange
        Consumer<int[]> onDrawEventHandler = (i) -> {};
        var             emulator           = new Emulator(onDrawEventHandler);

        // act
        emulator.onKeyPressed(asciiCode);

        // assert
        assertTrue(emulator.getKeypad().isKeyPressed(expectedCosmacVipKeyCode));
    }
}