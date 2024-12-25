package site.nullpointer.mss.ai.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
//这个类需要你自己修改，实现你的认证逻辑，也可以没有认证逻辑。
@Component
@Order(-1) // 确保此过滤器优先执行
public class ApiSecurityFilter implements WebFilter {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Value("${business.token.expiration.seconds:300}")
    private long tokenExpirationSeconds;

    //这里可以改为你自己的oauth2.0认证中心，用token获取用户信息
    private String oauth2CenterUrl = "https://xxxx/oauth2.0/profile";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if ("OPTIONS".equals(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey("Authorization")) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header"));
        }
        //用户的请求中必须带token：Bearer xxxx
        String authorizationHeader = request.getHeaders().getFirst("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header format"));
        }
        String token = authorizationHeader.substring(7); // Remove "Bearer "
        //这里是租户ID，用来支持多租户的。用户的请求中必须带current-enterprise
        String enterpriseId = request.getHeaders().getFirst("current-enterprise");

        return reactiveRedisTemplate.opsForValue()
                .get("AU:" + enterpriseId + ":" + token)
                .switchIfEmpty(fetchUserInfoFromAuthCenter(exchange, enterpriseId, token)).onErrorResume(e -> {
                    // 这里可以自定义错误处理逻辑，比如记录日志、返回特定错误信息给客户端等
                    log.error("Failed to fetch user info from auth center: ", e);
                    // 返回一个错误响应给客户端，这里假设使用HttpStatus.UNAUTHORIZED
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to authenticate user"));
                })
                .flatMap(userInfo -> {
                    exchange.getSession().doOnSuccess(session -> {
                        session.getAttributes().put("user", userInfo);
                        session.getAttributes().put("enterpriseId", enterpriseId);

                    });
                    exchange.getAttributes().put("userInfo", userInfo);
                    exchange.getAttributes().put("enterpriseId", enterpriseId);


                    return chain.filter(exchange);
                });
    }
    //从token认证中心获取用户信息，保存到redis中，便于下次直接认证通过，设置默认token过期时间。
    private Mono<String> fetchUserInfoFromAuthCenter(ServerWebExchange exchange, String enterpriseId, String token) {
        return WebClient.builder()
                .baseUrl(oauth2CenterUrl)
                .build()
                .get()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Token expired or invalid")))
                .bodyToMono(String.class)
                // 假设返回JSON字符串形式的用户信息
                .flatMap(userInfo -> {
                    // 直接在流中处理Redis存储逻辑
                    return reactiveRedisTemplate.opsForValue()
                            .set("AU:" + enterpriseId + ":" + token, userInfo, Duration.ofSeconds(tokenExpirationSeconds))
                            .thenReturn(userInfo) // 返回userInfo，以便后续操作可以继续使用
                            .onErrorResume(e -> {
                                log.error("Failed to store user info in Redis: ", e);
                                // 根据需要决定是抛出新异常终止链路，还是返回一个默认值等
                                return Mono.error(new RuntimeException("Failed to store in Redis"));
                            });
                });

    }
}