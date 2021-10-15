package com.acwilliam.reactive.test;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Integer.parseInt;

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

    private Flux<Object> emptyFlux(){
        return Flux.empty();
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

    @Test
    public void concatOperator(){
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concat(flux1, flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","b","c", "d")
                .expectComplete()
                .verify();

    }

    @Test
    public void concatOperatorError(){
        Flux<String> flux1 = Flux.just("a", "b")
                .map(s->{
                    if (s.equals("b")){
                        throw new IllegalArgumentException();
                    }
                    return s;
                });
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concatDelayError(flux1, flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a", "c", "d")
                .expectError()
                .verify();

    }

    @Test
    public void concatWithOperator(){
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = flux1.concatWith(flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","b","c", "d")
                .expectComplete()
                .verify();

    }

    @Test
    public void combineLastestOperator(){
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> combineFlux = Flux.combineLatest(flux1, flux2, (s1, s2) -> s1.toUpperCase() + s2.toUpperCase())
                .log();

        StepVerifier.create(combineFlux)
                .expectSubscription()
                .expectNext("BC","BD")
                .expectComplete()
                .verify();

    }

    @Test
    public void mergeOperator() throws Exception {
        Flux<String> flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = Flux.merge(flux1, flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

      //  mergeFlux.subscribe(log::info);
       // Thread.sleep(1000);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();

    }

    @Test
    public void mergeWithOperator() throws Exception {
        Flux<String> flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = flux1.mergeWith(flux2)
                 .delayElements(Duration.ofMillis(200))
                .log();

        //  mergeFlux.subscribe(log::info);
        // Thread.sleep(1000);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .expectComplete()
                .verify();

    }

    @Test
    public void mergeSequentialOperator() throws Exception {
        Flux<String> flux1 = Flux.just("a","b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = Flux.mergeSequential(flux1, flux2, flux1)
                .delayElements(Duration.ofMillis(200))
                .log();

        //  mergeFlux.subscribe(log::info);
        // Thread.sleep(1000);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","b","c","d","a","b")
                .expectComplete()
                .verify();

    }

    @Test
    public void mergeDelayErrorOperator() throws Exception {
        Flux<String> flux1 = Flux.just("a","b")
                .map(s->{
                    if (s.equals("b")){
                        throw new IllegalArgumentException();
                    }
                    return s;
                }).doOnError(t-> log.error("Voce deveria fazer alguma coisa com isto"));

        Flux<String> flux2 = Flux.just("c","d");

        Flux<String> mergeFlux = Flux.mergeDelayError( 1,flux1, flux2, flux1)
                .delayElements(Duration.ofMillis(200))
                .log();

         mergeFlux.subscribe(log::info);

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNext("a","b","c","d","a","b")
                .expectComplete()
                .verify();

    }

    @Test
    public void flatMapOperator() throws Exception {
        Flux<String> flux = Flux.just("a", "b");

        Flux<String> flatFlux = flux.map(String::toUpperCase)
                .flatMap(this::findByName)
                .log();

     //   flatFlux.subscribe(log::info);
     //  Thread.sleep(500);

        StepVerifier.create(flatFlux)
                .expectSubscription()
                .expectNext("nomeB1","nomeB2","nomeA1","nomeA2")
                .verifyComplete();
    }

    @Test
    public void flatMapSequentialOperator() throws Exception {
        Flux<String> flux = Flux.just("a", "b");

        Flux<String> flatFlux = flux.map(String::toUpperCase)
                .flatMapSequential(this::findByName)
                .log();

        //   flatFlux.subscribe(log::info);
        //  Thread.sleep(500);

        StepVerifier.create(flatFlux)
                .expectSubscription()
                .expectNext("nomeA1","nomeA2","nomeB1","nomeB2")
                .verifyComplete();
    }

    public Flux<String> findByName(String name){
        return name.equals("A") ? Flux.just("nomeA1","nomeA2").delayElements(Duration.ofMillis(100)):Flux.just("nomeB1","nomeB2");
    }

    @Test
    public void zipOperator(){
        Flux<String> tituloFlux = Flux.just("naruto", "boruto");
        Flux<String> estudioFlux = Flux.just("Studio Pierrot", "Studio Pierrot boruto");
        Flux<Integer> episodiosFlux = Flux.just(12,24);

        Flux<Anime> animeFlux = Flux.zip(tituloFlux, episodiosFlux, estudioFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(),tuple.getT3(), tuple.getT2())));

        animeFlux.subscribe(anime->log.info(anime.toString()));

        StepVerifier.create(animeFlux)
                .expectSubscription()
                .expectNext(new Anime("naruto","Studio Pierrot",12),
                            new Anime("boruto","Studio Pierrot boruto",24))
                .verifyComplete();

    }

    @Test
    public void zipWithOperator(){
        Flux<String> tituloFlux = Flux.just("naruto", "boruto");
        Flux<String> estudioFlux = Flux.just("Studio Pierrot", "Studio Pierrot boruto");
        Flux<Integer> episodiosFlux = Flux.just(12,24);

        Flux<Anime> animeFlux = Flux.zip(tituloFlux, episodiosFlux, estudioFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(),tuple.getT3(), tuple.getT2())));

        animeFlux.subscribe(anime->log.info(anime.toString()));

        StepVerifier.create(animeFlux)
                .expectSubscription()
                .expectNext(new Anime("naruto","Studio Pierrot",12),
                        new Anime("boruto","Studio Pierrot boruto",24))
                .verifyComplete();

    }

    @Getter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    class Anime{
        private String titulo;
        private String estudio;
        private int episodios;
    }


}
