package com.linguachat.compat;

//? if >=1.19 {
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
*///?}

import java.util.HashSet;
import java.util.Set;

/**
 * Compatibility layer for internationalization (i18n) across different Minecraft versions.
 * 
 * Provides translation functionality using Minecraft's built-in i18n system.
 * Supports Minecraft 1.16.5 through 1.21.11.
 */
public class I18nCompat {
    
    // Track missing keys to avoid spam in logs
    private static final Set<String> warnedKeys = new HashSet<>();
    
    /**
     * Translates a key to the current game language.
     * Falls back to English if translation not found.
     * 
     * @param key Translation key (e.g., "linguachat.config.enabled")
     * @return Translated string
     */
    public static String translate(String key) {
        if (!I18n.hasTranslation(key)) {
            logMissingKey(key);
        }
        return I18n.translate(key);
    }
    
    /**
     * Translates a key with format arguments.
     * 
     * @param key Translation key
     * @param args Format arguments
     * @return Translated and formatted string
     */
    public static String translate(String key, Object... args) {
        if (!I18n.hasTranslation(key)) {
            logMissingKey(key);
        }
        
        try {
            String translated = I18n.translate(key, args);
            return translated;
        } catch (IllegalArgumentException e) {
            // Format error - return unformatted translation
            return I18n.translate(key);
        }
    }
    
    /**
     * Creates a translatable Text component.
     * Uses TextCompat internally for version compatibility.
     * 
     * @param key Translation key
     * @return MutableText with translation
     */
    public static MutableText translatableText(String key) {
        return TextCompat.translatable(key);
    }
    
    /**
     * Creates a translatable Text component with arguments.
     * 
     * @param key Translation key
     * @param args Format arguments
     * @return MutableText with translation
     */
    public static MutableText translatableText(String key, Object... args) {
        return TextCompat.translatable(key, args);
    }
    
    /**
     * Checks if a translation key exists in the current language.
     * 
     * @param key Translation key to check
     * @return true if key exists, false otherwise
     */
    public static boolean hasTranslation(String key) {
        return I18n.hasTranslation(key);
    }
    
    /**
     * Logs a warning for missing translation keys (once per key).
     * 
     * @param key The missing translation key
     */
    private static void logMissingKey(String key) {
        if (!warnedKeys.contains(key)) {
            warnedKeys.add(key);
            System.err.println("[LinguaChat] Missing translation key: " + key);
        }
    }
}
