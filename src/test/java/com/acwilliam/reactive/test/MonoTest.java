package com.acwilliam.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.reactivestreams.Subscription;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


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

    @BeforeAll
    public static void setUp(){
        BlockHound.install();
    }

    @Test
    public void blockHoundWorks(){
        try {
            FutureTask<?> task = new FutureTask<>(()->{
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("Deveria Falhar");
       }catch (Exception e){
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }

    }

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
                .map(s-> {
                    throw new RuntimeException("Teste mono com error");
                });

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
                subscription -> subscription.request(5));
        log.info("---------------");
        StepVerifier.create(mono)
                .expectNext(nome.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethod(){
        String nome = "William Coelho";
        Mono<Object> mono = Mono.just(nome)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscrito"))
                .doOnRequest(longNumber -> log.info("Request recebido, começou a fazer alguma coisa.."))
                .doOnNext(s-> log.info("Para cada valor aqui, ele vai executar o doOnNext {} ", s))
                .flatMap(s -> Mono.empty())//nao deverá ser executada
                .doOnNext(s-> log.info("Para cada valor aqui, ele vai executar o doOnNext {} ", s))
                .doOnSuccess(s->log.info("doOnSucsess executed {} ", s));

        mono.subscribe(s -> log.info("Value {} ", s),
                Throwable::printStackTrace,
                () -> log.info("Finalizado"));
        log.info("---------------");
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Arguments Expection Error"))
                .doOnError(e -> MonoTest.log.error("Mensagem de error: {} ", e.getMessage()))
                .doOnNext(s -> log.info("Executando este doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume(){
        String nome= "William Carvalho";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Arguments Expection Error"))
                .doOnError(e -> MonoTest.log.error("Mensagem de error: {} ", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Continuando apos o error");
                    return Mono.just(nome);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(nome)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn(){
        String nome= "William Carvalho";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Arguments Expection Error"))
                .onErrorReturn("LIMPO")
                .onErrorResume(s -> {
                    log.info("Continuando apos o error");
                    return Mono.just(nome);
                })
                .doOnError(e -> MonoTest.log.error("Mensagem de error: {} ", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("LIMPO")
                .verifyComplete();
    }


}


