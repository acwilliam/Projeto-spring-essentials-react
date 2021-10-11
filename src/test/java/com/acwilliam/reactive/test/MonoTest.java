package com.acwilliam.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
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

    @Test
    public void monoSubscriberConsumer(){
        String nome = "William Coelho";
        Mono<String> mono = Mono.just(nome)
                .log();

        mono.subscribe(s -> log.info("Value{} ", s));
        log.info("---------------");
        StepVerifier.create(mono)
                .expectNext(nome)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        String nome = "William Coelho";
        Mono<String> mono = Mono.just(nome)
                .map(s-> {throw new RuntimeException("Teste mono com error");});

        mono.subscribe(s -> log.info("Nome{} ", s), s-> log.error("Alguma coisas ruim aconteceu"));
        mono.subscribe(s -> log.info("Nome{} ", s), Throwable::printStackTrace);
        log.info("---------------");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete(){
        String nome = "William Coelho";
        Mono<String> mono = Mono.just(nome)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {} ", s),
                Throwable::printStackTrace,
                () -> log.info("Finalizado"));
        log.info("---------------");
        StepVerifier.create(mono)
                .expectNext(nome.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String nome = "William Coelho";
        Mono<String> mono = Mono.just(nome)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {} ", s),
                Throwable::printStackTrace,
                () -> log.info("Finalizado"),
                Subscription::cancel);
        log.info("---------------");
        StepVerifier.create(mono)
                .expectNext(nome.toUpperCase())
                .verifyComplete();
    }
}


