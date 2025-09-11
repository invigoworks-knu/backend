package knu.invigoworksknu.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DistributedLockProvider implements LockProvider {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean tryLock(String key, long timeoutMs) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "locked", timeoutMs, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
