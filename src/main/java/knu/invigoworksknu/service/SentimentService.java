package knu.invigoworksknu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.annotation.PostConstruct;
import knu.invigoworksknu.common.exception.InvigoWorksException;
import knu.invigoworksknu.dto.LLMSentimentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static knu.invigoworksknu.common.exception.errors.SentimentError.MAX_USAGE_ERROR;
import static knu.invigoworksknu.common.exception.errors.SentimentError.SENTIMENT_RESULT_PARSING_ERROR;
import static knu.invigoworksknu.util.SentimentPrompt.SENTIMENT_ANALYSIS_PROMPT;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentimentService {

    public final ObjectMapper objectMapper;

    public String GEMINI_MODEL_NAME = "gemini-2.0-flash";

    @Value("${google.gemini.api-key}")
    public String mainApiKey;
    @Value("${google.gemini.sub.api-key}")
    public String subApiKey;

    public GoogleAiGeminiChatModel mainModel;
    public GoogleAiGeminiChatModel subModel;

    @PostConstruct
    public void init() {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .rootElement(JsonObjectSchema.builder()
                                .addStringProperty("label")
                                .build())
                        .build())
                .build();

        mainModel = GoogleAiGeminiChatModel.builder()
                .apiKey(mainApiKey)
                .modelName(GEMINI_MODEL_NAME)
                .maxRetries(0)
                .responseFormat(responseFormat)
                .build();

        subModel = GoogleAiGeminiChatModel.builder()
                .apiKey(subApiKey)
                .modelName(GEMINI_MODEL_NAME)
                .maxRetries(0)
                .responseFormat(responseFormat)
                .build();
    }

    public int getNewsSentiment(String content) {
        String promptText = SENTIMENT_ANALYSIS_PROMPT;

        UserMessage prompt = createPrompt(promptText, content);
        String response = getLLMResponse(prompt);

        try {
            LLMSentimentDto result = objectMapper.readValue(response, LLMSentimentDto.class);
            return parseLLMSentiment(result);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("getNewsSentimentError / response = {}", response, e);
            throw new InvigoWorksException(SENTIMENT_RESULT_PARSING_ERROR);
        }
    }

    private UserMessage createPrompt(String promptText, String var) {
        String prompt = promptText.formatted(var);
        UserMessage userMessage = new UserMessage(prompt);
        return userMessage;
    }

    private String getLLMResponse(UserMessage prompt) {
        String response;
        try {
            response = mainModel.chat(prompt).aiMessage().text();
        } catch (Exception e) {
            try {
                response = subModel.chat(prompt).aiMessage().text();
            } catch (Exception finalException) {
                log.error("maxUsageError", e);
                throw new InvigoWorksException(MAX_USAGE_ERROR);
            }
        }
        return response;
    }

    private int parseLLMSentiment(LLMSentimentDto result) {
        switch (result.getLabel()) {
            case "positive":
                return 1;
            case "neutral":
                return 0;
            case "negative":
                return -1;
            default:
                throw new IllegalArgumentException();
        }
    }

}
