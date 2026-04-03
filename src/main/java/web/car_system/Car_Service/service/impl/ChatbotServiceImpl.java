package web.car_system.Car_Service.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.chatbot.ChatRequestDTO;
import web.car_system.Car_Service.domain.dto.chatbot.ChatResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.ChatbotService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private final String DEFAULT_SYSTEM_INSTRUCTION = "Bạn là một trợ lý AI chuyên về dịch vụ xe hơi. Hãy trả lời ngắn gọn, lịch sự, đưa ra ý chính và chỉ cung cấp thông tin liên quan đến xe hơi, dịch vụ sửa chữa, hoặc bảo trì. Nếu không biết câu trả lời, hãy nói 'Tôi không có thông tin về vấn đề này, vui lòng liên hệ trung tâm dịch vụ.'";
    private final String MODEL_NAME = "gemini-3.1-flash-lite-preview";
    private final String ERROR_MESSAGE = "Rất xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.";
    private final String AI_NOT_AVAILABLE = "Trợ lý AI hiện không khả dụng. Vui lòng thử lại sau.";
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    private final Client geminiClient;

    private final Map<String, Chat> activeChatSessions = new ConcurrentHashMap<>();

    // Inject singleton Gemini Client từ GeminiConfig
    public ChatbotServiceImpl(Client geminiClient) {
        this.geminiClient = geminiClient;
        MutableDataSet options = new MutableDataSet();
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO> getAiReply(ChatRequestDTO chatRequestDTO) {
        if (this.geminiClient == null) {
            return createErrorResponse(AI_NOT_AVAILABLE);
        }
        try {
            Content systemInstruction = Content.fromParts(Part.fromText(DEFAULT_SYSTEM_INSTRUCTION));
            GenerateContentConfig config = createGenerateContentConfig(systemInstruction);
            Chat chatSession = activeChatSessions.computeIfAbsent(chatRequestDTO.sessionId(), key ->
                    this.geminiClient.chats.create(MODEL_NAME, config)
            );

            GenerateContentResponse response = chatSession.sendMessage(chatRequestDTO.message(), config);

            String aiReplyText = response.text();
            String formattedReply = convertMarkdownToHtml(aiReplyText);
            ChatResponseDTO responseData = new ChatResponseDTO(formattedReply);

            return createSuccessResponse(responseData);

        } catch (Exception e) {
            return createErrorResponse(ERROR_MESSAGE);
        }
    }


    private GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO> createSuccessResponse(ChatResponseDTO data) {
        return GlobalResponseDTO.<NoPaginatedMeta, ChatResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Thành công").build())
                .data(data)
                .build();
    }

    private GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO> createErrorResponse(String message) {
        return GlobalResponseDTO.<NoPaginatedMeta, ChatResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(message).build())
                .build();
    }

    private String convertMarkdownToHtml(String markdownText) {
        Node document = markdownParser.parse(markdownText);
        return htmlRenderer.render(document);
    }

    private GenerateContentConfig createGenerateContentConfig(Content systemInstruction) {
        return GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .candidateCount(1)
                .maxOutputTokens(1024)
                .safetySettings(ImmutableList.of(
                        SafetySetting.builder()
                                .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                                .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                                .build(),
                        SafetySetting.builder()
                                .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                                .threshold(HarmBlockThreshold.Known.BLOCK_LOW_AND_ABOVE)
                                .build()
                ))
                .build();
    }
}
