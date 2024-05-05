package io.github.maritims.chip8j;

public interface Observable {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
