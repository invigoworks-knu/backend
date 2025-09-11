package knu.invigoworksknu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SentimentResponse {
    private double score;

    @Builder
    public SentimentResponse(double score) {
        this.score = score;
    }

    public static SentimentResponse of(double score) {
        return SentimentResponse.builder()
                .score(score)
                .build();
    }
}
