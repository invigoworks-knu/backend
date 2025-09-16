package knu.invigoworksknu.util;


public abstract class SentimentPrompt {

    public static String SENTIMENT_ANALYSIS_PROMPT = """
            뉴스 감성 분석기다.
            - 사실 전달형/중립적 보도 톤은 기본값으로 neutral 처리한다.
            - 명백한 악재(적자 확대, 리콜, 규제 불이익, 급락 등)는 negative.
            - 명백한 호재(사상 최대 실적, 대규모 투자 유치/수주, 급등 등)는 positive.
            - 혼재할 경우 기사 전체 톤 기준으로 단 하나의 라벨만 선택한다.
            - 반드시 JSON 한 줄만 출력한다.
            출력 스키마: {{"label":"negative|neutral|positive"}}
            
            본문: {%s}
            """;
}
