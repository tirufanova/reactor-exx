package ru.alfa.mobile.reactor.operators;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static java.lang.System.out;

public class ZipTest {
    /**
     * Код напечатает строку
     * Orange costs 100
     */
    @Test
    void zipNonEmptyStreams() {
        var productInfo = Mono.just("Orange");
        var productPrice = Mono.just(100);

        productInfo.zipWith(productPrice, "%s costs %s"::formatted)
                .subscribe(out::printf);
    }

    /**
     * Код не напечатает ничего
     */
    @Test
    void zipWithEmptyStream() {
        var productInfo = Mono.just("Orange");
        var productPrice = Mono.empty();

        productInfo.zipWith(productPrice, "%s costs %s"::formatted)
                .subscribe(out::printf);
    }
}