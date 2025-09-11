package knu.invigoworksknu.dto;

import lombok.Getter;

public record CrawledNewsDto(
        String id,
        String datetime
) {
    public static CrawledNewsDto of(String id, String datetime) {
        return new CrawledNewsDto(id, datetime);
    }
}