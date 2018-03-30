package com.yyy.TourWeb.routes;

import com.yyy.TourWeb.domain.User;
import com.yyy.TourWeb.tools.JsonUtils;
import com.yyy.TourWeb.tools.RedisKey;
import com.yyy.TourWeb.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.yyy.TourWeb.tools.RedisKey.USER;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * 用户相关route
 */
@Component
public class UserRoute {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * 用户注册处理
     *
     * @param request 请求
     * @return 响应结果
     */
    private Mono<ServerResponse> reg(ServerRequest request) {

        return request.bodyToMono(User.class)
                .filter(user -> user.getAccount() != null)
                .filter(user -> user.getPassword() != null)
                .flatMap(user -> {
                    String redisKey = RedisKey.of(USER, user.getAccount());

                    return redisTemplate.hasKey(redisKey)
                            .flatMap(bo -> {
                                if (bo) {
                                    return ok().body(fromObject(Result.error(1, "账号已存在")));
                                } else {
                                    return redisTemplate.opsForValue().set(redisKey, JsonUtils.toString(user))
                                            .map(flag -> {
                                                if(flag){
                                                    return Result.ok();
                                                }else{
                                                    return Result.error(2, "数据库Save失败");
                                                }
                                            })
                                            .switchIfEmpty(Mono.just(Result.error(2, "数据库Save失败")))
                                            .flatMap(result -> ok().body(fromObject(Result.ok())))
                                            .switchIfEmpty(badRequest().build());
                                }
                            }).switchIfEmpty(ok().body(fromObject(Result.error(2, "Redis数据库异常"))));
                }).switchIfEmpty(badRequest().build());
    }

    @Bean
    RouterFunction<?> userRoutes() {

        return nest(path("/api/user"),
                route(POST("/reg"), this::reg)
        );
    }
}
