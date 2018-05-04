package com.yyy.TourWeb.service;

import com.yyy.TourWeb.domain.Article;
import com.yyy.TourWeb.domain.Comment;
import com.yyy.TourWeb.domain.User;
import com.yyy.TourWeb.tools.CommonUtil;
import com.yyy.TourWeb.tools.JsonUtils;
import com.yyy.TourWeb.tools.RedisKey;
import com.yyy.TourWeb.tools.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.yyy.TourWeb.tools.RedisKey.*;

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
        String redisKey = RedisKey.of(USER);
        String jsonUser = JsonUtils.toString(user);

        return redisTemplate.opsForHash().hasKey(redisKey, user.getAccount())
                .flatMap(bo -> {
                    if (bo) {
                        return Mono.just(Result.error(1, "账号已存在"));
                    } else {
                        return redisTemplate.opsForHash().put(redisKey, user.getAccount(), jsonUser)
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

    /**
     * 用户登录判断
     *
     * @param user 登录信息
     * @return 是否成功
     */
    public Mono<Result<Object>> login(User user) {
        String redisKey = RedisKey.of(USER);

        return redisTemplate.opsForHash().hasKey(redisKey, user.getAccount())
                .filter(bo -> bo)
                .flatMap(bo -> redisTemplate.opsForHash().get(redisKey, user.getAccount())
                        .filter(json -> (json != null) && !json.toString().isEmpty())
                        .map(json -> {
                            User dbUser = JsonUtils.toObject(json.toString(), User.class);

                            if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
                                return Result.ok();
                            } else {
                                return Result.error(2, "密码不正确");
                            }
                        })
                        .switchIfEmpty(Mono.just(Result.error(1, "账号不存在"))))
                .switchIfEmpty(Mono.just(Result.error(1, "账号不存在")));
    }

    /**
     * 获取指定帐号的用户信息
     *
     * @param account 帐号
     * @return 用户信息
     */
    public Mono<Result<User>> getInfo(String account) {
        String redisKey = RedisKey.of(USER);

        return redisTemplate.opsForHash().get(redisKey, account)
                .filter(json -> (json != null) && !json.toString().isEmpty())
                .map(json -> {
                    User dbUser = JsonUtils.toObject(json.toString(), User.class);

                    return Result.ok(dbUser);
                })
                .switchIfEmpty(Mono.just(Result.error(1, "账号不存在")));
    }

    /**
     * 上传头像
     *
     * @param multiValueMap 表单信息
     * @return 上传文件url
     */
    public Mono<Result<String>> iconUpload(MultiValueMap<String, Part> multiValueMap) {
        Map<String, Part> parts = multiValueMap.toSingleValueMap();
        if (parts.containsKey("file")) {
            FilePart part = (FilePart) parts.get("file");
            String ext = StringUtils.getFilenameExtension(part.filename());

            if (ext != null) {
                ext = ext.toLowerCase();
            }

            if (!"jpg".equals(ext) && !"gif".equals(ext) && !"png".equals(ext) && !"bmp".equals(ext)) {
                return Mono.just(Result.error(3, "不允许上传该格式的文件"));
            }

            String fileName = ZonedDateTime.now().toEpochSecond()
                    + "_" + CommonUtil.randomString(6) + "." + ext;

            String filePath = "upload" + File.separator + fileName;

            // 目录
            File dir = new File("upload");
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return part.transferTo(file).thenReturn(Result.ok(filePath));
        }

        return Mono.just(Result.error(1, "上传文件异常"));
    }

    /**
     * 图片更新
     *
     * @param account  帐号
     * @param iconPath 图标路径
     * @return
     */
    public Mono<Result<Object>> iconUpdate(String account, String iconPath) {
        String redisKey = RedisKey.of(USER);

        return redisTemplate.opsForHash().get(redisKey, account)
                .filter(json -> (json != null) && !json.toString().isEmpty())
                .flatMap(json -> {
                    User dbUser = JsonUtils.toObject(json.toString(), User.class);
                    dbUser.setIconPath(iconPath);

                    return redisTemplate.opsForHash().put(redisKey, account, JsonUtils.toString(dbUser))
                            .map(o -> Result.ok())
                            .switchIfEmpty(Mono.just(Result.error(2, "数据库连接异常")));
                })
                .switchIfEmpty(Mono.just(Result.error(1, "账号不存在")));
    }

    /**
     * 修改密码
     *
     * @param account  帐号
     * @param password 密码
     * @return
     */
    public Mono<Result<Object>> changePassword(String account, String password) {
        String redisKey = RedisKey.of(USER);

        return redisTemplate.opsForHash().get(redisKey, account)
                .filter(json -> (json != null) && !json.toString().isEmpty())
                .flatMap(json -> {
                    User dbUser = JsonUtils.toObject(json.toString(), User.class);
                    dbUser.setPassword(password);

                    return redisTemplate.opsForHash().put(redisKey, account, JsonUtils.toString(dbUser))
                            .map(o -> Result.ok())
                            .switchIfEmpty(Mono.just(Result.error(2, "数据库连接异常")));
                })
                .switchIfEmpty(Mono.just(Result.error(1, "账号不存在")));
    }

    public Mono<Result<Object>> addArticle(Article article) {
        String title = article.getTitle();
        String account = article.getUserAccount();
        String redisKey = RedisKey.of(ARTICLE);
        Long id = Instant.now().getEpochSecond();
        String userArticleKey = RedisKey.of(USER_ARTICLE, account);

        title = title.trim();
        article.setTitle(title);
        article.setId(id);
        article.setClickCount(1);
        article.setCommentCount(0);
        article.setGmtCreate(ZonedDateTime.now());

        return redisTemplate.opsForHash()
                .put(redisKey, id.toString(), JsonUtils.toString(article))
                .flatMap(o -> redisTemplate.opsForZSet().add(userArticleKey, id.toString(), id)
                        .flatMap(result -> Mono.just(Result.ok()))
                        .switchIfEmpty(Mono.just(Result.error(2, "数据库连接异常")))
                )
                .switchIfEmpty(Mono.just(Result.error(2, "数据库连接异常")));
    }

    public Mono<Result<Article>> getArticle(Long id) {
        String redisKey = RedisKey.of(ARTICLE);

        return redisTemplate.opsForHash().get(redisKey, id.toString())
                .flatMap(o -> Mono.just(Result.ok(JsonUtils.toObject(o.toString(), Article.class))))
                .switchIfEmpty(Mono.just(Result.error(1, "文章不存在")));
    }

    public Mono<Result<Comment>> addComment(Comment comment) {
        Long articleId = comment.getArticleId();
        String redisKey = RedisKey.of(COMMENT, articleId.toString());

        return null;
    }

    public Mono<List<Article>> getAllArticle() {
        String redisKey = RedisKey.of(ARTICLE);

        return redisTemplate.opsForHash().values(redisKey)
                .map(o -> JsonUtils.toObject(o.toString(), Article.class))
                .collectList();
    }
}