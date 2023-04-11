package ru.alfa.mobile.reactor.schedulers;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Function;

import static java.time.Duration.ofMillis;

public class ImplicitThreadSwitchTest {

    @Test
    void delayElementsThread() {
        Flux.range(0, 3)
                .log("before")
                .delayElements(Duration.ofNanos(5))
                .log("after")
                .take(2)
                .blockLast();
    }

    @Test
    void mergeDifferentThreads() {
        Flux<Integer> parallelThreadFlux =
                Flux.range(10, 2)
                        .publishOn(Schedulers.parallel())
                        .log("parallel");
        Flux<Integer> testWorkerThreadFlux =
                Flux.range(20, 2)
                        .log("testWorker");

        Flux.merge(parallelThreadFlux, testWorkerThreadFlux)
                .log("merge")
                .blockLast();
    }

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
