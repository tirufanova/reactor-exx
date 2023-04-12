package ru.alfa.mobile.reactor.operators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Function;


public class GroupByWithFlatMapNamesTest {

    @Test
    void completesNamesCountJustLog() {
        Flux<String> namesFlux = Flux
                .just("Carl", "Dan", "Adam", "Brian",
                        "Bruce", "Billy", "Alice");

        Function<GroupedFlux<Character, String>, Publisher<String>> countFunction =
                (gFlux) -> gFlux.log("countFunction").count().map(count ->
                        "%s %s names".formatted(count, gFlux.key()));

        namesFlux
                .log("beforeGroupBy")
                .groupBy(name -> name.charAt(0))
                .flatMap(countFunction)
                .log("afterFlatMap")
                .blockLast();
    }

    @Test
    void hangsNamesCountJustLog() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> {
                    Flux<String> namesFlux = Flux
                            .just("Carl", "Dan", "Adam", "Brian",
                                    "Bruce", "Billy", "Alice");

                    Function<GroupedFlux<Character, String>, Publisher<String>> countFunction =
                            (gFlux) -> gFlux.count().log("countFunction").map(count ->
                                    "%s %s names".formatted(count, gFlux.key()));

                    int prefetch = 3;
                    int concurrency = 3;

                    namesFlux
                            .log("beforeGroupBy")
                            .groupBy(name -> name.charAt(0), prefetch)
                            .flatMap(countFunction, concurrency)
                            .log("afterFlatMap")
                            .blockLast(Duration.ofSeconds(5));
                },
                "Timeout on blocking read for 5000000000 NANOSECONDS");
    }

    @Test
    void verifyCompletePassesNamesCount() {
        Flux<String> namesFlux = Flux
                .just("Carl", "Dan", "Adam", "Brian",
                        "Bruce", "Billy", "Alice");
        Function<GroupedFlux<Character, String>, Publisher<String>> countFunction =
                (gFlux) -> gFlux.count().map(count ->
                        "%s %s names".formatted(count, gFlux.key()));

        Flux<String> countedNamesFlux = namesFlux
                .groupBy(name -> name.charAt(0))
                .flatMap(countFunction);

        StepVerifier.create(countedNamesFlux)
                .expectNext("2 A names")
                .expectNext("3 B names")
                .expectNext("1 C names")
                .expectNext("1 D names")
                .verifyComplete();
    }

    @Test
    void verifyTimeoutPassesNamesCount() {
        Flux<String> namesFlux = Flux
                .just("Carl", "Dan", "Adam", "Brian",
                        "Bruce", "Billy", "Alice");
        Function<GroupedFlux<Character, String>, Publisher<String>> countNames =
                (gFlux) -> gFlux.count().map(count ->
                        "%s %s names".formatted(count, gFlux.key()));

        int prefetch = 3;
        int concurrency = 3;
        Flux<String> countedNamesFlux = namesFlux
                .groupBy(name -> name.charAt(0), prefetch)
                .flatMap(countNames, concurrency);

        StepVerifier.create(countedNamesFlux)
                .verifyTimeout(Duration.ofSeconds(5));
    }
}
