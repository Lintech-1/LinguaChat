package com.linguachat.translation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageNormalizationTest {

    @ParameterizedTest(name = "normalizeLanguageCode(\"{0}\") -> \"{1}\"")
    @CsvSource({
        "cz, cs",
        "jp, ja",
        "kr, ko",
        "gr, el",
        "ua, uk",
        "cn, zh",
        "tw, zh-TW",
        "en, en",
        "ru, ru",
        "de, de",
        "fr, fr",
        "es, es"
    })
    @DisplayName("Should map common incorrect codes and preserve standard ISO 639-1 codes")
    void testStandardAliases(String input, String expected) {
        assertEquals(expected, TranslationManager.normalizeLanguageCode(input));
    }

    @ParameterizedTest(name = "normalizeLanguageCode(\"{0}\") -> \"{1}\"")
    @CsvSource({
        "' RU ', ru",
        "'EN', en",
        "'  cz  ', cs",
        "'JP', ja",
        "'Ua', uk"
    })
    @DisplayName("Should trim whitespace and convert input to lowercase")
    void testCasingAndWhitespace(String input, String expected) {
        assertEquals(expected, TranslationManager.normalizeLanguageCode(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should return 'auto' for null or empty language codes")
    void testNullAndEmpty(String input) {
        assertEquals("auto", TranslationManager.normalizeLanguageCode(input));
    }
}
