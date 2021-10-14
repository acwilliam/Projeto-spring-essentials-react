package com.acwilliam.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSimple(){
      Flux<Integer> flux =  Flux.range(1,4)
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return i;
                });
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void publisherOnSimple(){
        Flux<Integer> flux =  Flux.range(1,4)
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return i;
                });
        flux.subscribe();
        flux.subscribe();
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void multipleSubscriberOnSimple(){
        Flux<Integer> flux =  Flux.range(1,4)
                .subscribeOn(Schedulers.single())
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return i;
                });
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void multiplePublishOn(){
        Flux<Integer> flux =  Flux.range(1,4)
                .publishOn(Schedulers.single())
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void publisheAndSubscriberOnSimple(){
        Flux<Integer> flux =  Flux.range(1,4)
                .publishOn(Schedulers.single())//publish on é a precedencia sobre o subs
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())//totalmente ignorado
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return i;
                });
        flux.subscribe();
        flux.subscribe();
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void subscriberAndPublisheOnSimple(){
        Flux<Integer> flux =  Flux.range(1,4)
                .subscribeOn(Schedulers.single())//afeta tudo abaixo
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())//publish afeta depois do momento da declaracao
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return i;
                });
        flux.subscribe();
        flux.subscribe();
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    public void subscribeOn() throws Exception{
        Mono<List<String>> list = Mono.fromCallable(() -> Files.readAllLines(Path.of("text-file")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

      //  list.subscribe(s-> log.info("{}",s));

        Thread.sleep(2000);

        StepVerifier.create(list)
                .expectSubscription()
                .thenConsumeWhile(l ->{
                    Assertions.assertFalse(l.isEmpty());
                    log.info(" tamanho {}", l.size());
                    return true;
                })
                .verifyComplete();

    }

    @Test
    public void switchIfEmptyOperator(){
        Flux<Object> flux = emptyFlux()
                .switchIfEmpty(Flux.just("Não tem nada limpo"))
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("Não tem nada limpo")
                .expectComplete()
                .verify();

    }

    @Test
    public void deferOperator() throws Exception{
        Mono<Long> just = Mono.just(System.currentTimeMillis());
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
            defer.subscribe(l -> log.info("Tempo {}", l));
            Thread.sleep(100);
            defer.subscribe(l -> log.info("Tempo {}", l));
            Thread.sleep(100);
            defer.subscribe(l -> log.info("Tempo {}", l));
            Thread.sleep(100);
            defer.subscribe(l -> log.info("Tempo {}", l));
            Thread.sleep(100);
            defer.subscribe(l -> log.info("Tempo {}", l));

        AtomicLong atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get() >0);
        
    }

    private Flux<Object> emptyFlux(){
        return Flux.empty();
    }
}
