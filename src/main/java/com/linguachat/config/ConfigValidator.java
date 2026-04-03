package com.linguachat.config;

import com.linguachat.LinguaChatMod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigValidator {
    // ISO 639-1 codes + common regional variants
    private static final Set<String> VALID_LANGUAGE_CODES = new HashSet<>(Arrays.asList(
        "auto", "en", "ru", "de", "fr", "es", "it", "ja", "zh", "ko", "pt", "ar", "hi", "tr",
        "pl", "nl", "sv", "da", "fi", "no", "cs", "sk", "hu", "ro", "bg", "uk", "el", "he",
        "th", "vi", "id", "ms", "tl", "bn", "ta", "te", "mr", "ur", "fa", "sw", "am", "ne",
        "en-US", "en-GB", "pt-BR", "pt-PT", "zh-CN", "zh-TW"
    ));

    private static final Set<String> VALID_TRANSLATORS = new HashSet<>(Arrays.asList(
        "google", "deepl", "kagi"
    ));

    private static final int MIN_CACHE_SIZE = 64;
    private static final int MAX_CACHE_SIZE = 4096;

    public static class ValidationResult {
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private boolean configModified = false;

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void setConfigModified(boolean modified) {
            this.configModified = modified;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean isConfigModified() {
            return configModified;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Validates config and auto-fixes what it can
     */
    public static ValidationResult validate(ModConfig config) {
        ValidationResult result = new ValidationResult();

        validateLanguageCode(config.getDefaultSourceLang(), "defaultSourceLang", result);
        validateLanguageCode(config.getDefaultTargetLang(), "defaultTargetLang", result);
        validatePreferredTranslator(config, result);
        validateCacheSize(config, result);
        validateProviderAvailability(config, result);

        return result;
    }

    private static void validateLanguageCode(String code, String fieldName, ValidationResult result) {
        if (code == null || code.isEmpty()) {
            result.addError(String.format("Field %s is empty", fieldName));
            return;
        }

        String normalized = code.toLowerCase().trim();
        if (!VALID_LANGUAGE_CODES.contains(normalized)) {
            // Try base code (first 2 chars) before failing
            if (normalized.length() >= 2) {
                String baseCode = normalized.substring(0, 2);
                if (VALID_LANGUAGE_CODES.contains(baseCode)) {
                    result.addWarning(String.format(
                        "Language code '%s' in field %s is non-standard, but base code '%s' is valid",
                        code, fieldName, baseCode
                    ));
                    return;
                }
            }
            result.addWarning(String.format(
                "Language code '%s' in field %s is not recognized (ISO 639-1)",
                code, fieldName
            ));
        }
    }

    private static void validatePreferredTranslator(ModConfig config, ValidationResult result) {
        String translator = config.getPreferredTranslator();
        if (translator == null || translator.isEmpty()) {
            result.addError("Field preferredTranslator is empty, using 'google' as default");
            config.setPreferredTranslator("google");
            result.setConfigModified(true);
            return;
        }

        String normalized = translator.toLowerCase().trim();
        if (!VALID_TRANSLATORS.contains(normalized)) {
            result.addWarning(String.format(
                "Unknown provider '%s', using 'google' as default",
                translator
            ));
            config.setPreferredTranslator("google");
            result.setConfigModified(true);
        }
    }

    private static void validateCacheSize(ModConfig config, ValidationResult result) {
        int cacheSize = config.getCacheSize();
        
        if (cacheSize < MIN_CACHE_SIZE) {
            result.addWarning(String.format(
                "Cache size %d is too small, set to minimum %d",
                cacheSize, MIN_CACHE_SIZE
            ));
            config.setCacheSize(MIN_CACHE_SIZE);
            result.setConfigModified(true);
        } else if (cacheSize > MAX_CACHE_SIZE) {
            result.addWarning(String.format(
                "Cache size %d is too large, set to maximum %d",
                cacheSize, MAX_CACHE_SIZE
            ));
            config.setCacheSize(MAX_CACHE_SIZE);
            result.setConfigModified(true);
        }
    }

    private static void validateProviderAvailability(ModConfig config, ValidationResult result) {
        String preferred = config.getPreferredTranslator().toLowerCase();
        
        boolean providerAvailable;
        if ("deepl".equals(preferred)) {
            String apiKey = config.getDeeplApiKey();
            providerAvailable = apiKey != null && !apiKey.isEmpty();
            if (!providerAvailable) {
                result.addWarning("DeepL is selected as preferred, but API key is not specified. Will fallback to Google.");
            }
        } else if ("kagi".equals(preferred)) {
            String apiKey = config.getKagiApiKey();
            String sessionToken = config.getKagiSessionToken();
            providerAvailable = (apiKey != null && !apiKey.isEmpty()) || 
                               (sessionToken != null && !sessionToken.isEmpty());
            if (!providerAvailable) {
                result.addWarning("Kagi is selected as preferred, but neither API key nor session token is specified. Will fallback to Google.");
            }
        } else if ("google".equals(preferred)) {
            providerAvailable = true;
        } else {
            result.addWarning("Unknown provider, will use Google");
            providerAvailable = true;
        }

        if (!providerAvailable && !"google".equals(preferred)) {
            result.addWarning("Selected provider is unavailable, using Google Translate as fallback");
        }
    }

    /**
     * Logs validation results to console
     */
    public static void logValidationResults(ValidationResult result) {
        if (result.hasErrors()) {
            LinguaChatMod.LOGGER.error(com.linguachat.compat.I18nCompat.translate("linguachat.log.validation_errors_header"));
            for (String error : result.getErrors()) {
                LinguaChatMod.LOGGER.error(com.linguachat.compat.I18nCompat.translate("linguachat.log.validation_error_item", error));
            }
        }

        if (result.hasWarnings()) {
            LinguaChatMod.LOGGER.warn(com.linguachat.compat.I18nCompat.translate("linguachat.log.validation_warnings_header"));
            for (String warning : result.getWarnings()) {
                LinguaChatMod.LOGGER.warn(com.linguachat.compat.I18nCompat.translate("linguachat.log.validation_warning_item", warning));
            }
        }

        if (result.isConfigModified()) {
            LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_auto_corrected"));
        }

        if (!result.hasErrors() && !result.hasWarnings()) {
            LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_valid"));
        }
    }
}
