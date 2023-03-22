package io.github.lambig.patterns;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Patternsの処理の実行結果がnullだった場合の挙動
 */

public interface WhenValueIsNull<V> extends Supplier<V> {
    static <T> WhenValueIsNull<T> returnDefaultValue(final T value) {
        return new ReturnDefaultValue<>(value);
    }

    static <T> WhenValueIsNull<T> returnNull() {
        return new ReturnNull<>();
    }

    static <T> WhenValueIsNull<T> throwRuntimeException(final String message) {
        return new ThrowRuntimeException<>(new RuntimeException(message));
    }

    static <T> WhenValueIsNull<T> throwRuntimeException(final Throwable t) {
        return new ThrowRuntimeException<>(t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ReturnDefaultValue<V> implements WhenValueIsNull<V> {
        private final V value;

        @Override
        public V get() {
            return value;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class ReturnNull<V> implements WhenValueIsNull<V> {
        @Override
        public V get() {
            return null;
        }
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ThrowRuntimeException<V> implements WhenValueIsNull<V> {
        private final RuntimeException runtimeException;

        @Override
        public V get() {
            throw runtimeException;
        }
    }

}
