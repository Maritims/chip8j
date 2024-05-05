package io.github.maritims.chip8j;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Builder<T> {
    private final T instance;

    public Builder(T instance) {
        this.instance = instance;
    }

    public Builder<T> with(Consumer<T> consumer) {
        consumer.accept(instance);
        return this;
    }

    public T build() {
        return instance;
    }

    public void andThen(Consumer<T> consumer) {
        consumer.accept(instance);
    }

    public static <U> Builder<U> of(Supplier<U> element) {
        return new Builder<>(element.get());
    }
}
