package io.github.maritims.chip8j.util;

public interface Observable {
    void registerObservers(Observer... observers);
    void removeObservers(Observer... observers);
    void notifyObservers();
}
