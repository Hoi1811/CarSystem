package web.car_system.Car_Service.service.advisor;

import web.car_system.Car_Service.domain.dto.advisor.AdvisorBreakdownResponse;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorChatRequest;
import web.car_system.Car_Service.domain.dto.advisor.AdvisorChatResponse;

/**
 * Orchestrator của RAG flow:
 *   1) embed query
 *   2) cosine search top-K
 *   3) build prompt với DANH SÁCH XE + nhu cầu user
 *   4) gọi Gemini chat → trả markdown
 *
 * `chat` = endpoint chính cho khách dùng.
 * `breakdown` = endpoint debug, show inner-working cho thuyết trình.
 */
public interface RagAdvisorService {

    AdvisorChatResponse chat(AdvisorChatRequest request);

    AdvisorBreakdownResponse breakdown(AdvisorChatRequest request);
}
