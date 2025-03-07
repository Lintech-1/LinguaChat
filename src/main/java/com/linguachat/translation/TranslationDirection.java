package com.linguachat.translation;

import com.linguachat.config.ModConfig;

public enum TranslationDirection {
    CLIENT_TO_SERVER,  // Исходящие сообщения (от клиента к серверу)
    SERVER_TO_CLIENT;  // Входящие сообщения (от сервера к клиенту)

    private static final TranslationManager translationManager = new TranslationManager();

    public String getSourceLang() {
        String lang = switch (this) {
            case CLIENT_TO_SERVER -> ModConfig.get().getDefaultSourceLang();
            case SERVER_TO_CLIENT -> "auto"; // Автоопределение языка для входящих сообщений
        };
        return translationManager.resolveDeepLLanguage(lang);
    }

    public String getTargetLang() {
        String lang = switch (this) {
            case CLIENT_TO_SERVER -> "en"; // Переводим на английский для сервера
            case SERVER_TO_CLIENT -> ModConfig.get().getDefaultTargetLang();
        };
        return translationManager.resolveDeepLLanguage(lang);
    }

    public boolean shouldTranslate() {
        if (!ModConfig.get().isEnabled()) return false;
        
        return switch (this) {
            case CLIENT_TO_SERVER -> ModConfig.get().isTranslateOutgoing();
            case SERVER_TO_CLIENT -> ModConfig.get().isTranslateIncoming();
        };
    }
} 