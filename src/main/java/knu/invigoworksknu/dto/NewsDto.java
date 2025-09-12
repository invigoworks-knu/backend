package knu.invigoworksknu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NewsDto newsDto = (NewsDto) object;
        return Objects.equals(id, newsDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
