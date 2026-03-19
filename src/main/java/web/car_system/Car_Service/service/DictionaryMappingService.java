package web.car_system.Car_Service.service;

import java.util.Map;

public interface DictionaryMappingService {
    
    /**
     * Map crawled specifications into standardized format.
     * Contains validation flags indicating SUCCESS or WARNING (needs manual review).
     *
     * @param rawSpecifications Raw JSON string from the crawler
     * @return Resulting mapped specifications wrap representing the normalized JSON 
     *         and the validation flags map object.
     */
    MappingResult normalizeSpecifications(Map<String, Object> rawSpecifications);

    class MappingResult {
        public Map<String, Object> normalizedData;
        public Map<String, ValidationFlag> validationFlags;

        public MappingResult(Map<String, Object> normalizedData, Map<String, ValidationFlag> validationFlags) {
            this.normalizedData = normalizedData;
            this.validationFlags = validationFlags;
        }
    }

    class ValidationFlag {
        public String status; // "SUCCESS" or "WARNING"
        public String originalText;
        public Object mappedTo;

        public ValidationFlag(String status, String originalText, Object mappedTo) {
            this.status = status;
            this.originalText = originalText;
            this.mappedTo = mappedTo;
        }
    }
}
