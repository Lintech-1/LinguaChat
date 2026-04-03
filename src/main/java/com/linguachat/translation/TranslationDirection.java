package com.linguachat.translation;

import com.linguachat.config.ModConfig;

public enum TranslationDirection {
    CLIENT_TO_SERVER,  // Outgoing messages (client sends to server, translated according to defaultTargetLang)
    SERVER_TO_CLIENT;  // Incoming messages (server sends to client, translated according to defaultTargetLang)

    private static final TranslationManager translationManager = new TranslationManager();

    public String getSourceLang() {
        String lang;
        if (this == CLIENT_TO_SERVER) {
            lang = ModConfig.get().getDefaultSourceLang();
        } else { // SERVER_TO_CLIENT
            lang = "auto"; // Auto-detect language for incoming messages
        }
        return translationManager.normalizeLanguageCode(lang);
    }

    public String getTargetLang() {
        String lang;
        if (this == CLIENT_TO_SERVER) {
            // For outgoing messages, use defaultTargetLang
            // This allows the user to send messages in the desired language (e.g., the server's language)
            lang = ModConfig.get().getDefaultTargetLang();
        } else { // SERVER_TO_CLIENT
            // For incoming messages we want to see them in our language
            lang = ModConfig.get().getDefaultTargetLang();
        }
        return translationManager.normalizeLanguageCode(lang);
    }

    public boolean shouldTranslate() {
        if (!ModConfig.get().isEnabled()) return false;
        
        if (this == CLIENT_TO_SERVER) {
            return ModConfig.get().isTranslateOutgoing();
        } else { // SERVER_TO_CLIENT
            return ModConfig.get().isTranslateIncoming();
        }
    }
} 