package knu.invigoworksknu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NewsDto {
    private String id;
    private int score;

    @Builder
    public NewsDto(String id, int score) {
        this.id = id;
        this.score = score;
    }

    public static NewsDto of(String id, int score) {
        return NewsDto.builder()
                .id(id)
                .score(score)
                .build();
    }
}
