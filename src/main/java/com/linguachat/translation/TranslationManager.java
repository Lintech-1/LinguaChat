package com.linguachat.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

//? if >=26.1 {
/*// Unobfuscated Minecraft: Mojang naming
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.Minecraft;
*///?} else {
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.client.MinecraftClient;
//?}

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
    private String translateWithFallback(String text, String sourceLang, String targetLang) throws TranslationException {
        List<TranslationProvider> providers = buildProviderChain();
        
        for (int i = 0; i < providers.size(); i++) {
            TranslationProvider provider = providers.get(i);
            try {
                return provider.translate(text, sourceLang, targetLang);
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
        
        // All providers failed - caller decides how to degrade
        LinguaChatMod.LOGGER.warn(I18nCompat.translate("linguachat.log.all_providers_failed"));
        throw new TranslationException(
            TranslationException.ErrorType.NETWORK_ERROR,
            "All translation providers failed"
        );
    }

    public static String normalizeLanguageCode(String lang) {
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

    //? if >=26.1 {
    /*public Component translate(Component text, TranslationDirection direction) {
        if (!direction.shouldTranslate()) {
            return text;
        }
        
        // cache hit = instant return, no API call
        Component cachedTranslation = TranslationCache.get(text, direction);
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
            Component translatedText = createTranslatedText(text, translatedContent);
            
            TranslationCache.put(text, translatedText, direction);
            
            return translatedText;
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error(I18nCompat.translate("linguachat.log.translation_error", e.getMessage()));
            return text;
        }
    }
    
    public void translateAsync(Component text, TranslationDirection direction, Consumer<Component> callback) {
        if (!direction.shouldTranslate()) {
            callback.accept(text);
            return;
        }

        // cache hit = instant return, no API call
        Component cachedTranslation = TranslationCache.get(text, direction);
        if (cachedTranslation != null) {
            callback.accept(cachedTranslation);
            return;
        }
        
        String content = text.getString();
        if (content.isEmpty()) {
            callback.accept(text);
            return;
        }
        
        String sourceLang = direction.getSourceLang();
        String targetLang = direction.getTargetLang();
        String cacheKey = content + "_" + sourceLang + "_" + targetLang;
        CompletableFuture<String> translationFuture = activeTranslations.compute(cacheKey, (key, existing) -> {
            if (existing != null && !existing.isDone()) {
                return existing;
            }

            return CompletableFuture.supplyAsync(() -> {
                try {
                    return translateWithFallback(content, sourceLang, targetLang);
                } catch (TranslationException e) {
                    throw new CompletionException(e);
                }
            }, translationExecutor);
        });
        
        translationFuture.thenAcceptAsync(translatedContent -> {
            Component translatedText = createTranslatedText(text, translatedContent);
            TranslationCache.put(text, translatedText, direction);
            callback.accept(translatedText);
            activeTranslations.remove(cacheKey, translationFuture);
        }, Minecraft.getInstance()::execute)
        .exceptionally(e -> {
            // unwrap to the root cause so the user sees the real error, not CompletionException
            Throwable unwrapped = e;
            while (unwrapped.getCause() != null && unwrapped.getCause() != unwrapped) {
                unwrapped = unwrapped.getCause();
            }
            final Throwable cause = unwrapped;
            LinguaChatMod.LOGGER.error(I18nCompat.translate("linguachat.log.translation_processing_error", cause.getMessage()));
            // failure path runs on executor thread - callback may touch chat HUD, keep it on main
            Minecraft.getInstance().execute(() -> {
                showTranslationError(direction, errorReason(cause));
                // outgoing: callback gets null = do NOT send the untranslated original;
                // incoming: fall back to the original so the message isn't lost
                callback.accept(direction == TranslationDirection.CLIENT_TO_SERVER ? null : text);
            });
            activeTranslations.remove(cacheKey, translationFuture);
            return null;
        });
    }

    // Client-only: error is added directly to the chat HUD, so it can't reach other players
    private static volatile long lastErrorShownAt = 0;

    // player-facing reason: translated error type; the raw message stays in the log only
    private static String errorReason(Throwable cause) {
        String key = "linguachat.error.type.NETWORK_ERROR";
        if (cause instanceof TranslationException) {
            key = "linguachat.error.type." + ((TranslationException) cause).getErrorType().name();
        }
        return I18nCompat.translate(key);
    }

    private void showTranslationError(TranslationDirection direction, String reason) {
        // outgoing errors are user-initiated - always show; incoming throttled against chat spam
        boolean outgoing = direction == TranslationDirection.CLIENT_TO_SERVER;
        long now = System.currentTimeMillis();
        if (!outgoing && now - lastErrorShownAt < 10_000) {
            return;
        }
        lastErrorShownAt = now;
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            // direct add - sendMessage routes through ChatListener where our own mixin would translate this
            String key = outgoing ? "linguachat.error.outgoing_failed" : "linguachat.error.translation_failed";
            client.gui.hud.getChat().addClientSystemMessage(
                TextCompat.literal(I18nCompat.translate(key, reason))
            );
        }
    }
    
    private Component createTranslatedText(Component original, String translatedContent) {
        MutableComponent translatedText = TextCompat.literal(translatedContent);
        
        // hover shows original if user enabled it in config
        if (ModConfig.get().isShowOriginalOnHover()) {
            String originalContent = original.getString();
            
            // no point showing hover if text didn't change
            if (!originalContent.equals(translatedContent)) {
                // compat layer handles hover across MC versions
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalContent))
                );
                
                // keep original root style (click events, color) and add hover on top
                return translatedText.withStyle(style -> original.getStyle().withHoverEvent(hoverEvent));
            }
        }
        
        // keep original formatting (colors, bold, etc)
        return translatedText.setStyle(original.getStyle());
    }
    
    public Component translateChat(Component message, String playerName, TranslationDirection direction) {
        // chat messages get stored for hover effect later
        Component translatedText = translate(message, direction);
        
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
*///?} else {
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

        String sourceLang = direction.getSourceLang();
        String targetLang = direction.getTargetLang();
        String cacheKey = content + "_" + sourceLang + "_" + targetLang;
        CompletableFuture<String> translationFuture = activeTranslations.compute(cacheKey, (key, existing) -> {
            if (existing != null && !existing.isDone()) {
                return existing;
            }

            return CompletableFuture.supplyAsync(() -> {
                try {
                    return translateWithFallback(content, sourceLang, targetLang);
                } catch (TranslationException e) {
                    throw new CompletionException(e);
                }
            }, translationExecutor);
        });

        translationFuture.thenAcceptAsync(translatedContent -> {
            Text translatedText = createTranslatedText(text, translatedContent);
            TranslationCache.put(text, translatedText, direction);
            callback.accept(translatedText);
            activeTranslations.remove(cacheKey, translationFuture);
        }, MinecraftClient.getInstance()::execute)
        .exceptionally(e -> {
            // unwrap to the root cause so the user sees the real error, not CompletionException
            Throwable unwrapped = e;
            while (unwrapped.getCause() != null && unwrapped.getCause() != unwrapped) {
                unwrapped = unwrapped.getCause();
            }
            final Throwable cause = unwrapped;
            LinguaChatMod.LOGGER.error(I18nCompat.translate("linguachat.log.translation_processing_error", cause.getMessage()));
            // failure path runs on executor thread - callback may touch chat HUD, keep it on main
            MinecraftClient.getInstance().execute(() -> {
                showTranslationError(direction, errorReason(cause));
                // outgoing: callback gets null = do NOT send the untranslated original;
                // incoming: fall back to the original so the message isn't lost
                callback.accept(direction == TranslationDirection.CLIENT_TO_SERVER ? null : text);
            });
            activeTranslations.remove(cacheKey, translationFuture);
            return null;
        });
    }

    // Client-only: error is added directly to the chat HUD, so it can't reach other players
    private static volatile long lastErrorShownAt = 0;

    // player-facing reason: translated error type; the raw message stays in the log only
    private static String errorReason(Throwable cause) {
        String key = "linguachat.error.type.NETWORK_ERROR";
        if (cause instanceof TranslationException) {
            key = "linguachat.error.type." + ((TranslationException) cause).getErrorType().name();
        }
        return I18nCompat.translate(key);
    }

    private void showTranslationError(TranslationDirection direction, String reason) {
        // outgoing errors are user-initiated - always show; incoming throttled against chat spam
        boolean outgoing = direction == TranslationDirection.CLIENT_TO_SERVER;
        long now = System.currentTimeMillis();
        if (!outgoing && now - lastErrorShownAt < 10_000) {
            return;
        }
        lastErrorShownAt = now;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // direct add - sendMessage routes through MessageHandler where our own mixin would translate this
            String key = outgoing ? "linguachat.error.outgoing_failed" : "linguachat.error.translation_failed";
            client.inGameHud.getChatHud().addMessage(
                TextCompat.literal(I18nCompat.translate(key, reason))
            );
        }
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

                // keep original root style (click events, color) and add hover on top
                return translatedText.styled(style -> original.getStyle().withHoverEvent(hoverEvent));
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
//?}
