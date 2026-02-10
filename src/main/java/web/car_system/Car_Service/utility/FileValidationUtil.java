package web.car_system.Car_Service.utility;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Utility class để validate file uploads
 * Prevents malicious files, oversized uploads, và invalid file types
 */
@Component
public class FileValidationUtil {

    // Constants
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_FILES_PER_REQUEST = 10;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    /**
     * Validate danh sách ảnh (cho car creation/update)
     * @param images List MultipartFile cần validate
     * @throws IllegalArgumentException nếu validation fails
     */
    public void validateImages(List<MultipartFile> images) {
        validateImages(images, true); // Required by default
    }

    /**
     * Validate danh sách ảnh với optional requirement
     * @param images List MultipartFile cần validate
     * @param required Có bắt buộc phải có ảnh không
     * @throws IllegalArgumentException nếu validation fails
     */
    public void validateImages(List<MultipartFile> images, boolean required) {
        // Check if images list is null or empty
        if (images == null || images.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("Ít nhất 1 ảnh là bắt buộc");
            }
            return; // Optional and no images provided - OK
        }

        // Check max number of files
        if (images.size() > MAX_FILES_PER_REQUEST) {
            throw new IllegalArgumentException(
                    String.format("Tối đa %d ảnh mỗi lần upload. Bạn đã upload %d ảnh.",
                            MAX_FILES_PER_REQUEST, images.size())
            );
        }

        // Validate each file
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            try {
                validateSingleImage(image);
            } catch (IllegalArgumentException e) {
                // Add index to error message for better UX
                throw new IllegalArgumentException(
                        String.format("Ảnh #%d: %s", i + 1, e.getMessage())
                );
            }
        }
    }

    /**
     * Validate 1 ảnh đơn lẻ
     * @param file MultipartFile cần validate
     * @throws IllegalArgumentException nếu validation fails
     */
    public void validateSingleImage(MultipartFile file) {
        // Check for null
        if (file == null) {
            throw new IllegalArgumentException("File không được null");
        }

        // Check for empty file
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("File '%s' rỗng", file.getOriginalFilename())
            );
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            long sizeMB = file.getSize() / (1024 * 1024);
            throw new IllegalArgumentException(
                    String.format("File '%s' quá lớn (%d MB). Tối đa 5 MB.",
                            file.getOriginalFilename(), sizeMB)
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("File '%s' không có content type", file.getOriginalFilename())
            );
        }

        if (!ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("File '%s' không phải định dạng ảnh hợp lệ. Chỉ chấp nhận: JPEG, JPG, PNG, WEBP",
                            file.getOriginalFilename())
            );
        }

        // Additional security: Check file extension matches content type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.isEmpty()) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isExtensionMatchingContentType(extension, contentType)) {
                throw new IllegalArgumentException(
                        String.format("File '%s': Extension không khớp với content type",
                                originalFilename)
                );
            }
        }
    }

    /**
     * Lấy file extension từ filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * Kiểm tra extension có match với content type không
     */
    private boolean isExtensionMatchingContentType(String extension, String contentType) {
        return switch (extension) {
            case "jpg", "jpeg" -> contentType.equals("image/jpeg") || contentType.equals("image/jpg");
            case "png" -> contentType.equals("image/png");
            case "webp" -> contentType.equals("image/webp");
            default -> false;
        };
    }
}
