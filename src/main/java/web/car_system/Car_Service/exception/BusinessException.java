package web.car_system.Car_Service.exception;

/**
 * Business logic exception
 * Used for validation failures and business rule violations
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
