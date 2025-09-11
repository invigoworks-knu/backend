package knu.invigoworksknu.controller;

import knu.invigoworksknu.common.domain.ApiResponse;
import knu.invigoworksknu.dto.SentimentResponse;
import knu.invigoworksknu.facade.SentimentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sentiment")
public class SentimentController {

    private final SentimentFacade sentimentFacade;

    @GetMapping
    public ApiResponse<SentimentResponse> getSentimentScore() {
        LocalDateTime now = LocalDateTime.now();
        double score = sentimentFacade.getSentimentFromCrawledNews(now);
        return ApiResponse.ok(SentimentResponse.of(score));
    }
}
