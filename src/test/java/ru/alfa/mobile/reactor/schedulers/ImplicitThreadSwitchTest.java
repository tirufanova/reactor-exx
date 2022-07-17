package ru.alfa.mobile.reactor.schedulers;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.function.Function;

import static java.time.Duration.ofMillis;

public class ImplicitThreadSwitchTest {

    /**
     * Смена потока не всегда происходит явно через операторы subscribeOn и publishOn.
     * Например, при использовании оператора flatMap (merge), тред реактивного потока может поменяться.
     * Но это не значит, что будет происходить конкурентная обработка! Спека гарантирует потокобезопасность.
     */
    @Test
    void flatMapThread() {
        Function<String, Flux<Long>> supplier =
                (prefix) ->
                        Flux.interval(ofMillis(10))
                                .take(5)
                                .log(prefix + "supplier");

        Flux.merge(
                        supplier.apply("1-"),
                        supplier.apply("2-"),
                        supplier.apply("3-"),
                        supplier.apply("4-")
                )
                .log("merge")
                .blockLast();
    }
}
