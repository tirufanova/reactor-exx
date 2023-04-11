package ru.alfa.mobile.reactor.overhead;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static java.lang.System.out;

public class ReadabilityTest {

    @Test
    @DisplayName("Imperative is more readable then reactive")
    public void imperativeVsReactive() {

        for (int i = 5; i < 15; i++) {
            String iStr = Integer.toString(i);
            if (iStr.length() == 1) {
                out.printf("iStr = %s\n", iStr);
            }
        }

        Flux.range(5, 10)
                .map(Object::toString)
                .filter(iStr -> iStr.length() == 1)
                .doOnNext(iStr -> out.printf("iStr = %s\n", iStr))
                .blockLast();
    }
}
