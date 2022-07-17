package ru.alfa.mobile.reactor.schedulers;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import static java.time.Duration.ofMillis;

public class PublishOnSubscribeOnTest {

    /**
     * publishOn модифицирует поток после себя в момент исполнения.
     * Необходимо использовать непосредственно перед оператором,
     * который хотим переключить на новый поток
     */
    @Test
    void publishOn() {
        Flux.range(0, 3)
                .log("before")
                .publishOn(
                        Schedulers.parallel())
                .log("after")
                .take(2)
                .blockLast();
    }

    /**
     * subscribeOn модифицирует перед собой в момент подписки.
     * Необходимо использовать непосредственно после источника данных
     */
    @Test
    void subscribeOn() {
        Flux.range(0, 3)
                .log("before")
                .subscribeOn(
                        Schedulers.parallel())
                .log("after")
                .take(2)
                .blockLast();
    }


    /**
     * Ниже пример с несколькими вызовами publishOn.
     * Изначально последовательность генерируется через Flux.interval,
     * он работает на шедулере parallel.
     * Этот тред будет использоваться до ближайшего вызова publishOn,
     * где исполнение переключится на тред шедулера single.
     * Тот в свою очередь будет работать до следующего переключения на boundedElastic scheduler.
     * <p>
     * Если посмотреть на логи, то видно, что часть событий
     * (подписка subscribe и первый backpressure request) исполняются не на шедулерах,
     * а на родительском треде (у нас это Test worker фреймворка jupiter),
     * в котором был запущен стрим (то есть произошла сама подписка, у нас это операция blockLast()).
     * Так происходит потому, что смена шедулера выполняется только на этапе Runtime.
     */
    @Test
    void publishOnInterval() {
        Flux.interval(ofMillis(1))
                .map(Object::toString)
                .log("thread.interval")
                .publishOn(Schedulers.single())
                .map(Long::parseLong)
                .log("thread.signal")
                .publishOn(Schedulers.boundedElastic())
                .map(Object::toString)
                .log("thread.boundedElastic")
                .take(300).blockLast();
    }

    /**
     * subscribeOn модифицирует все, что выше себя. Потому что изменяет поток на этапе подписки.
     * Поэтому события subscribe и request происходят на потоке mySingle-1.
     * Но дальнейшие события onNext тоже происходят на этом же потоке, хотя они стоят ниже оператора subscribeOn!
     * Это происходит потому, что других смен потока в цепочке вызовов не происходило,
     * а следовательно вся дальнейшая обработка пойдет на том же потоке
     */
    @Test
    void rangeSubscribeOn() {
        Flux.range(0, 2)
                .log("before.subscribeOn")
                .subscribeOn(Schedulers.single())
                .log("after.subscribeOn")
                .take(5).blockLast();
    }

    /**
     * Этот пример отличается от предыдущего тем, что последовательность элементов создается через Flux.interval().
     * Но если посмотреть лог теста, то выяснится, что только подписка и первый request
     * произошли на пуле mySingle-1, который мы попросили. Остальные события onNext и cancel идут на пуле parallel-1.
     * Все потому, что внутри себя метод Flux.interval() вызывает ещё один subscribeOn на parallel пуле.
     * Поскольку смена потока для subscribeOn идет на этапе подписки, то есть снизу вверх по цепочке,
     * parallel пул оказывается последним примененным, и вся последующая обработка будет проходить на его потоке.
     */
    @Test
    void intervalSubscribeOn() {
        Flux.interval(ofMillis(10))
                .log("thread.before.mySingle")
                .subscribeOn(Schedulers.newSingle("mySingle"))
                .take(5).blockLast();
    }

    /**
     * Для операторов publishOn и subscribeOn есть правило:
     * subscribeOn должен находиться как можно ближе к источнику
     * (условно, следующим оператором после источника в цепочке),
     * publishOn как можно ближе к операции обработки,
     * которую ходим переключить на новый пул (непосредственно перед самой операцией).
     * <p>
     * Первые записи в логе идут все на том же потоке Test worker,
     * так как они происходят на этапе подписки до вызова subscribeOn.
     * Дальше уже идут сигналы subscribe и request на потоке parallel-1,
     * как мы и заказывали в subscribeOn. И в момент исполнения поток переключается на boundedElastic
     * <p>
     * Отдельно хочется добавить, что не смотря на использование пулов boundedElastic и
     */
    @Test
    void subscribeOnAndPublishOn() {
        Flux.range(0, 3)
                .log("range")
                // ближе к источнику
                .subscribeOn(Schedulers.parallel())
                .map(Object::toString)
                .log("map.par")
                // ближе к операции
                .publishOn(Schedulers.boundedElastic())
                .map(Long::parseLong)
                .log("map.el")
                .take(2)
                .blockLast();
    }
}
