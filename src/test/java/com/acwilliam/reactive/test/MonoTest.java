package com.acwilliam.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/*
 * Reactive Streams
 * 1. assicrono
 * 2. não blocante
 * 3. backpressure
 * publisher <- (subscribe) subscriber - vai emitir os eventos ele é cold
 * subscription é criado quando voce da o subscriber.
 * o publisher (vai chamar o onSubscribe com o  subscription) -> Subscriber
 * Subscription <-(request N) Subscriber
 * Publisher -> OnNext) Subscriber
 * executar até:
 * 1. Publisher envia todos os objetos requisitado
 * 2. Publisher envia todos os objetos. (onComplete) subscriber and e subscription são cancelados
 * 3. Quando há um ero. (onError)-> subscriber e subscription são cancelados
 */
public class MonoTest {

    @Test
    public void monoSubscriber(){
        String nome = "William Coelho";
        Mono<String> mono = Mono.just(nome)
                .log();
        mono.subscribe();
        log.info("---------------");
        StepVerifier.create(mono)
                .expectNext(nome)
                .verifyComplete();

    }
}
