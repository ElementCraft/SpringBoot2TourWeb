package com.yyy.TourWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

/**
 * 主类
 *
 * @author yyy
 */
@SpringBootApplication
@EnableScheduling
@EnableRedisRepositories
public class TourWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourWebApplication.class, args);
    }

    @Bean
    RouterFunction<?> resourceRouter() {
        return RouterFunctions.resources("/upload/**", new FileSystemResource("upload/"));
    }
}
