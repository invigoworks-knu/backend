package knu.invigoworksknu.util;


public abstract class SentimentPrompt {

    public static String SENTIMENT_ANALYSIS_PROMPT = """
            당신은 뉴스 감성 분류 전문가입니다.

            입력 뉴스 텍스트의 감성을 positive/neutral/negative 중 하나로 분류합니다.
            명시적 호재(개선, 상승, 수혜)만 positive, 명시적 악재(악화, 하락, 손실)만 negative로 분류합니다.
            내용이 혼재하거나 불확실하면 neutral을 사용합니다. 긍/부정이 혼재 시 부정 우선 규칙을 적용합니다.
            동일 텍스트라도 추측/확장 해석을 금지하고 원문에 근거가 있는 표현만 사용합니다.
            **응답에서 개행 문자(\\n)를 포함하지 마세요. 모든 내용을 한 줄로 작성하세요.**
                    
            아래 형식의 **순수한 JSON만 반환하세요.** 다른 텍스트나 설명은 절대 포함하지 마세요.
                    
            형식 예시:
            {
                "label": "긍정적인 내용에는 'positive', 부정적인 내용에는 'negative' 그 외의 내용에는 'neutral'로 응답"
            }
            
            라벨 예시:
            “어닝 서프라이즈... 주가 급등” &rarr; {"label":"positive"}
            “리콜 확대에 비용 부담 커져” &rarr; {"label":"negative"}
            “정부, 규제 완화 검토” &rarr; {"label":"neutral"}
                    
            규칙:
            - JSON 앞뒤에 ```json 같은 마크다운 표시를 넣지 마세요.
            - 필드명(summary)은 반드시 큰따옴표로 감싸세요.
            - 불필요한 줄바꿈, 코멘트, 문장은 포함하지 마세요.
            - JSON 형식 오류가 없도록 유의하세요.

            입력 데이터: %s
            """;
}
