package web.car_system.Car_Service.service.impl;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.vladsch.flexmark.util.ast.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import web.car_system.Car_Service.domain.dto.chatbot.ChatRequestDTO;
import web.car_system.Car_Service.domain.dto.chatbot.ChatResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.ChatbotService;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.vladsch.flexmark.html.HtmlRenderer; // Import
import com.vladsch.flexmark.parser.Parser;     // Import
import com.vladsch.flexmark.util.data.MutableDataSet; // Import

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private final String DEFAULT_SYSTEM_INSTRUCTION = "Bạn là một trợ lý AI chuyên về dịch vụ xe hơi. Hãy trả lời ngắn gọn, lịch sự, đưa ra ý chính và chỉ cung cấp thông tin liên quan đến xe hơi, dịch vụ sửa chữa, hoặc bảo trì. Nếu không biết câu trả lời, hãy nói 'Tôi không có thông tin về vấn đề này, vui lòng liên hệ trung tâm dịch vụ.'";
    private final String MODEL_NAME = "gemini-2.0-flash-001";
    private final String ERROR_MESSAGE = "Rất xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.";
    private final String AI_NOT_AVAILABLE = "Trợ lý AI hiện không khả dụng. Vui lòng thử lại sau.";
    @Value("${google.gemini.api.key}")
    private String apiKey;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    private Client geminiClient;


    public ChatbotServiceImpl() {
        // Cấu hình Markdown parser
        MutableDataSet options = new MutableDataSet();
        // Có thể thêm các extensions nếu muốn hỗ trợ nhiều tính năng Markdown hơn
        // options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }


    // Quản lý các phiên chat bất đồng bộ


    private final Map<String, Chat> activeChatSessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.geminiClient = Client.builder().apiKey(this.apiKey).build();
    }

    @Override
    public GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO> getAiReply(ChatRequestDTO chatRequestDTO) {
        if (this.geminiClient == null) {
            return createErrorResponse(AI_NOT_AVAILABLE);
        }
        try {
            Content systemInstruction = Content.fromParts(Part.fromText(DEFAULT_SYSTEM_INSTRUCTION));
            // Tạo cấu hình với system instruction
            GenerateContentConfig config = createGenerateContentConfig(systemInstruction);
            // Lấy hoặc tạo phiên chat
            Chat chatSession = activeChatSessions.computeIfAbsent(chatRequestDTO.sessionId(), key ->
                    this.geminiClient.chats.create(MODEL_NAME, config)
            );

            // Lời gọi này là BLOCKING.
            // Nhưng vì nó đang chạy trên một Virtual Thread, thread "thật" của hệ điều hành
            // đã được giải phóng để đi xử lý request khác.
            GenerateContentResponse response = chatSession.sendMessage(chatRequestDTO.message(), config);

            // Code tuần tự, dễ đọc
            String aiReplyText = response.text();
            String formattedReply = convertMarkdownToHtml(aiReplyText);
            ChatResponseDTO responseData = new ChatResponseDTO(formattedReply);

            return createSuccessResponse(responseData);

        } catch (Exception e) {
            // Bắt lỗi một cách thông thường bằng try-catch
            return createErrorResponse(ERROR_MESSAGE);
        }
    }


    // Hàm helper để tạo response thành công
    private GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO> createSuccessResponse(ChatResponseDTO data) {
        return GlobalResponseDTO.<NoPaginatedMeta, ChatResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Thành công").build())
                .data(data)
                .build();
    }

    // Hàm helper để tạo response lỗi
    private GlobalResponseDTO<NoPaginatedMeta, ChatResponseDTO> createErrorResponse(String message) {
        return GlobalResponseDTO.<NoPaginatedMeta, ChatResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(message).build())
                .build();
    }

    /**
     * Chuyển đổi văn bản Markdown thành HTML an toàn.
     * @param markdownText Văn bản Markdown từ AI.
     * @return Văn bản HTML đã được chuyển đổi.
     */
    private String convertMarkdownToHtml(String markdownText) {
        // Parse Markdown thành AST (Abstract Syntax Tree)
        Node document = markdownParser.parse(markdownText);
        // Render AST thành HTML
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
