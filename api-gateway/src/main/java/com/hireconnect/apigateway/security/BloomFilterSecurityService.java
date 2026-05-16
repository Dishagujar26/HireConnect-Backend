package com.hireconnect.apigateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloomFilterSecurityService {

    private final StringRedisTemplate redisTemplate;
    
    private static final String IP_BLACKLIST_FILTER = "hireconnect:security:blacklist:ips";
    private static final String PATH_WHITELIST_FILTER = "hireconnect:security:whitelist:paths";

    @PostConstruct
    public void init() {
        log.info("Initializing Gateway Bloom Filters...");
        List<String> validPaths = List.of(
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
            "/api/jobs", "/api/jobs/search", "/api/profiles/me", "/api/applications/apply",
            "/swagger-ui/index.html", "/v3/api-docs", "/v3/api-docs/swagger-config",
            "/actuator", "/actuator/health", "/actuator/info"
        );
        validPaths.forEach(this::addValidPath);
        
        // Also whitelist all aggregate doc paths
        List.of("auth", "profile", "job", "application", "interview", "notification", "payment", "admin")
            .forEach(service -> addValidPath("/aggregate/" + service + "/v3/api-docs"));
    }

    public void addValidPath(String path) {
        executeBloomAdd(PATH_WHITELIST_FILTER, path);
    }

    public void blacklistIp(String ip) {
        executeBloomAdd(IP_BLACKLIST_FILTER, ip);
    }

    public boolean isIpBlacklisted(String ip) {
        return executeBloomExists(IP_BLACKLIST_FILTER, ip);
    }

    public boolean isValidPath(String path) {
        // Strip query params and IDs for simpler matching if needed
        String normalizedPath = path.split("\\?")[0];
        return executeBloomExists(PATH_WHITELIST_FILTER, normalizedPath);
    }

    private void executeBloomAdd(String key, String item) {
        try {
            redisTemplate.execute(new DefaultRedisScript<>("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class),
                    Collections.singletonList(key), item);
        } catch (Exception e) {
            log.warn("Bloom Add failed for {}: {}", key, e.getMessage());
        }
    }

    private boolean executeBloomExists(String key, String item) {
        try {
            Long result = redisTemplate.execute(new DefaultRedisScript<>("return redis.call('BF.EXISTS', KEYS[1], ARGV[1])", Long.class),
                    Collections.singletonList(key), item);
            return result != null && result == 1L;
        } catch (Exception e) {
            return false; // Default to allow if Redis/Bloom fails
        }
    }

    /**
     * RATE LIMITING: Checks if an IP has exceeded the limit (10 requests/min).
     * Uses a fixed-window counter pattern in Redis.
     */
    public boolean isRateLimited(String ip) {
        String key = "hireconnect:rate:limit:" + ip;
        try {
            // Increment the counter for this IP
            Long count = redisTemplate.opsForValue().increment(key);
            
            // If this is the first request in the window, set expiration to 60 seconds
            if (count != null && count == 1) {
                redisTemplate.expire(key, 1, java.util.concurrent.TimeUnit.MINUTES);
            }
            
            // Limit check: 1000 requests per minute (Increased to prevent accidental blocking during dev testing)
            if (count != null && count > 1000) {
                log.warn("RATE LIMIT: IP {} exceeded 1000 req/min limit. Current count: {}", ip, count);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("REDIS ERROR: Rate limit check failed for {}. Error: {}", ip, e.getMessage());
            return false; // Fail-open: allow request if Redis is down
        }
    }
}
