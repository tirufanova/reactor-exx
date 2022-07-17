package ru.alfa.mobile.reactor.operators;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.test.StepVerifier;

import java.time.Duration;


/**
 * Реактивная дока приводит рекомендации по использованию оператора groupBy
 * <a href="https://projectreactor.io/docs/core/release/reference/#advanced-three-sorts-batching">https://projectreactor.io/docs/core/release/reference/#advanced-three-sorts-batching</a>
 * <p>
 * StepVerifier.create(
 * Flux.just(1, 3, 5, 2, 4, 6, 11, 12, 13)
 * .groupBy(i -> i % 2 == 0 ? "even" : "odd")
 * .concatMap(g -> g.defaultIfEmpty(-1) //if empty groups, show them
 * .map(String::valueOf) //map to string
 * .startWith(g.key())) //start with the group's key
 * )
 * .expectNext("odd", "1", "3", "5", "11", "13")
 * .expectNext("even", "2", "4", "6", "12")
 * .verifyComplete();
 * <p>
 * Grouping is best suited for when you have a medium to low number of groups.
 * The groups must also imperatively be consumed (such as by a flatMap) so that
 * groupBy continues fetching data from upstream and feeding more groups.
 * Sometimes, these two constraints multiply and lead to hangs,
 * such as when you have a high cardinality and
 * the concurrency of the flatMap consuming the groups is too low.
 */
public class GroupByWithFlatMapTest {

    /**
     * Все элементы успешно проходят через цепочку
     */
    @Test
    void verifyCompletePasses_1() {
//        int flatMapConcurrency = 2; // default 256
//        int groupByPrefetch = 3;    // default 256
//        Duration stepVerifierTimeout = Duration.ofSeconds(3);

        Flux<String> publisher = Flux
                .just("Adam", "Ann", "Aria", "Brian",
                        "Agata", "Alice", "Art", "Abe", "Alex",
                        "Carl", "Chad", "Dan", "Ariel")
                .groupBy(name -> "Hi " + name.charAt(0) + "!")
                .flatMap((GroupedFlux<String, String> gFlux) ->
                        gFlux.startWith(gFlux.key()));

        StepVerifier.create(publisher)
                .expectNext("Hi A!", "Adam", "Ann", "Aria")
                .expectNext("Hi B!", "Brian")
                .expectNext("Agata", "Alice", "Art", "Abe", "Alex")
                .expectNext("Hi C!", "Carl", "Chad")
                .expectNext("Hi D!", "Dan")
                .expectNext("Ariel")
                .verifyComplete();
    }


    /**
     * Все элементы успешно проходят через цепочку,
     * но поменялся их порядок,
     * так как flatMap одновременно обрабатывает только 2 реактивных стрима
     */
    @Test
    void verifyCompletePassesWithLowConcurrency_2() {
        int flatMapConcurrency = 2;
//        int groupByPrefetch = 3;
//        Duration stepVerifierTimeout = Duration.ofSeconds(3);

        Flux<String> publisher = Flux
                .just("Adam", "Ann", "Aria", "Brian",
                        "Agata", "Alice", "Art", "Abe", "Alex",
                        "Carl", "Chad", "Dan", "Ariel")
                .groupBy(name -> "Hi " + name.charAt(0) + "!")
                .flatMap((GroupedFlux<String, String> gFlux) ->
                        gFlux.startWith(gFlux.key()), flatMapConcurrency);

        StepVerifier.create(publisher)
                .expectNext("Hi A!", "Adam", "Ann", "Aria")
                .expectNext("Hi B!", "Brian")
                .expectNext("Agata", "Alice", "Art", "Abe", "Alex")
                .expectNext("Ariel")
                .expectNext("Hi C!", "Carl", "Chad")
                .expectNext("Hi D!", "Dan")
                .verifyComplete();
    }

    /**
     * Теперь тест бы повис, если бы мы его явно не прервали,
     * так как в буфере groupBy слишком мало элементов,
     * и он теперь не знает об окончании потоков
     */
    @Test
    void hangsWithSmallPrefetch_3() throws InterruptedException {
        var threadForTimeout = new Thread(() -> {
            int flatMapConcurrency = 2;
            int groupByPrefetch = 3;
//        Duration stepVerifierTimeout = Duration.ofSeconds(3);

            Flux<String> publisher = Flux
                    .just("Adam", "Ann", "Aria", "Brian",
                            "Agata", "Alice", "Art", "Abe", "Alex",
                            "Carl", "Chad", "Dan", "Ariel")
                    .groupBy(name -> "Hi " + name.charAt(0) + "!", groupByPrefetch)
                    .flatMap((GroupedFlux<String, String> gFlux) ->
                            gFlux.startWith(gFlux.key()), flatMapConcurrency);

            StepVerifier.create(publisher)
                    .expectNext("Hi A!", "Adam", "Ann", "Aria")
                    .expectNext("Hi B!", "Brian")
                    .expectNext("Agata", "Alice", "Art", "Abe", "Alex")
                    .expectNext("Ariel")
                    .expectNext("Hi C!", "Carl", "Chad")
                    .expectNext("Hi D!", "Dan")
                    .verifyComplete();
        });
        Thread.sleep(3000);
        threadForTimeout.interrupt();
    }

    /**
     * Верифицируем таймаут средствами io.projectreactor:reactor-test
     */
    @Test
    void verifyTimeoutPasses_4() {
        int flatMapConcurrency = 2;
        int groupByPrefetch = 3;
        Duration stepVerifierTimeout = Duration.ofSeconds(3);

        Flux<String> publisher = Flux
                .just("Adam", "Ann", "Aria", "Brian",
                        "Agata", "Alice", "Art", "Abe", "Alex",
                        "Carl", "Chad", "Dan", "Ariel")
                .groupBy(name -> "Hi " + name.charAt(0) + "!", groupByPrefetch)
                .flatMap((GroupedFlux<String, String> gFlux) ->
                        gFlux.startWith(gFlux.key()), flatMapConcurrency);

        StepVerifier.create(publisher)
                .expectNext("Hi A!", "Adam", "Ann", "Aria")
                .expectNext("Hi B!", "Brian")
                .expectNext("Agata", "Alice", "Art", "Abe", "Alex")
//                .expectNext("Ariel")
//                .expectNext("Hi C!", "Carl", "Chad")
//                .expectNext("Hi D!", "Dan")
                .verifyTimeout(stepVerifierTimeout);
    }

}
