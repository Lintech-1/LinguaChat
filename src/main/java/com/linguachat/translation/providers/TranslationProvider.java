package com.linguachat.translation.providers;

/**
 * Interface for translation providers
 */
public interface TranslationProvider {
    /**
     * Translates text from one language to another
     * 
     * @param text Text to translate
     * @param sourceLang Source language (can be "auto")
     * @param targetLang Target language
     * @return Translated text
     * @throws TranslationException On translation error
     */
    String translate(String text, String sourceLang, String targetLang) throws TranslationException;
    
    /**
     * Checks if the provider is available
     * 
     * @return true if provider is available (has API key, not disabled, etc.)
     */
    boolean isAvailable();
    
    /**
     * Returns the provider name
     * 
     * @return Provider name (e.g., "Google", "DeepL", "Kagi")
     */
    String getProviderName();
    
    /**
     * Validates language code for this provider
     * 
     * @param languageCode Language code to validate
     * @return true if code is valid for this provider
     */
    boolean validateLanguageCode(String languageCode);
}
