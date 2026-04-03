package com.linguachat.translation.providers;

/**
 * Exception for translation errors
 */
public class TranslationException extends Exception {
    private final ErrorType errorType;
    
    public enum ErrorType {
        AUTHENTICATION_ERROR,  // Authentication error (401, 403)
        RATE_LIMIT_ERROR,      // Rate limit exceeded (429)
        NETWORK_ERROR,         // Network error, timeout
        PARSE_ERROR,           // Response parsing error
        INVALID_LANGUAGE_CODE, // Invalid language code
        SERVER_ERROR,          // Server error (5xx)
        TIMEOUT_ERROR          // Request timeout
    }
    
    public TranslationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public TranslationException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
}
