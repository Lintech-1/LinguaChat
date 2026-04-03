package com.linguachat.translation;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.I18nCompat;
import com.linguachat.config.ModConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationLogger {
    // rate limit spam: provider + error type -> last log time
    private static final Map<String, Long> lastErrorLogTime = new ConcurrentHashMap<>();
    private static final long ERROR_LOG_INTERVAL_MS = 60000; // 60 seconds

    /**
     * Logs translation request start
     */
    public static void logTranslationRequest(String provider, String text, String sourceLang, String targetLang) {
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate(
                "linguachat.log.translation_request",
                provider, truncate(text, 50), sourceLang, targetLang
            ));
        }
    }

    /**
     * Logs successful response from provider
     */
    public static void logTranslationResponse(String provider, String originalText, String translatedText) {
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate(
                "linguachat.log.translation_success",
                provider, truncate(originalText, 30), truncate(translatedText, 30)
            ));
        }
    }

    /**
     * Logs provider error with rate limiting
     */
    public static void logProviderError(String provider, String errorType, String errorMessage) {
        String key = provider + ":" + errorType;
        long now = System.currentTimeMillis();
        Long lastLog = lastErrorLogTime.get(key);

        // throttle spam - only log same error once per minute
        if (lastLog == null || (now - lastLog) >= ERROR_LOG_INTERVAL_MS) {
            LinguaChatMod.LOGGER.error(I18nCompat.translate(
                "linguachat.log.provider_error",
                provider, errorType, errorMessage
            ));
            lastErrorLogTime.put(key, now);
        }
    }

    /**
     * Logs fallback to another provider
     */
    public static void logFallback(String fromProvider, String toProvider, String reason) {
        LinguaChatMod.LOGGER.warn(I18nCompat.translate(
            "linguachat.log.fallback",
            fromProvider, toProvider, reason
        ));
    }

    /**
     * Logs language code transformation
     */
    public static void logLanguageCodeTransformation(String provider, String originalCode, String transformedCode) {
        if (ModConfig.get().isDebugMode() && !originalCode.equals(transformedCode)) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate(
                "linguachat.log.language_transform",
                provider, originalCode, transformedCode
            ));
        }
    }

    /**
     * Logs full request payload (debug mode only)
     */
    public static void logRequestPayload(String provider, String payload) {
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(String.format(
                "[%s] Request: %s",
                provider, payload
            ));
        }
    }

    /**
     * Logs full response payload (debug mode only)
     */
    public static void logResponsePayload(String provider, int statusCode, String payload) {
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(String.format(
                "[%s] Response (code %d): %s",
                provider, statusCode, truncate(payload, 500)
            ));
        }
    }

    /**
     * Truncates string to specified length with "..." suffix
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
