package knu.invigoworksknu.common.exception.errors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SentimentError implements ErrorInfo {

    SENTIMENT_RESULT_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "감성 분석 결과 파싱 중 에러가 발생했습니다."),
    LOCK_ACQUIREMENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "분산락 획득 재시도 횟수를 초과하였습니다."),
    MAX_USAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LLM 허용 사용량을 초과하였습니다.");

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
