package com.linguachat.config;

import com.linguachat.LinguaChatMod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ConfigValidator {
    private static final Pattern LANGUAGE_CODE_PATTERN = Pattern.compile("^[a-z]{2,3}(-[a-z0-9]{2,8})?$");

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

        // normalize codes in place: old configs may hold " EN " / "EN" from free-text input
        String src = normalize(config.getDefaultSourceLang());
        if (!src.equals(config.getDefaultSourceLang())) {
            config.setDefaultSourceLang(src);
            result.setConfigModified(true);
        }
        String tgt = normalize(config.getDefaultTargetLang());
        if (!tgt.equals(config.getDefaultTargetLang())) {
            config.setDefaultTargetLang(tgt);
            result.setConfigModified(true);
        }
        validateLanguageCode(config.getDefaultSourceLang(), "defaultSourceLang", result);
        validateLanguageCode(config.getDefaultTargetLang(), "defaultTargetLang", result);
        validatePreferredTranslator(config, result);
        validateCacheSize(config, result);
        validateProviderAvailability(config, result);

        return result;
    }

    private static String normalize(String code) {
        return code == null ? "" : code.trim().replace('_', '-').toLowerCase();
    }

    private static void validateLanguageCode(String code, String fieldName, ValidationResult result) {
        if (code == null || code.isEmpty()) {
            result.addError(String.format("Field %s is empty", fieldName));
            return;
        }

        String normalized = normalize(code);
        if (!"auto".equals(normalized) && !LANGUAGE_CODE_PATTERN.matcher(normalized).matches()) {
            result.addWarning(String.format(
                "Language code '%s' in field %s does not look like an ISO-639 code",
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
            providerAvailable = apiKey != null && !apiKey.trim().isEmpty();
            if (!providerAvailable) {
                result.addWarning("DeepL is selected as preferred, but API key is not specified. Will fallback to Google.");
            }
        } else if ("kagi".equals(preferred)) {
            String apiKey = config.getKagiApiKey();
            String sessionToken = config.getKagiSessionToken();
            providerAvailable = (apiKey != null && !apiKey.trim().isEmpty())
                || (sessionToken != null && !sessionToken.trim().isEmpty());
            if (!providerAvailable) {
                result.addWarning("Kagi is selected as preferred, but neither API key nor session token is specified. Will fallback to Google.");
            }
        } else { // "google"
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
            LinguaChatMod.LOGGER.error(ModConfig.logText("linguachat.log.validation_errors_header"));
            for (String error : result.getErrors()) {
                LinguaChatMod.LOGGER.error(ModConfig.logText("linguachat.log.validation_error_item", error));
            }
        }

        if (result.hasWarnings()) {
            LinguaChatMod.LOGGER.warn(ModConfig.logText("linguachat.log.validation_warnings_header"));
            for (String warning : result.getWarnings()) {
                LinguaChatMod.LOGGER.warn(ModConfig.logText("linguachat.log.validation_warning_item", warning));
            }
        }

        if (result.isConfigModified()) {
            LinguaChatMod.LOGGER.info(ModConfig.logText("linguachat.log.config_auto_corrected"));
        }

        if (!result.hasErrors() && !result.hasWarnings()) {
            LinguaChatMod.LOGGER.info(ModConfig.logText("linguachat.log.config_valid"));
        }
    }
}
