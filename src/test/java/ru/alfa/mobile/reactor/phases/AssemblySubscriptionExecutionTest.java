package ru.alfa.mobile.reactor.phases;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class AssemblySubscriptionExecutionTest {

    @Test
    public void assemble() {
        Flux<String> assemble =
                Flux.range(5, 100).hide()
                        .map(Object::toString)
                        .filter(s -> s.length() == 1)
                        .take(3);

        assemble.subscribe();
    }

    @Test
    public void subscribe() {
        Flux.range(5, 100).hide()
                .map(Object::toString)
                .filter(s -> s.length() == 1)
                .take(3)
                .blockLast();
    }

    @Test
    public void execute() {
        Flux.range(5, 100).hide()
                .map(Object::toString)
                .filter(s -> s.length() == 1)
                .log("filtered")
                .take(3)
                .log("   taken")
                .blockLast();
    }
}
