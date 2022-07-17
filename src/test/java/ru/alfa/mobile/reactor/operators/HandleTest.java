package ru.alfa.mobile.reactor.operators;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;

/**
 * Два примера кода делают одно и то же с разным количеством реактивных обёрток
 */
public class HandleTest {
    /**
     * Здесь создастся 8 обёрток (по 2 на каждый filter и map)
     */
    @Test
    void withoutHandle() {
        Flux.just(-1, 500, -20, 55, 100, 14)
                .filter(i -> i % 2 == 0)
                .filter(Character::isAlphabetic)
                .map(Character::toChars)
                .map(Arrays::toString)
                .subscribe(System.out::println);
    }

    /**
     * Здесь создастся 2 обёртки только для оператора handle
     */
    @Test
    void withHandle() {
        Flux.just(-1, 500, -20, 55, 100, 14)
                .handle((i, sink) -> {
                    if (i % 2 == 0 && Character.isAlphabetic(i)) {
                        char[] chars = Character.toChars(i);
                        sink.next(Arrays.toString(chars));
                    }
                })
                .subscribe(System.out::println);
    }
}

