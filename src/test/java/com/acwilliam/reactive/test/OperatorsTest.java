package com.acwilliam.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSimple(){
      Flux<Integer> flux =  Flux.range(1,4)
                .map(i-> {
                    log.info("MAP 1 - Number {} na Thread {}", i, Thread.currentThread().getName());
                    return 1;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return 1;
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
                    return 1;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("MAP 2 - Number {} na Thread {} ",i, Thread.currentThread().getName());
                    return 1;
                });
        flux.subscribe();
        flux.subscribe();
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }
}
