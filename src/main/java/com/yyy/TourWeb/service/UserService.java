package com.yyy.TourWeb.service;

import com.yyy.TourWeb.domain.User;
import com.yyy.TourWeb.tools.JsonUtils;
import com.yyy.TourWeb.tools.RedisKey;
import com.yyy.TourWeb.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static com.yyy.TourWeb.tools.RedisKey.USER;

/**
 * @author yyy
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * 用户注册处理
     *
     * @param user 用户实体
     * @return 处理结果
     */
    public Mono<Result> reg(User user) {
        String redisKey = RedisKey.of(USER, user.getAccount());
        String jsonUser = JsonUtils.toString(user);

        return redisTemplate.hasKey(redisKey)
                .flatMap(bo -> {
                    if (bo) {
                        return Mono.just(Result.error(1, "账号已存在"));
                    } else {
                        return redisTemplate.opsForValue().set(redisKey, jsonUser)
                                .map(flag -> {
                                    if (flag) {
                                        return Result.ok();
                                    } else {
                                        return Result.error(2, "数据库Save失败");
                                    }
                                })
                                .switchIfEmpty(Mono.just(Result.error(2, "数据库Save失败")));
                    }
                });
    }
}