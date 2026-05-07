package web.car_system.Car_Service.service.advisor.impl;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.config.RagProperties;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorBreakdownResponse;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorChatRequest;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorChatResponse;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.service.advisor.EmbeddingService;
import web.car_system.Car_Service.service.advisor.RagAdvisorService;
import web.car_system.Car_Service.service.advisor.VectorSearchService;
import web.car_system.Car_Service.service.advisor.util.QueryConstraintExtractor;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Trái tim của RAG. Trật tự bước:
 *
 *   1) embedQuery(userQuery)                       → float[768]
 *   2) vectorSearchService.findTopK(qVec, K)       → List<ScoredCar>
 *   3) carRepository.findAllById(carIds)           → load LIVE từ DB
 *      (giá/thông số luôn mới nhất, embedding chỉ dùng để retrieve)
 *   4) buildPrompt(SYSTEM + cars + query)
 *   5) geminiClient.models.generateContent(...)    → text markdown
 *
 * Prompt thiết kế chống hallucination 3 lớp:
 *  - Quy tắc cứng "TUYỆT ĐỐI không nhắc xe ngoài danh sách"
 *  - Context-only: chỉ đưa top-K xe vào prompt
 *  - Cấu trúc cố định: ép LLM trả theo template để FE render đẹp
 */
@Service
public class RagAdvisorServiceImpl implements RagAdvisorService {

    private static final Logger log = LoggerFactory.getLogger(RagAdvisorServiceImpl.class);

    // Prompt rút gọn ~60% so với bản đầu — giữ NGUYÊN tắc anti-hallucination + cấu trúc.
    // Prompt ngắn → Gemini xử lý input phase nhanh hơn, output phase cũng có thiên hướng
    // ngắn theo (LLM mimic tone từ instruction).
    private static final String SYSTEM_INSTRUCTION = """
            Bạn là chuyên gia tư vấn xe tại showroom HOI_NGUYEN.

            QUY TẮC:
            - CHỈ trích xe trong "DANH SÁCH XE PHÙ HỢP" — KHÔNG bịa xe ngoài.
            - Nếu không có xe khớp hoàn toàn → nói "chưa có xe khớp" và gợi ý xe gần nhất.
            - Chỉ dùng thông số THỰC trong danh sách. Thiếu thì nói "thông tin chưa có".
            - Tối đa 3 xe, dưới 200 từ, Markdown.
            - Cuối câu LUÔN hỏi follow-up.

            CẤU TRÚC:
            **Dựa trên nhu cầu của anh/chị, tôi gợi ý:**

            ### 1. [Tên xe] — [Giá]
            - ✅ [Lý do phù hợp]
            - 💡 [Lưu ý]

            **[Câu hỏi follow-up]**
            """;

    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;
    private final CarRepository carRepository;
    private final Client geminiClient;
    private final RagProperties props;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public RagAdvisorServiceImpl(EmbeddingService embeddingService,
                                 VectorSearchService vectorSearchService,
                                 CarRepository carRepository,
                                 Client geminiClient,
                                 RagProperties props) {
        this.embeddingService = embeddingService;
        this.vectorSearchService = vectorSearchService;
        this.carRepository = carRepository;
        this.geminiClient = geminiClient;
        this.props = props;
        MutableDataSet options = new MutableDataSet();
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdvisorChatResponse chat(AdvisorChatRequest request) {
        long start = System.currentTimeMillis();
        ChatPipelineResult result = runPipeline(request);
        long latency = System.currentTimeMillis() - start;

        List<AdvisorChatResponse.RetrievedCar> retrievedDtos = result.cars.stream()
                .map(c -> toRetrieved(c, scoreOf(result.scoredCarIds, c.getCarId())))
                .toList();

        return new AdvisorChatResponse(
                result.llmAnswer,
                htmlRenderer.render(markdownParser.parse(result.llmAnswer)),
                retrievedDtos,
                new AdvisorChatResponse.Meta(
                        request.sessionId() == null ? UUID.randomUUID().toString() : request.sessionId(),
                        latency,
                        result.cars.size(),
                        props.getChat().getModel(),
                        props.getEmbedding().getModel()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdvisorBreakdownResponse breakdown(AdvisorChatRequest request) {
        long start = System.currentTimeMillis();
        ChatPipelineResult result = runPipeline(request);
        long latency = System.currentTimeMillis() - start;

        // Preview 5 số đầu của query embedding (đừng dump cả 768)
        List<Float> preview = new ArrayList<>(5);
        for (int i = 0; i < Math.min(5, result.queryVector.length); i++) {
            preview.add(result.queryVector[i]);
        }

        List<AdvisorBreakdownResponse.ScoredCarPreview> scored = result.cars.stream()
                .map(c -> new AdvisorBreakdownResponse.ScoredCarPreview(
                        c.getCarId(), c.getName(), c.getModel(),
                        scoreOf(result.scoredCarIds, c.getCarId())))
                .toList();

        return new AdvisorBreakdownResponse(
                request.query(),
                preview,
                scored,
                truncate(result.fullPrompt, 2000),
                result.llmAnswer,
                latency
        );
    }

    // ==================== Pipeline shared bởi chat() & breakdown() ====================

    private record ChatPipelineResult(
            float[] queryVector,
            List<VectorSearchService.ScoredCar> scoredCarIds,
            List<Car> cars,
            String fullPrompt,
            String llmAnswer
    ) {}

    private ChatPipelineResult runPipeline(AdvisorChatRequest request) {
        if (!props.isEnabled()) {
            throw new BusinessException("AI Advisor đang bị tắt (ai.rag.enabled=false)");
        }
        String query = request.query();

        // Bước 1: embed query
        float[] qVec = embeddingService.embedQuery(query);

        // Bước 2: HYBRID retrieval — extract hard constraint (price/seats/EV) bằng regex
        // rồi áp filter trước khi cosine. Đảm bảo budget/seats/EV-intent KHÔNG bị vi phạm.
        Optional<BigDecimal> maxPrice = QueryConstraintExtractor.extractMaxPrice(query);
        Optional<Integer> minSeats = QueryConstraintExtractor.extractMinSeats(query);
        boolean wantsEv = QueryConstraintExtractor.wantsPureEv(query);
        Predicate<VectorSearchService.CarVecInfo> filter = buildFilter(maxPrice, minSeats, wantsEv);

        if (maxPrice.isPresent() || minSeats.isPresent() || wantsEv) {
            log.info("Hybrid filter applied: maxPrice={}, minSeats={}, wantsEv={}",
                    maxPrice.map(BigDecimal::toPlainString).orElse("none"),
                    minSeats.map(String::valueOf).orElse("none"),
                    wantsEv);
        }

        int topK = props.getRetrieval().getTopK();
        List<VectorSearchService.ScoredCar> scored = vectorSearchService.findTopK(qVec, topK, filter);

        // Fallback: nếu filter quá khắt khe → 0 kết quả → relax filter và search lại
        if (scored.isEmpty() && (maxPrice.isPresent() || minSeats.isPresent())) {
            log.warn("Hybrid filter quá khắt khe, fallback sang cosine thuần");
            scored = vectorSearchService.findTopK(qVec, topK);
        }

        if (scored.isEmpty()) {
            throw new BusinessException(
                    "Chưa có dữ liệu xe nào phù hợp. Vui lòng đợi hệ thống học catalog (chạy embedding pipeline).");
        }

        // Bước 3: load LIVE từ DB (giữ giá/thông số mới nhất)
        List<Integer> carIds = scored.stream().map(VectorSearchService.ScoredCar::carId).toList();
        List<Car> carsRaw = carRepository.findAllById(carIds);
        // sắp lại theo thứ tự score giảm dần
        Map<Integer, Car> byId = carsRaw.stream().collect(Collectors.toMap(Car::getCarId, c -> c));
        List<Car> carsOrdered = carIds.stream().map(byId::get).filter(Objects::nonNull).toList();

        // Bước 4: build prompt
        String fullPrompt = buildPrompt(carsOrdered, query);

        // Bước 5: gọi LLM
        String llmAnswer = callGemini(fullPrompt);

        return new ChatPipelineResult(qVec, scored, carsOrdered, fullPrompt, llmAnswer);
    }

    private String buildPrompt(List<Car> cars, String userQuery) {
        StringBuilder ctx = new StringBuilder(2000);
        ctx.append("DANH SÁCH XE PHÙ HỢP (CHỈ TRÍCH DẪN XE Ở ĐÂY):\n\n");
        for (int i = 0; i < cars.size(); i++) {
            Car c = cars.get(i);
            String mfg = c.getManufacturer() != null ? c.getManufacturer().getName() : "";
            String seg = c.getCarSegment() != null ? c.getCarSegment().getName() : "";
            ctx.append(String.format(Locale.ROOT,
                    "[%d] %s %s %s (%d) — Giá: %s VND%n" +
                            "    Phân khúc: %s | Hãng: %s%n" +
                            "    %s chỗ | Động cơ: %s | Hộp số: %s | Dẫn động: %s%n" +
                            "    Công suất: %s HP | Tiêu hao: %s L/100km | %s túi khí%n%n",
                    i + 1, mfg, c.getName(), c.getModel(), c.getYear(), c.getPrice(),
                    seg, mfg,
                    nullSafe(c.getSeats()),
                    nullSafe(c.getEngineType()),
                    nullSafe(c.getTransmissionType()),
                    nullSafe(c.getDriveTrain()),
                    nullSafe(c.getHorsepower()),
                    nullSafe(c.getFuelConsumption()),
                    nullSafe(c.getAirbagCount())
            ));
        }
        ctx.append("\nCÂU HỎI KHÁCH HÀNG:\n").append(userQuery);
        return ctx.toString();
    }

    private String callGemini(String fullPrompt) {
        try {
            Content systemInstruction = Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION));
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .candidateCount(1)
                    .maxOutputTokens(props.getChat().getMaxOutputTokens())
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

            GenerateContentResponse resp = geminiClient.models.generateContent(
                    props.getChat().getModel(), fullPrompt, config);
            String text = resp.text();
            if (text == null || text.isBlank()) {
                throw new BusinessException("LLM trả phản hồi rỗng. Vui lòng thử lại.");
            }
            return text;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Gemini chat call failed", ex);
            throw new BusinessException("Lỗi khi gọi AI: " + ex.getMessage());
        }
    }

    // ==================== Helpers ====================

    /**
     * Compose price + seats + EV filter. Null trong CarVecInfo (xe thiếu data) được PASS để
     * không loại oan — embedding vẫn phải xét. LLM sẽ gắn "thông tin chưa có" sau.
     * EV filter là filter CỨNG: nếu user nói "xe điện" thì chỉ trả pure EV.
     */
    private static Predicate<VectorSearchService.CarVecInfo> buildFilter(
            Optional<BigDecimal> maxPrice, Optional<Integer> minSeats, boolean wantsEv) {
        Predicate<VectorSearchService.CarVecInfo> p = info -> true;
        if (maxPrice.isPresent()) {
            BigDecimal cap = maxPrice.get();
            p = p.and(info -> info.price() == null || info.price().compareTo(cap) <= 0);
        }
        if (minSeats.isPresent()) {
            int floor = minSeats.get();
            p = p.and(info -> info.seats() == null || info.seats() >= floor);
        }
        if (wantsEv) {
            p = p.and(VectorSearchService.CarVecInfo::isElectric);
        }
        return p;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "... [truncated]";
    }

    private static String nullSafe(Object o) {
        return o == null ? "?" : o.toString();
    }

    private static double scoreOf(List<VectorSearchService.ScoredCar> list, Integer carId) {
        for (VectorSearchService.ScoredCar s : list) {
            if (s.carId().equals(carId)) return s.score();
        }
        return 0d;
    }

    private static AdvisorChatResponse.RetrievedCar toRetrieved(Car c, double score) {
        return new AdvisorChatResponse.RetrievedCar(
                c.getCarId(),
                c.getName(),
                c.getModel(),
                c.getYear(),
                c.getPrice(),
                c.getThumbnail(),
                c.getCarSegment() != null ? c.getCarSegment().getName() : null,
                c.getManufacturer() != null ? c.getManufacturer().getName() : null,
                score
        );
    }
}
