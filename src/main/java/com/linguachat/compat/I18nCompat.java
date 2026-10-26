package com.linguachat.compat;

//? if >=26.1 {
/*import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
*///?} else if >=1.19 {
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
 * Supports Minecraft 1.16.5 through 26.3.
 */
public class I18nCompat {

    // Track missing keys to avoid spam in logs
    private static final Set<String> warnedKeys = new HashSet<>();

    /**
     * Translates a key to the current game language.
     * Falls back to English if translation not found.
     */
    //? if >=26.1 {
    //public static String translate(String key) {
    //    if (!hasTranslation(key)) {
    //        logMissingKey(key);
    //    }
    //    return I18n.get(key);
    //}
    //?} else {
    public static String translate(String key) {
        if (!I18n.hasTranslation(key)) {
            logMissingKey(key);
        }
        return I18n.translate(key);
    }
    //?}

    /**
     * Translates a key with format arguments.
     */
    //? if >=26.1 {
    //public static String translate(String key, Object... args) {
    //    if (!hasTranslation(key)) {
    //        logMissingKey(key);
    //    }
    //    return I18n.get(key, args);
    //}
    //?} else {
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
    //?}

    /**
     * Creates a translatable Text component.
     * Uses TextCompat internally for version compatibility.
     */
    //? if >=26.1 {
    //public static MutableComponent translatableText(String key) {
    //    return TextCompat.translatable(key);
    //}
    //
    //public static MutableComponent translatableText(String key, Object... args) {
    //    return TextCompat.translatable(key, args);
    //}
    //?} else {
    public static MutableText translatableText(String key) {
        return TextCompat.translatable(key);
    }

    public static MutableText translatableText(String key, Object... args) {
        return TextCompat.translatable(key, args);
    }
    //?}

    /**
     * Checks if a translation key exists in the current language.
     */
    //? if >=26.1 {
    //public static boolean hasTranslation(String key) {
    //    return Language.getInstance().has(key);
    //}
    //?} else {
    public static boolean hasTranslation(String key) {
        return I18n.hasTranslation(key);
    }
    //?}

    /**
     * Logs a warning for missing translation keys (once per key).
     */
    private static void logMissingKey(String key) {
        if (!warnedKeys.contains(key)) {
            warnedKeys.add(key);
            System.err.println("[LinguaChat] Missing translation key: " + key);
        }
    }
}
