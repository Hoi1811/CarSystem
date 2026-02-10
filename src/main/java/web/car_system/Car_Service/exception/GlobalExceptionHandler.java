package web.car_system.Car_Service.exception;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message(errorMessage)
                                .build())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<String> handleClientAbort(ClientAbortException ex) {
        log.warn("Client disconnected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Client disconnected");
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleAsyncTimeout(AsyncRequestTimeoutException ex) {
        log.warn("Async request timeout: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message("Request timeout")
                                .build())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message(ex.getMessage())
                                .build())
                        .data(null)
                        .build());
    }
    
    /**
     * Handle business logic violations
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message(ex.getMessage())
                                .build())
                        .data(null)
                        .build());
    }
    
    /**
     * Handler cho các lỗi không tìm thấy tài nguyên (ví dụ: xe, người dùng...).
     * Trả về mã lỗi 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body( // <-- Trả về 404
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message(ex.getMessage()) // <-- Lấy message từ exception
                                .build())
                        .data(null)
                        .build());
    }

    /**
     * Handler cho các lỗi về trạng thái hoặc logic nghiệp vụ không hợp lệ.
     * Trả về mã lỗi 400 Bad Request.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( // <-- Trả về 400
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message(ex.getMessage()) // <-- Lấy message từ exception
                                .build())
                        .data(null)
                        .build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponseDTO<?, ?>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                        .meta(NoPaginatedMeta.builder()
                                .status(Status.ERROR)
                                .message("An unexpected error occurred")
                                .build())
                        .data(null)
                        .build());
    }
}