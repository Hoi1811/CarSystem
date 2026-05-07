package web.car_system.Car_Service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe binding cho block `ai.rag.*` trong application.yml.
 * Mọi tham số tinh chỉnh RAG (model, dim, top-k, cron, retry) đều xuất phát từ đây
 * — tránh hardcode rải rác trong service.
 *
 * Provider:
 *  - "local"  → LocalEmbeddingServiceImpl (DJL + sentence-transformers, chạy trong JVM)
 *  - "gemini" → EmbeddingServiceImpl (Gemini API, có rate limit free tier 5 RPM/100 RPD)
 */
@Configuration
@ConfigurationProperties(prefix = "ai.rag")
public class RagProperties {

    private boolean enabled = true;
    private Embedding embedding = new Embedding();
    private Chat chat = new Chat();
    private Retrieval retrieval = new Retrieval();
    private Scheduler scheduler = new Scheduler();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Embedding getEmbedding() { return embedding; }
    public void setEmbedding(Embedding embedding) { this.embedding = embedding; }
    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }
    public Retrieval getRetrieval() { return retrieval; }
    public void setRetrieval(Retrieval retrieval) { this.retrieval = retrieval; }
    public Scheduler getScheduler() { return scheduler; }
    public void setScheduler(Scheduler scheduler) { this.scheduler = scheduler; }

    public static class Embedding {
        /** "local" hoặc "gemini" — quyết định bean nào được @ConditionalOnProperty load */
        private String provider = "local";
        /** Tên model — dùng làm modelVersion trong DB CarEmbedding để phân biệt provider */
        private String model = "paraphrase-multilingual-MiniLM-L12-v2";
        /** Số chiều vector. Local MiniLM = 384, Gemini = 768. Lưu mỗi row CarEmbedding. */
        private int dimensions = 384;
        /** Sleep giữa request lúc batch — chỉ cần cho gemini provider */
        private long batchSleepMs = 0;
        /** Số lần retry khi gọi API lỗi — chỉ cho gemini */
        private int maxRetries = 3;

        // Gemini-specific
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

        // Local DJL-specific
        private Local local = new Local();

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getDimensions() { return dimensions; }
        public void setDimensions(int dimensions) { this.dimensions = dimensions; }
        public long getBatchSleepMs() { return batchSleepMs; }
        public void setBatchSleepMs(long batchSleepMs) { this.batchSleepMs = batchSleepMs; }
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public Local getLocal() { return local; }
        public void setLocal(Local local) { this.local = local; }
    }

    /** Cấu hình riêng cho LocalEmbeddingServiceImpl (DJL + sentence-transformers). */
    public static class Local {
        /**
         * URL model theo định dạng DJL. Mặc định lấy paraphrase-multilingual-MiniLM-L12-v2
         * — model chuẩn cho text embedding đa ngôn ngữ (gồm tiếng Việt), 384 chiều.
         */
        private String modelUrl =
                "djl://ai.djl.huggingface.pytorch/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2";
        /** Engine DJL. PyTorch ổn định nhất với HuggingFace models. */
        private String engine = "PyTorch";
        /** Token tối đa per text, vượt sẽ bị truncate (MiniLM giới hạn 512 token). */
        private int maxLength = 512;
        /** Có normalize vector về unit length không (khuyến nghị true cho cosine similarity). */
        private boolean normalize = true;

        public String getModelUrl() { return modelUrl; }
        public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }
        public String getEngine() { return engine; }
        public void setEngine(String engine) { this.engine = engine; }
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        public boolean isNormalize() { return normalize; }
        public void setNormalize(boolean normalize) { this.normalize = normalize; }
    }

    public static class Chat {
        private String model = "gemini-3.1-flash-lite-preview";
        private int maxOutputTokens = 1024;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getMaxOutputTokens() { return maxOutputTokens; }
        public void setMaxOutputTokens(int maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
    }

    public static class Retrieval {
        private int topK = 5;
        private double minScore = 0.55;

        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }
        public double getMinScore() { return minScore; }
        public void setMinScore(double minScore) { this.minScore = minScore; }
    }

    public static class Scheduler {
        private boolean enabled = true;
        private String cron = "0 0 2 * * *";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getCron() { return cron; }
        public void setCron(String cron) { this.cron = cron; }
    }
}
