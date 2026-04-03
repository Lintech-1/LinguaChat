package com.linguachat.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.io.Closeable;
//? if >=1.18 {
import java.net.http.HttpClient;
import java.time.Duration;
//?}

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.I18nCompat;
import com.linguachat.compat.TextCompat;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.providers.*;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.client.MinecraftClient;

public class TranslationManager implements Closeable {
    //? if >=1.18 {
    private final HttpClient httpClient;
    //?}
    private ExecutorService translationExecutor;
    private final Map<String, CompletableFuture<String>> activeTranslations = new ConcurrentHashMap<>();
    
    // provider instances - Google always available, others need API keys
    private final GoogleTranslateClient googleClient;
    private final DeepLTranslateClient deeplClient;
    private final KagiTranslateClient kagiClient;

    public TranslationManager() {
        //? if >=1.18 {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
        // Java 11+ uses HttpClient, providers share it
        this.googleClient = new GoogleTranslateClient(httpClient);
        this.deeplClient = new DeepLTranslateClient();
        this.kagiClient = new KagiTranslateClient(httpClient);
        //?} else {
        /*// Java 8: providers don't require HttpClient
        this.googleClient = new GoogleTranslateClient(null);
        this.deeplClient = new DeepLTranslateClient();
        this.kagiClient = new KagiTranslateClient(null);
        *///?}
        
        // 2 threads for async translation - keeps UI responsive
        initializeExecutor();
    }

    private void initializeExecutor() {
        this.translationExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r, "LinguaChat-Translation-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    public void ensureExecutorRunning() {
        if (translationExecutor == null || translationExecutor.isShutdown() || translationExecutor.isTerminated()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.reinit_thread_pool"));
            initializeExecutor();
        }
    }

    /**
     * Builds provider chain based on settings
     */
    private List<TranslationProvider> buildProviderChain() {
        List<TranslationProvider> chain = new ArrayList<>();
        String preferred = ModConfig.get().getPreferredTranslator().toLowerCase();
        
        // user's preferred provider gets first shot at translation
        TranslationProvider preferredProvider;
        if ("kagi".equals(preferred)) {
            preferredProvider = kagiClient;
        } else if ("deepl".equals(preferred)) {
            preferredProvider = deeplClient;
        } else {
            preferredProvider = googleClient;
        }
        
        if (preferredProvider.isAvailable()) {
            chain.add(preferredProvider);
        }
        
        // chain other providers for fallback if preferred fails
        if (preferredProvider != kagiClient && kagiClient.isAvailable()) {
            chain.add(kagiClient);
        }
        if (preferredProvider != deeplClient && deeplClient.isAvailable()) {
            chain.add(deeplClient);
        }
        if (preferredProvider != googleClient) {
            chain.add(googleClient); // Google is always available as last fallback
        }
        
        return chain;
    }

    /**
     * Attempts to translate text using provider chain with fallback
     */
    private String translateWithFallback(String text, String sourceLang, String targetLang) {
        List<TranslationProvider> providers = buildProviderChain();
        
        String translatedResult = null;
        for (int i = 0; i < providers.size(); i++) {
            TranslationProvider provider = providers.get(i);
            try {
                translatedResult = provider.translate(text, sourceLang, targetLang);
                
                // workaround: if auto-detect returns same text (wrong lang guess), retry with English
                if ("auto".equals(sourceLang) && 
                    translatedResult.equals(text) && 
                    !"en".equals(targetLang) && 
                    text.trim().length() > 0) {
                    
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.language_match_detected", targetLang));
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.retry_english"));
                        
                        try {
                            String englishTranslation = provider.translate(text, sourceLang, "en");
                            
                            // make sure English fallback actually translated something
                            if (!englishTranslation.equals(text) && !englishTranslation.equals(translatedResult)) {
                                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.fallback_english_success", text, englishTranslation));
                                return englishTranslation;
                            } else {
                                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.english_no_change"));
                            }
                        } catch (TranslationException retryException) {
                            LinguaChatMod.LOGGER.warn(I18nCompat.translate("linguachat.log.retry_english_error", retryException.getMessage()));
                        }
                }
                
                return translatedResult;
            } catch (TranslationException e) {
                TranslationLogger.logProviderError(
                    provider.getProviderName(),
                    e.getErrorType().name(),
                    e.getMessage()
                );
                
                // provider failed, try next in chain
                if (i < providers.size() - 1) {
                    TranslationProvider nextProvider = providers.get(i + 1);
                    TranslationLogger.logFallback(
                        provider.getProviderName(),
                        nextProvider.getProviderName(),
                        e.getMessage()
                    );
                }
            }
        }
        
        // If all providers failed, return original text
        LinguaChatMod.LOGGER.warn(I18nCompat.translate("linguachat.log.all_providers_failed"));
        return text;
    }

    public String normalizeLanguageCode(String lang) {
        if (lang == null || lang.isEmpty()) return "auto";
        
        String normalized = lang.toLowerCase().trim();
        
        // Map common incorrect codes to correct ISO 639-1 codes
        switch (normalized) {
            case "cz": return "cs";  // Czech
            case "jp": return "ja";  // Japanese
            case "kr": return "ko";  // Korean
            case "gr": return "el";  // Greek
            case "ua": return "uk";  // Ukrainian
            case "cn": return "zh";  // Chinese (Simplified)
            case "tw": return "zh-TW";  // Chinese (Traditional)
            default: return normalized;
        }
    }

    // Deprecated method for backward compatibility
    @Deprecated
    public String resolveDeepLLanguage(String lang) {
        return normalizeLanguageCode(lang);
    }

    public Text translate(Text text, TranslationDirection direction) {
        if (!direction.shouldTranslate()) {
            return text;
        }
        
        // cache hit = instant return, no API call
        Text cachedTranslation = TranslationCache.get(text, direction);
        if (cachedTranslation != null) {
            return cachedTranslation;
        }
        
        String content = text.getString();
        if (content.isEmpty()) {
            return text;
        }
        
        try {
            String sourceLang = direction.getSourceLang();
            String targetLang = direction.getTargetLang();
            
            // try providers in order until one succeeds
            String translatedContent = translateWithFallback(content, sourceLang, targetLang);
            
            // wrap translated string in Text with hover effect
            Text translatedText = createTranslatedText(text, translatedContent);
            
            TranslationCache.put(text, translatedText, direction);
            
            return translatedText;
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error(I18nCompat.translate("linguachat.log.translation_error", e.getMessage()));
            return text;
        }
    }
    
    public void translateAsync(Text text, TranslationDirection direction, Consumer<Text> callback) {
        if (!direction.shouldTranslate()) {
            callback.accept(text);
            return;
        }

        // cache hit = instant return, no API call
        Text cachedTranslation = TranslationCache.get(text, direction);
        if (cachedTranslation != null) {
            callback.accept(cachedTranslation);
            return;
        }
        
        String content = text.getString();
        if (content.isEmpty()) {
            callback.accept(text);
            return;
        }
        
        // dedupe: don't translate same message twice if already pending
        String cacheKey = content + "_" + direction.getSourceLang() + "_" + direction.getTargetLang();
        CompletableFuture<String> existingTranslation = activeTranslations.get(cacheKey);
        
        if (existingTranslation != null && !existingTranslation.isDone()) {
            // piggyback on existing translation task
            existingTranslation.thenAcceptAsync(translatedContent -> {
                Text translatedText = createTranslatedText(text, translatedContent);
                TranslationCache.put(text, translatedText, direction);
                callback.accept(translatedText);
            }, MinecraftClient.getInstance()::execute);
            return;
        }
        
        CompletableFuture<String> translationFuture = CompletableFuture.supplyAsync(() -> {
            String sourceLang = direction.getSourceLang();
            String targetLang = direction.getTargetLang();
            
            // try providers in order until one succeeds
            return translateWithFallback(content, sourceLang, targetLang);
        }, translationExecutor);
        
        activeTranslations.put(cacheKey, translationFuture);
        
        translationFuture.thenAcceptAsync(translatedContent -> {
            Text translatedText = createTranslatedText(text, translatedContent);
            TranslationCache.put(text, translatedText, direction);
            callback.accept(translatedText);
            activeTranslations.remove(cacheKey);
        }, MinecraftClient.getInstance()::execute)
        .exceptionally(e -> {
            LinguaChatMod.LOGGER.error(I18nCompat.translate("linguachat.log.translation_processing_error", e.getMessage()));
            callback.accept(text);
            activeTranslations.remove(cacheKey);
            return null;
        });
    }
    
    private Text createTranslatedText(Text original, String translatedContent) {
        MutableText translatedText = TextCompat.literal(translatedContent);
        
        // hover shows original if user enabled it in config
        if (ModConfig.get().isShowOriginalOnHover()) {
            String originalContent = original.getString();
            
            // no point showing hover if text didn't change
            if (!originalContent.equals(translatedContent)) {
                // compat layer handles hover across MC versions
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalContent))
                );
                
                return translatedText.styled(style -> style.withHoverEvent(hoverEvent));
            }
        }
        
        // keep original formatting (colors, bold, etc)
        return translatedText.setStyle(original.getStyle());
    }
    
    public Text translateChat(Text message, String playerName, TranslationDirection direction) {
        // chat messages get stored for hover effect later
        Text translatedText = translate(message, direction);
        
        if (!message.getString().equals(translatedText.getString())) {
            MessageStore.linkMessages(playerName, message.getString(), translatedText.getString());
        }
        
        return translatedText;
    }
    
    @Override
    public void close() {
        if (translationExecutor != null) {
            translationExecutor.shutdown();
        }
    }
} 