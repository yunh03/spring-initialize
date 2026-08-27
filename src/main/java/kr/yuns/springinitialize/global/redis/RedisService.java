package kr.yuns.springinitialize.global.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class RedisService {
    private final ValueOperations<String, Object> values;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.values = redisTemplate.opsForValue();
    }

    public void setValues(String key, String data, Duration duration) {
        values.set(key, data, duration);
    }
}
