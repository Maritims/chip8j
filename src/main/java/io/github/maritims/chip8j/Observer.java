package io.github.maritims.chip8j;

public interface Observer<T extends Observable> {
    void update(T observable);
}
