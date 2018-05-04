package com.yyy.TourWeb.routes;

import com.yyy.TourWeb.domain.Article;
import com.yyy.TourWeb.domain.Comment;
import com.yyy.TourWeb.domain.User;
import com.yyy.TourWeb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyExtractors.toMultipartData;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

/**
 * 用户相关route
 *
 * @author yyy
 */
@Component
public class UserRoute {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;

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
                .flatMap(userService::reg)
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    /**
     * 用户登录处理
     *
     * @param request 请求
     * @return 响应结果
     */
    private Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(User.class)
                .filter(user -> user.getAccount() != null)
                .filter(user -> user.getPassword() != null)
                .flatMap(userService::login)
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    @Bean
    RouterFunction<?> userRoutes() {

        return nest(path("/api/user"),
                route(POST("/reg"), this::reg)
                        .andRoute(POST("/login"), this::login)
                        .andRoute(GET("/info/{account}"), this::info)
                        .andRoute(POST("/icon/upload").and(accept(MediaType.MULTIPART_FORM_DATA)), this::iconUpload)
                        .andRoute(POST("/icon/update/{account}"), this::iconUpdate)
                        .andRoute(POST("/password/{account}"), this::changePassword)
                        .andRoute(POST("/article"), this::addArticle)
                        .andRoute(GET("/article/{id}"), this::getArticle)
                        .andRoute(GET("/articles"), this::getAllArticle)
                        .andRoute(POST("/comment/{id}"), this::addComment)
        );
    }

    private Mono<ServerResponse> getAllArticle(ServerRequest request) {
        return userService.getAllArticle()
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * 新增评论
     *
     * @param request
     * @return
     */
    private Mono<ServerResponse> addComment(ServerRequest request) {
        return request.bodyToMono(Comment.class)
                .filter(comment -> comment.getArticleId() != null)
                .filter(comment -> comment.getContent() != null && !comment.getContent().isEmpty())
                .filter(comment -> comment.getUserAccount() != null && !comment.getUserAccount().isEmpty())
                .flatMap(comment -> userService.addComment(comment))
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    /**
     * 根据文章ID获取文章
     *
     * @param request 请求
     * @return 响应
     */
    private Mono<ServerResponse> getArticle(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        return userService.getArticle(id)
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * 新增文章
     *
     * @param request
     * @return
     */
    private Mono<ServerResponse> addArticle(ServerRequest request) {

        return request.bodyToMono(Article.class)
                .filter(article -> article.getTitle() != null && !article.getTitle().isEmpty())
                .filter(article -> article.getContent() != null && !article.getContent().isEmpty())
                .filter(article -> article.getUserAccount() != null && !article.getUserAccount().isEmpty())
                .flatMap(article -> userService.addArticle(article))
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    /**
     * 更新头像
     *
     * @param request 请求
     * @return 响应
     */
    private Mono<ServerResponse> iconUpdate(ServerRequest request) {
        String account = request.pathVariable("account");
        return request.bodyToMono(User.class)
                .filter(user -> user.getIconPath() != null && !user.getIconPath().isEmpty())
                .flatMap(user -> userService.iconUpdate(account, user.getIconPath()))
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    /**
     * 修改密码
     *
     * @param request 请求
     * @return 响应
     */
    private Mono<ServerResponse> changePassword(ServerRequest request) {
        String account = request.pathVariable("account");

        return request.bodyToMono(User.class)
                .filter(user -> user.getPassword() != null && !user.getPassword().isEmpty())
                .flatMap(user -> userService.changePassword(account, user.getPassword()))
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    /**
     * 头像上传
     *
     * @param request
     * @return
     */
    private Mono<ServerResponse> iconUpload(ServerRequest request) {
        return request.body(toMultipartData())
                .filter(data -> !data.isEmpty())
                .flatMap(userService::iconUpload)
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(badRequest().build());
    }

    /**
     * 获取指定帐号用户信息
     *
     * @param request 请求
     * @return 响应
     */
    private Mono<ServerResponse> info(ServerRequest request) {
        String account = request.pathVariable("account");

        return userService.getInfo(account)
                .flatMap(o -> ok().body(fromObject(o)))
                .switchIfEmpty(status(HttpStatus.INTERNAL_SERVER_ERROR).build());

    }
}
