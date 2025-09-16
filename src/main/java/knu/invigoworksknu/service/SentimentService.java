package knu.invigoworksknu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.annotation.PostConstruct;
import knu.invigoworksknu.common.exception.InvigoWorksException;
import knu.invigoworksknu.dto.LLMSentimentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static knu.invigoworksknu.common.exception.errors.SentimentError.SENTIMENT_RESULT_PARSING_ERROR;
import static knu.invigoworksknu.util.SentimentPrompt.SENTIMENT_ANALYSIS_PROMPT;

@Component
@RequiredArgsConstructor
public class SentimentService {

    public final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    public String openaiApiKey;

    public ChatModel model;

    @PostConstruct
    public void init() {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("LLMSentimentSchema")
                        .rootElement(JsonObjectSchema.builder()
                                .addStringProperty("label")
                                .build())
                        .build())
                .build();

        model = OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .maxRetries(0)
                .responseFormat(responseFormat)
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
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
            throw new InvigoWorksException(SENTIMENT_RESULT_PARSING_ERROR);
        }
    }

    private UserMessage createPrompt(String promptText, String var) {
        String prompt = promptText.formatted(var);
        UserMessage userMessage = new UserMessage(prompt);
        return userMessage;
    }

    private String getLLMResponse(UserMessage prompt) {
        String response = model.chat(prompt).aiMessage().text();
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
