package knu.invigoworksknu.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CrawlingError implements ErrorInfo {

    CRAWLING_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "뉴스 크롤링 중 에러가 발생했습니다."),
    INVALID_DATETIME_FORMAT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 날짜 포맷입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return this.status;
    }

    @Override
    public String message() {
        return this.message;
    }
}
