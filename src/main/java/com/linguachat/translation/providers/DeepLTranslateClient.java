package com.linguachat.translation.providers;

import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.TranslationLogger;

/**
 * DeepL translation provider (requires API key)
 */
public class DeepLTranslateClient implements TranslationProvider {
    private final Translator deeplTranslator;

    public DeepLTranslateClient() {
        String apiKey = ModConfig.get().getDeeplApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            this.deeplTranslator = new Translator(apiKey);
        } else {
            this.deeplTranslator = null;
        }
    }

    /**
     * DeepL requires specific variants (en-US not en, pt-BR not pt)
     */
    private String normalizeDeepLLanguageCode(String lang) {
        if (lang == null || lang.isEmpty() || "auto".equals(lang)) {
            return lang;
        }

        String lowerLang = lang.toLowerCase();

        if ("en".equals(lowerLang)) {
            return "en-US";
        } else if ("pt".equals(lowerLang)) {
            return "pt-BR";
        } else {
            return lang;
        }
    }
    
    @Override
    public String translate(String text, String sourceLang, String targetLang) throws TranslationException {
        if (deeplTranslator == null) {
            throw new TranslationException(
                TranslationException.ErrorType.AUTHENTICATION_ERROR,
                "DeepL API key not configured"
            );
        }
        
        String normalizedSourceLang = normalizeDeepLLanguageCode(sourceLang);
        String normalizedTargetLang = normalizeDeepLLanguageCode(targetLang);
        
        TranslationLogger.logTranslationRequest(getProviderName(), text, normalizedSourceLang, normalizedTargetLang);
        
        try {
            String deeplSourceLang = "auto".equals(normalizedSourceLang) ? null : normalizedSourceLang;
            
            TextResult result = deeplTranslator.translateText(text, deeplSourceLang, normalizedTargetLang);
            String translatedText = result.getText();
            
            TranslationLogger.logTranslationResponse(getProviderName(), text, translatedText);
            return translatedText;
            
        } catch (com.deepl.api.DeepLException e) {
            // Map DeepL errors to our error types
            String message = e.getMessage().toLowerCase();
            TranslationException.ErrorType errorType;
            
            if (message.contains("authorization") || message.contains("forbidden")) {
                errorType = TranslationException.ErrorType.AUTHENTICATION_ERROR;
            } else if (message.contains("quota") || message.contains("limit")) {
                errorType = TranslationException.ErrorType.RATE_LIMIT_ERROR;
            } else if (message.contains("timeout")) {
                errorType = TranslationException.ErrorType.TIMEOUT_ERROR;
            } else {
                errorType = TranslationException.ErrorType.NETWORK_ERROR;
            }
            
            throw new TranslationException(errorType, "DeepL error: " + e.getMessage(), e);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TranslationException(
                TranslationException.ErrorType.TIMEOUT_ERROR,
                "Request interrupted",
                e
            );
        }
    }
    
    @Override
    public boolean isAvailable() {
        return deeplTranslator != null;
    }
    
    @Override
    public String getProviderName() {
        return "DeepL";
    }
    
    @Override
    public boolean validateLanguageCode(String languageCode) {
        // DeepL will error if unsupported, so just accept anything
        return languageCode != null && !languageCode.isEmpty();
    }
}
