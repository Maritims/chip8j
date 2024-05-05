package io.github.maritims.chip8j.cpu;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

class PixelBuffer {
    private final int[] pixelBuffer;

    PixelBuffer(int columns, int rows) {
        pixelBuffer = new int[columns * rows];
    }

    void clear() {
        Arrays.fill(pixelBuffer, 0);
    }

    void updatePixel(int position, IntUnaryOperator func) {
        pixelBuffer[position] = func.applyAsInt(pixelBuffer[position]);
    }

    public int[] getPixels() {
        var returnBuffer = new int[pixelBuffer.length];
        System.arraycopy(pixelBuffer, 0, returnBuffer, 0, pixelBuffer.length);
        return returnBuffer;
    }
}
