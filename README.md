# reactor-exx

## Реактивные стримы
* API
* Specification
* TCK

https://github.com/reactive-streams/reactive-streams-jvm

## Операторы Project Reactor

В документации Project Reactor есть [раздел выбора подходящего оператора](https://projectreactor.io/docs/core/release/reference/#which-operator) или [то же на гитхабе](https://github.com/reactor/reactor-core/blob/main/docs/asciidoc/apdx-operatorChoice.adoc). Но не забывайте про читаемость кода

Примеры работы с операторами:
* [src/test/java/ru/alfa/mobile/reactor/operators/HandleTest.java](src/test/java/ru/alfa/mobile/reactor/operators/HandleTest.java)
* [src/test/java/ru/alfa/mobile/reactor/operators/ZipTest.java](src/test/java/ru/alfa/mobile/reactor/operators/ZipTest.java)
* [src/test/java/ru/alfa/mobile/reactor/operators/GroupByWithFlatMapTest.java](src/test/java/ru/alfa/mobile/reactor/operators/GroupByWithFlatMapTest.java)

## Тестирование реактивных цепочек
Пакет `io.projectreactor:reactor-test` https://projectreactor.io/docs/core/release/reference/#testing или [то же самое на гитхабе](https://github.com/reactor/reactor-core/blob/main/docs/asciidoc/testing.adoc).

## Управление потоками
* Явное [src/test/java/ru/alfa/mobile/reactor/schedulers/PublishOnSubscribeOnTest.java](src/test/java/ru/alfa/mobile/reactor/schedulers/PublishOnSubscribeOnTest.java)
* Неявное [src/test/java/ru/alfa/mobile/reactor/schedulers/ImplicitThreadSwitchTest.java](src/test/java/ru/alfa/mobile/reactor/schedulers/ImplicitThreadSwitchTest.java)

## Погружаясь глубже
Инструментирование кода, замены ThreadLocal
* Global Hooks
* Context
* ...

https://projectreactor.io/docs/core/release/reference/#advanced или [то же самое на гитхаб](https://github.com/reactor/reactor-core/blob/main/docs/asciidoc/advancedFeatures.adoc)