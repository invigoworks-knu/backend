package knu.invigoworksknu.dto;

import java.util.List;

public record CrawledNewsListDto(YibKrA YIB_KR_A) {

    public record YibKrA(List<Result> result) {
    }

    public record Result(String DATETIME, String CID) {
    }

    public List<CrawledNewsDto> getMultipleCrawledNews() {
        return YIB_KR_A.result()
                .stream()
                .map(r -> CrawledNewsDto.of(r.CID(), r.DATETIME()))
                .toList();
    }
}