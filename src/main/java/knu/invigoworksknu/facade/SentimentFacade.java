package knu.invigoworksknu.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import knu.invigoworksknu.common.exception.InvigoWorksException;
import knu.invigoworksknu.dto.NewsDto;
import knu.invigoworksknu.service.CrawlingService;
import knu.invigoworksknu.service.NewsService;
import knu.invigoworksknu.service.SentimentService;
import knu.invigoworksknu.util.LockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static knu.invigoworksknu.common.exception.errors.SentimentError.LOCK_ACQUIREMENT_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentimentFacade {

    private static final String CACHE_KEY = "sentiment-score";
    private static final String LOCK_KEY = "lock-sentiment-score";

    public static final int RETRY_COUNT = 100;
    public static final int LOCK_TIMEOUT_MS = 20000;

    private final CrawlingService crawlingService;
    private final SentimentService sentimentService;
    private final NewsService newsService;

    private final LockProvider lockProvider;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public double getSentimentFromCrawledNews(LocalDateTime now) {
        // 1. 캐시 확인
        String cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, Double.class);
            } catch (JsonProcessingException e) {
                log.error("CachedData parsing error : {}", cachedData, e);
            }
        }

        // 2. 분산 락 획득 시도
        for (int retry = 0; retry < RETRY_COUNT; retry++) {
            boolean lockAcquired = lockProvider.tryLock(LOCK_KEY, LOCK_TIMEOUT_MS);
            // 3. 랜덤 지터만큼 대기
            if (!lockAcquired) {
                long jitter = ThreadLocalRandom.current().nextLong(100, 200);
                try {
                    Thread.sleep(jitter);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            try {
                // 4. Double-Checked Locking
                cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
                if (cachedData != null) {
                    try {
                        return objectMapper.readValue(cachedData, Double.class);
                    } catch (JsonProcessingException e) {
                        log.error("CachedData parsing error : {}", cachedData, e);
                    }
                }

                // 5. 데이터 처리
                List<NewsDto> newsList = new ArrayList<>();

                List<String> coinReadersBreakingNewsIds = crawlingService.getCoinReadersBreakingNews(now);
                List<CompletableFuture<Integer>> breakingNewsFutures = coinReadersBreakingNewsIds.stream()
                        .map(id -> CompletableFuture.supplyAsync(() -> {
                            String content = crawlingService.getCoinReadersBreakingNewsById(id);
                            int sentimentScore = sentimentService.getNewsSentiment(content);
                            NewsDto news = NewsDto.of(id, sentimentScore);
                            newsList.add(news);
                            return sentimentScore;
                        }).exceptionally(ex -> {
                            log.error("getNewsSentimentError = ", ex);
                            return 0;
                        }))
                        .toList();
                CompletableFuture.allOf(breakingNewsFutures.toArray(new CompletableFuture[breakingNewsFutures.size()])).join();

                List<String> coinReadersEthereumNewsIds = crawlingService.getCoinReadersEthereumNews(now);
                List<CompletableFuture<Integer>> ethereumNewsFutures = coinReadersEthereumNewsIds.stream()
                        .map(id -> CompletableFuture.supplyAsync(() -> {
                            String content = crawlingService.getCoinReadersEthereumNewsById(id);
                            int sentimentScore = sentimentService.getNewsSentiment(content);
                            NewsDto news = NewsDto.of(id, sentimentScore);
                            newsList.add(news);
                            return sentimentScore;
                        }).exceptionally(ex -> {
                            log.error("getNewsSentimentError = ", ex);
                            return 0;
                        }))
                        .toList();
                CompletableFuture.allOf(ethereumNewsFutures.toArray(new CompletableFuture[ethereumNewsFutures.size()])).join();

                Double coinReadersBreakingNewsSentimentScore = breakingNewsFutures.stream()
                        .mapToInt(CompletableFuture::join)
                        .average()
                        .orElse(0.0);

                Double coinReadersEthereumNewsSentimentScore = ethereumNewsFutures.stream()
                        .mapToInt(CompletableFuture::join)
                        .average()
                        .orElse(0.0);

                newsService.saveNewsListInBatch(newsList, now);

                double calculatedSentimentScore = (coinReadersBreakingNewsSentimentScore + coinReadersEthereumNewsSentimentScore) / 2;
                calculatedSentimentScore = Math.round(calculatedSentimentScore * 100) / 100.0;

                // 6. 캐시 저장
                try {
                    redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(calculatedSentimentScore), 50, TimeUnit.MINUTES);
                } catch (JsonProcessingException e) {
                    log.error("CalculatedSentimentScore serializing error : {}", calculatedSentimentScore, e);
                }

                return calculatedSentimentScore;
            } finally {
                // 7. 락 반환
                lockProvider.releaseLock(LOCK_KEY);
            }
        }
        throw new InvigoWorksException(LOCK_ACQUIREMENT_ERROR);
    }
}
