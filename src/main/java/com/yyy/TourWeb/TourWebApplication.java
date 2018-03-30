package com.yyy.TourWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * 主类
 *
 * @author yyy
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableRedisRepositories
public class TourWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourWebApplication.class, args);
    }

    @Bean
    RouterFunction<?> defaultRoutes(ReactiveRedisTemplate<String, String> redisTemplate) {
        return nest(path("/api"),

                route(GET("/get/{key}"), request -> {
                    Mono<String> v = redisTemplate.opsForValue().get(request.pathVariable("key"));
                    return ok().body(v, String.class);
                }).andRoute(GET("/set/{key}/{value}"), request -> {
                    String k = request.pathVariable("key");
                    String v = request.pathVariable("value");

                    return Optional.ofNullable(v).map(o -> {
                        Mono<Boolean> bo = redisTemplate.opsForValue().set(k, o);
                        return ok().body(bo, Boolean.class);
                    }).orElse(badRequest().build());
                })
        );
    }
}
