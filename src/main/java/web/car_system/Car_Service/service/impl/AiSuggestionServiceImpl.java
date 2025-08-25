package web.car_system.Car_Service.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.ai_suggestion_request.SuggestionRequestDTO;
import web.car_system.Car_Service.domain.dto.ai_suggestion_request.SuggestionResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.AiSuggestionService;

@Service
public class AiSuggestionServiceImpl implements web.car_system.Car_Service.service.AiSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(AiSuggestionServiceImpl.class);

    private static final String MODEL_NAME = "gemini-1.5-flash-latest"; // Cập nhật model nếu cần
    private static final String ERROR_MESSAGE = "Đã có lỗi xảy ra! Vui lòng thử lại sau.";

    // 1. Inject API Key trực tiếp vào service từ file application.yml
    @Value("${google.gemini.api.key}")
    private String apiKey;

    // 2. Khai báo các dependency mà service sẽ tự tạo
    private Client geminiClient;

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    // 3. Constructor để khởi tạo các công cụ Markdown (giống ChatbotServiceImpl)
    public AiSuggestionServiceImpl() {
        MutableDataSet options = new MutableDataSet();
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }

    // 4. Dùng @PostConstruct để khởi tạo geminiClient SAU KHI apiKey được inject
    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_API_KEY")) {
            logger.error("!!! Google Gemini API key is not configured properly !!!");
            throw new IllegalArgumentException("Google Gemini API key is missing or invalid.");
        }
        // Khởi tạo client trực tiếp trong service
        this.geminiClient = Client.builder().apiKey(this.apiKey).build();
        logger.info("Gemini Client for AiSuggestionService initialized successfully.");
    }

    // ghi đè
    @Override
    public GlobalResponseDTO<NoPaginatedMeta, SuggestionResponseDTO> getSuggestions(SuggestionRequestDTO request) {
        // Thêm kiểm tra client đã được khởi tạo chưa
        if (this.geminiClient == null) {
            logger.warn("Gemini Client is not available. Check API key configuration.");
            return createErrorResponse("Dịch vụ AI hiện không khả dụng.");
        }

        try {
            String prompt = buildPrompt(request.carName(), request.specificationsText());

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .maxOutputTokens(1500)
                    .candidateCount(1)
                    .build();

            // Sử dụng geminiClient của class này
            GenerateContentResponse response = this.geminiClient.models.generateContent(MODEL_NAME, prompt, config);

            String markdownOutput = response.text();
            if (markdownOutput == null || markdownOutput.isBlank()) {
                return createErrorResponse("Không nhận được phản hồi từ AI");
            }

            String htmlOutput = convertMarkdownToHtml(markdownOutput);

            SuggestionResponseDTO data = SuggestionResponseDTO.builder()
                    .markdown(markdownOutput)
                    .html(htmlOutput)
                    .build();

            return createSuccessResponse(data);
        } catch (Exception e) {
            logger.error("Error in AI suggestion service", e);
            return createErrorResponse(ERROR_MESSAGE);
        }
    }

    private String buildPrompt (String carName, String specs){
        // ... (Nội dung hàm buildPrompt giữ nguyên, không cần thay đổi)
        return String.format(
                """
                Bạn là một chuyên gia tư vấn bán xe ô tô chuyên nghiệp và am hiểu kỹ thuật.
                Dựa vào các thông số kỹ thuật chi tiết của chiếc xe '%s' được cung cấp dưới đây, hãy viết một bài đánh giá và tư vấn ngắn gọn (khoảng 150-200 từ) cho một khách hàng đang cân nhắc mua xe này.
    
                Yêu cầu về nội dung:
                1.  **Điểm mạnh nổi bật:** Chọn ra 2-3 điểm mạnh nhất (ví dụ: hiệu suất động cơ, công nghệ an toàn, sự rộng rãi) và giải thích ngắn gọn tại sao nó đáng giá.
                2.  **Điểm cần cân nhắc:** Nêu ra 1-2 điểm mà khách hàng cần lưu ý hoặc có thể là nhược điểm so với đối thủ (ví dụ: mức tiêu thụ nhiên liệu, giá phụ tùng, không gian hàng ghế sau).
                3.  **Đối tượng phù hợp:** Kết luận bằng việc gợi ý chiếc xe này phù hợp nhất với đối tượng nào (ví dụ: gia đình trẻ, người độc thân yêu tốc độ, doanh nhân).
    
                Yêu cầu về định dạng:
                -   Sử dụng Markdown.
                -   Dùng tiêu đề (heading 3) cho mỗi phần.
                -   Dùng gạch đầu dòng (bullet points) để liệt kê các ý cho rõ ràng.
                -   Không dùng lời chào hỏi, đi thẳng vào vấn đề.
    
                Dưới đây là thông số kỹ thuật:
                ---
                %s
                ---
                """,
                carName, specs
        );
    }

    // Các hàm helper giữ nguyên
    private GlobalResponseDTO<NoPaginatedMeta, SuggestionResponseDTO> createSuccessResponse(SuggestionResponseDTO data) {
        return GlobalResponseDTO.<NoPaginatedMeta, SuggestionResponseDTO> builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Phân tích thành công").build())
                .data(data)
                .build();
    }

    private GlobalResponseDTO<NoPaginatedMeta, SuggestionResponseDTO> createErrorResponse(String message) {
        return GlobalResponseDTO.<NoPaginatedMeta, SuggestionResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.ERROR).message(message).build())
                .build();
    }

    private String convertMarkdownToHtml(String markdownText) {
        Node document = this.markdownParser.parse(markdownText);
        return this.htmlRenderer.render(document);
    }
}