package web.car_system.Car_Service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.service.DictionaryMappingService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DictionaryMappingServiceImpl implements DictionaryMappingService {

    @Override
    public MappingResult normalizeSpecifications(Map<String, Object> rawSpecifications) {
        Map<String, Object> normalizedData = new HashMap<>();
        Map<String, ValidationFlag> validationFlags = new HashMap<>();

        if (rawSpecifications == null) {
            return new MappingResult(normalizedData, validationFlags);
        }

        // Iterate through each raw spec and map individually
        for (Map.Entry<String, Object> entry : rawSpecifications.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String rawText) {
                switch (key.toLowerCase()) {
                    case "gearbox":
                    case "transmission":
                    case "hộp số":
                        mapGearbox(rawText, normalizedData, validationFlags);
                        break;
                    case "drivetrain":
                    case "dẫn động":
                        mapDrivetrain(rawText, normalizedData, validationFlags);
                        break;
                    case "fuel":
                    case "nhiên liệu":
                        mapFuel(rawText, normalizedData, validationFlags);
                        break;
                    default:
                        // Keep unmapped attributes as-is but flag as success
                        normalizedData.put(key, value);
                        validationFlags.put(key, new ValidationFlag("SUCCESS", rawText, value));
                        break;
                }
            } else {
                normalizedData.put(key, value); // Not a string, just copy
            }
        }

        return new MappingResult(normalizedData, validationFlags);
    }

    private void mapGearbox(String rawText, Map<String, Object> normalizedData, Map<String, ValidationFlag> flags) {
        String lowerText = rawText.toLowerCase().replaceAll("\\s+", "");
        
        // Match CVT
        if (lowerText.contains("cvt") || lowerText.contains("vôcấp")) {
            normalizedData.put("gearbox", "CVT");
            flags.put("gearbox", new ValidationFlag("SUCCESS", rawText, "CVT"));
            return;
        }

        // Match MT (Manual)
        if (lowerText.contains("mts") || lowerText.contains("sànt") || lowerText.contains("sốsàn")) {
            normalizedData.put("gearbox", "MT");
            flags.put("gearbox", new ValidationFlag("SUCCESS", rawText, "MT"));
            return;
        }

        // Match xAT or Auto
        Pattern pattern = Pattern.compile("(\\d+)cấp");
        Matcher matcher = pattern.matcher(lowerText);
        if (lowerText.contains("tựđộng") || lowerText.contains("at")) {
            if (matcher.find()) {
                String mapped = matcher.group(1) + "AT";
                normalizedData.put("gearbox", mapped);
                flags.put("gearbox", new ValidationFlag("SUCCESS", rawText, mapped));
            } else {
                normalizedData.put("gearbox", "AT");
                flags.put("gearbox", new ValidationFlag("SUCCESS", rawText, "AT"));
            }
            return;
        }

        // Warning if no map found
        flags.put("gearbox", new ValidationFlag("WARNING", rawText, null));
    }

    private void mapDrivetrain(String rawText, Map<String, Object> normalizedData, Map<String, ValidationFlag> flags) {
        String lowerText = rawText.toLowerCase().replaceAll("\\s+", "");

        if (lowerText.contains("fwd") || lowerText.contains("cầutrước")) {
            normalizedData.put("drivetrain", "FWD");
            flags.put("drivetrain", new ValidationFlag("SUCCESS", rawText, "FWD"));
        } else if (lowerText.contains("rwd") || lowerText.contains("cầusau")) {
            normalizedData.put("drivetrain", "RWD");
            flags.put("drivetrain", new ValidationFlag("SUCCESS", rawText, "RWD"));
        } else if (lowerText.contains("awd") || lowerText.contains("2cầutoànthờigian") || lowerText.contains("bốnbánh")) {
            normalizedData.put("drivetrain", "AWD");
            flags.put("drivetrain", new ValidationFlag("SUCCESS", rawText, "AWD"));
        } else if (lowerText.contains("4wd") || lowerText.contains("2cầubánthờigian")) {
            normalizedData.put("drivetrain", "4WD");
            flags.put("drivetrain", new ValidationFlag("SUCCESS", rawText, "4WD"));
        } else {
            flags.put("drivetrain", new ValidationFlag("WARNING", rawText, null));
        }
    }

    private void mapFuel(String rawText, Map<String, Object> normalizedData, Map<String, ValidationFlag> flags) {
        String lowerText = rawText.toLowerCase();

        if (lowerText.contains("xăng") || lowerText.contains("petrol") || lowerText.contains("gasoline")) {
            normalizedData.put("fuelType", "PETROL");
            flags.put("fuelType", new ValidationFlag("SUCCESS", rawText, "PETROL"));
        } else if (lowerText.contains("dầu") || lowerText.contains("diesel")) {
            normalizedData.put("fuelType", "DIESEL");
            flags.put("fuelType", new ValidationFlag("SUCCESS", rawText, "DIESEL"));
        } else if (lowerText.contains("điện") || lowerText.contains("ev") || lowerText.contains("electric")) {
            normalizedData.put("fuelType", "ELECTRIC");
            flags.put("fuelType", new ValidationFlag("SUCCESS", rawText, "ELECTRIC"));
        } else if (lowerText.contains("lai") || lowerText.contains("hybrid") || lowerText.contains("hev") || lowerText.contains("phev")) {
            normalizedData.put("fuelType", "HYBRID");
            flags.put("fuelType", new ValidationFlag("SUCCESS", rawText, "HYBRID"));
        } else {
            flags.put("fuelType", new ValidationFlag("WARNING", rawText, null));
        }
    }
}
