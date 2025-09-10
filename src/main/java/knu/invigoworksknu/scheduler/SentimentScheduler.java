package knu.invigoworksknu.scheduler;

import knu.invigoworksknu.facade.SentimentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class SentimentScheduler {

    private final SentimentFacade sentimentFacade;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void saveSentimentScore() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        LocalDateTime now = LocalDateTime.now();
        double score = sentimentFacade.getSentimentFromCrawledNews(now);
        log.info("뉴스 감성 분석 완료 : {}", score);
    }
}
