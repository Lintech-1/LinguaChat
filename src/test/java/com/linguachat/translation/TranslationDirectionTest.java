package com.linguachat.translation;

import com.linguachat.config.ModConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationDirectionTest {

    @Test
    @DisplayName("Incoming translation should always have 'auto' as source language")
    void testIncomingSourceLanguageIsAuto() {
        assertEquals("auto", TranslationDirection.SERVER_TO_CLIENT.getSourceLang());
    }

    @Test
    @DisplayName("Target language should equal normalized config default target language")
    void testTargetLanguageMatchesConfig() {
        ModConfig config = ModConfig.get();
        config.setDefaultTargetLang("ru");

        assertEquals("ru", TranslationDirection.SERVER_TO_CLIENT.getTargetLang());
        assertEquals("ru", TranslationDirection.CLIENT_TO_SERVER.getTargetLang());
    }

    @Test
    @DisplayName("shouldTranslate should respect isEnabled flag")
    void testShouldTranslateRespectsEnabled() {
        ModConfig config = ModConfig.get();
        config.setEnabled(false);

        assertFalse(TranslationDirection.SERVER_TO_CLIENT.shouldTranslate());
        assertFalse(TranslationDirection.CLIENT_TO_SERVER.shouldTranslate());

        config.setEnabled(true);
        config.setTranslateIncoming(true);
        config.setTranslateOutgoing(false);

        assertTrue(TranslationDirection.SERVER_TO_CLIENT.shouldTranslate());
        assertFalse(TranslationDirection.CLIENT_TO_SERVER.shouldTranslate());
    }
}
