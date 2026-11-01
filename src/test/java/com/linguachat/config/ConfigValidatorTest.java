package com.linguachat.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidatorTest {

    @Test
    @DisplayName("Default configuration should pass validation without errors or modifications")
    void testDefaultConfigIsValid() {
        ModConfig config = new ModConfig();
        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertFalse(result.hasErrors(), "Default config should not have errors: " + result.getErrors());
        assertFalse(result.hasWarnings(), "Default config should not have warnings: " + result.getWarnings());
        assertFalse(result.isConfigModified(), "Default config should not be modified");
        assertEquals("google", config.getPreferredTranslator());
        assertEquals("auto", config.getDefaultSourceLang());
        assertEquals("en", config.getDefaultTargetLang());
        assertEquals(512, config.getCacheSize());
    }

    @Test
    @DisplayName("Cache size below MIN_CACHE_SIZE (64) should be clamped to 64")
    void testCacheSizeMinClamping() {
        ModConfig config = new com.google.gson.Gson().fromJson("{\"cacheSize\": 10}", ModConfig.class);

        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertTrue(result.isConfigModified(), "Config should be marked modified");
        assertEquals(64, config.getCacheSize(), "Cache size should be clamped to minimum 64");
        assertTrue(result.hasWarnings(), "Warning should be reported for clamping cache size");
    }

    @Test
    @DisplayName("Cache size above MAX_CACHE_SIZE (4096) should be clamped to 4096")
    void testCacheSizeMaxClamping() {
        ModConfig config = new com.google.gson.Gson().fromJson("{\"cacheSize\": 10000}", ModConfig.class);

        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertTrue(result.isConfigModified(), "Config should be marked modified");
        assertEquals(4096, config.getCacheSize(), "Cache size should be clamped to maximum 4096");
    }

    @Test
    @DisplayName("Unknown translation provider should fallback to 'google'")
    void testUnknownProviderFallback() {
        ModConfig config = new ModConfig();
        config.setPreferredTranslator("unknown_engine");

        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertTrue(result.isConfigModified());
        assertEquals("google", config.getPreferredTranslator());
        assertTrue(result.hasWarnings());
    }

    @Test
    @DisplayName("Language codes with whitespace or underscores should be normalized")
    void testLanguageCodeNormalization() {
        ModConfig config = new ModConfig();
        config.setDefaultSourceLang(" EN ");
        config.setDefaultTargetLang("ru_RU");

        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertEquals("en", config.getDefaultSourceLang());
        assertEquals("ru-ru", config.getDefaultTargetLang());
        assertTrue(result.isConfigModified());
    }

    @Test
    @DisplayName("DeepL without API key should warn about missing credentials")
    void testDeepLMissingCredentialsWarning() {
        ModConfig config = new ModConfig();
        config.setPreferredTranslator("deepl");
        config.setDeeplApiKey("");

        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("DeepL")));
    }

    @Test
    @DisplayName("Kagi without API key or session token should warn about missing credentials")
    void testKagiMissingCredentialsWarning() {
        ModConfig config = new ModConfig();
        config.setPreferredTranslator("kagi");
        config.setKagiApiKey("");
        config.setKagiSessionToken("");

        ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("Kagi")));
    }
}
