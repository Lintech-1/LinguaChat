package com.linguachat.translation;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.io.Closeable;

import com.linguachat.LinguaChatMod;
import com.linguachat.config.ModConfig;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

public class TranslationManager implements Closeable {
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t";
    private final HttpClient httpClient;
    private Translator deeplTranslator;
    private ExecutorService translationExecutor;
    private final Map<String, CompletableFuture<String>> activeTranslations = new ConcurrentHashMap<>();
    
    // Алиасы для языков DeepL
    private static final Map<String, String> DEEPL_LANGUAGE_ALIASES = new HashMap<>();
    static {
        // Английский язык
        DEEPL_LANGUAGE_ALIASES.put("en", "en-US"); // Английский (американский)
        DEEPL_LANGUAGE_ALIASES.put("english", "en-US");
        DEEPL_LANGUAGE_ALIASES.put("en-GB", "en-GB"); // Английский (британский)
        DEEPL_LANGUAGE_ALIASES.put("en-US", "en-US"); // Явный алиас
        DEEPL_LANGUAGE_ALIASES.put("en_US", "en-US"); // Подчеркивание вместо дефиса
        DEEPL_LANGUAGE_ALIASES.put("en_GB", "en-GB");
        
        // Русский язык
        DEEPL_LANGUAGE_ALIASES.put("ru", "ru");
        DEEPL_LANGUAGE_ALIASES.put("russian", "ru");
        
        // Португальский
        DEEPL_LANGUAGE_ALIASES.put("pt", "pt-PT"); // Португальский (Португалия)
        DEEPL_LANGUAGE_ALIASES.put("portuguese", "pt-PT");
        DEEPL_LANGUAGE_ALIASES.put("pt-BR", "pt-BR"); // Португальский (Бразилия)
        DEEPL_LANGUAGE_ALIASES.put("pt_BR", "pt-BR");
        DEEPL_LANGUAGE_ALIASES.put("pt_PT", "pt-PT");
        
        // Немецкий
        DEEPL_LANGUAGE_ALIASES.put("de", "de");
        DEEPL_LANGUAGE_ALIASES.put("german", "de");
        
        // Французский
        DEEPL_LANGUAGE_ALIASES.put("fr", "fr");
        DEEPL_LANGUAGE_ALIASES.put("french", "fr");
        
        // Испанский
        DEEPL_LANGUAGE_ALIASES.put("es", "es");
        DEEPL_LANGUAGE_ALIASES.put("spanish", "es");
        
        // Итальянский
        DEEPL_LANGUAGE_ALIASES.put("it", "it");
        DEEPL_LANGUAGE_ALIASES.put("italian", "it");
        
        // Японский
        DEEPL_LANGUAGE_ALIASES.put("ja", "ja");
        DEEPL_LANGUAGE_ALIASES.put("japanese", "ja");
        
        // Китайский
        DEEPL_LANGUAGE_ALIASES.put("zh", "zh");
        DEEPL_LANGUAGE_ALIASES.put("chinese", "zh");
        
        // Новые языки
        
        // Болгарский
        DEEPL_LANGUAGE_ALIASES.put("bg", "bg");
        DEEPL_LANGUAGE_ALIASES.put("bulgarian", "bg");
        
        // Чешский
        DEEPL_LANGUAGE_ALIASES.put("cs", "cs");
        DEEPL_LANGUAGE_ALIASES.put("czech", "cs");
        
        // Датский
        DEEPL_LANGUAGE_ALIASES.put("da", "da");
        DEEPL_LANGUAGE_ALIASES.put("danish", "da");
        
        // Греческий
        DEEPL_LANGUAGE_ALIASES.put("el", "el");
        DEEPL_LANGUAGE_ALIASES.put("greek", "el");
        
        // Эстонский
        DEEPL_LANGUAGE_ALIASES.put("et", "et");
        DEEPL_LANGUAGE_ALIASES.put("estonian", "et");
        
        // Финский
        DEEPL_LANGUAGE_ALIASES.put("fi", "fi");
        DEEPL_LANGUAGE_ALIASES.put("finnish", "fi");
        
        // Венгерский
        DEEPL_LANGUAGE_ALIASES.put("hu", "hu");
        DEEPL_LANGUAGE_ALIASES.put("hungarian", "hu");
        
        // Индонезийский
        DEEPL_LANGUAGE_ALIASES.put("id", "id");
        DEEPL_LANGUAGE_ALIASES.put("indonesian", "id");
        
        // Корейский
        DEEPL_LANGUAGE_ALIASES.put("ko", "ko");
        DEEPL_LANGUAGE_ALIASES.put("korean", "ko");
        
        // Литовский
        DEEPL_LANGUAGE_ALIASES.put("lt", "lt");
        DEEPL_LANGUAGE_ALIASES.put("lithuanian", "lt");
        
        // Латышский
        DEEPL_LANGUAGE_ALIASES.put("lv", "lv");
        DEEPL_LANGUAGE_ALIASES.put("latvian", "lv");
        
        // Норвежский
        DEEPL_LANGUAGE_ALIASES.put("nb", "nb");
        DEEPL_LANGUAGE_ALIASES.put("norwegian", "nb");
        
        // Нидерландский
        DEEPL_LANGUAGE_ALIASES.put("nl", "nl");
        DEEPL_LANGUAGE_ALIASES.put("dutch", "nl");
        
        // Польский
        DEEPL_LANGUAGE_ALIASES.put("pl", "pl");
        DEEPL_LANGUAGE_ALIASES.put("polish", "pl");
        
        // Румынский
        DEEPL_LANGUAGE_ALIASES.put("ro", "ro");
        DEEPL_LANGUAGE_ALIASES.put("romanian", "ro");
        
        // Словацкий
        DEEPL_LANGUAGE_ALIASES.put("sk", "sk");
        DEEPL_LANGUAGE_ALIASES.put("slovak", "sk");
        
        // Словенский
        DEEPL_LANGUAGE_ALIASES.put("sl", "sl");
        DEEPL_LANGUAGE_ALIASES.put("slovenian", "sl");
        
        // Шведский
        DEEPL_LANGUAGE_ALIASES.put("sv", "sv");
        DEEPL_LANGUAGE_ALIASES.put("swedish", "sv");
        
        // Турецкий
        DEEPL_LANGUAGE_ALIASES.put("tr", "tr");
        DEEPL_LANGUAGE_ALIASES.put("turkish", "tr");
        
        // Украинский
        DEEPL_LANGUAGE_ALIASES.put("uk", "uk");
        DEEPL_LANGUAGE_ALIASES.put("ukrainian", "uk");
        
        // Особый случай - автоопределение
        DEEPL_LANGUAGE_ALIASES.put("auto", "auto");
    }

    public TranslationManager() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
        // Инициализируем DeepL только если есть API ключ
        String apiKey = ModConfig.get().getDeeplApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            this.deeplTranslator = new Translator(apiKey);
        }
        
        // Создаем выделенный пул потоков для переводов
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
            LinguaChatMod.LOGGER.info("Переинициализация пула потоков для перевода");
            initializeExecutor();
        }
    }

    public String resolveDeepLLanguage(String lang) {
        if (lang == null || lang.isEmpty()) return "auto"; // Безопасное значение по умолчанию
        
        // Нижний регистр для поиска
        String lowerLang = lang.toLowerCase().trim();
        
        // Логируем для отладки
        LinguaChatMod.LOGGER.info("Преобразование языка для DeepL: '" + lowerLang + "'");
        
        // Проверяем карту алиасов
        String resolvedLang = DEEPL_LANGUAGE_ALIASES.getOrDefault(lowerLang, null);
        
        if (resolvedLang == null) {
            // Если используется паттерн с подчеркиванием (xx_XX), преобразуем в формат с дефисом (xx-XX)
            if (lowerLang.contains("_")) {
                String withHyphen = lowerLang.replace('_', '-');
                resolvedLang = DEEPL_LANGUAGE_ALIASES.getOrDefault(withHyphen, null);
                
                // Если все еще не нашли, попробуем использовать только основную часть (xx)
                if (resolvedLang == null) {
                    String mainPart = lowerLang.split("_")[0];
                    resolvedLang = DEEPL_LANGUAGE_ALIASES.getOrDefault(mainPart, null);
                }
            }
            
            // Если все еще не нашли, попробуем использовать только основную часть кода с дефисом (xx-XX -> xx)
            if (resolvedLang == null && lowerLang.contains("-")) {
                String mainPart = lowerLang.split("-")[0];
                resolvedLang = DEEPL_LANGUAGE_ALIASES.getOrDefault(mainPart, null);
            }
            
            // Если все методы не сработали, возвращаем оригинальный код или "en-US" для безопасности
            if (resolvedLang == null) {
                // Если это выглядит как валидный код языка (2 или 5 символов), используем его
                if (lowerLang.length() == 2 || 
                    (lowerLang.length() == 5 && (lowerLang.charAt(2) == '-' || lowerLang.charAt(2) == '_'))) {
                    resolvedLang = lowerLang.replace('_', '-');
                } else {
                    resolvedLang = "en-US"; // Безопасное значение по умолчанию
                }
            }
        }
        
        LinguaChatMod.LOGGER.info("Преобразован язык для DeepL: '" + lowerLang + "' -> '" + resolvedLang + "'");
        return resolvedLang;
    }

    public Text translate(Text text, TranslationDirection direction) {
        if (!direction.shouldTranslate()) {
            return text;
        }
        
        // Проверяем кэш
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
            
            // Выбираем сервис перевода
            String translatedContent;
            String preferredTranslator = ModConfig.get().getPreferredTranslator();
            
            if ("deepl".equalsIgnoreCase(preferredTranslator) && deeplTranslator != null) {
                // Используем DeepL
                try {
                    // Заменяем "auto" на null для автоопределения языка
                    String deeplSourceLang = "auto".equals(sourceLang) ? null : sourceLang;
                    TextResult result = deeplTranslator.translateText(content, deeplSourceLang, targetLang);
                    translatedContent = result.getText();
                    LinguaChatMod.LOGGER.info("DeepL перевод: " + content + " -> " + translatedContent);
                } catch (Exception e) {
                    LinguaChatMod.LOGGER.error("Ошибка при использовании DeepL API: " + e.getMessage());
                    // Запасной вариант - Google Translate
                    translatedContent = translateWithGoogle(content, sourceLang, targetLang);
                }
            } else {
                // Используем Google Translate
                translatedContent = translateWithGoogle(content, sourceLang, targetLang);
            }
            
            // Создаем новый текст с переводом
            Text translatedText = createTranslatedText(text, translatedContent);
            
            // Сохраняем в кэш
            TranslationCache.put(text, translatedText, direction);
            
            return translatedText;
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error("Ошибка при переводе: " + e.getMessage());
            return text;
        }
    }
    
    public void translateAsync(Text text, TranslationDirection direction, Consumer<Text> callback) {
        if (!direction.shouldTranslate()) {
            callback.accept(text);
            return;
        }
        
        // Проверяем кэш
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
        
        // Проверяем, не выполняется ли уже перевод этого текста
        String cacheKey = content + "_" + direction.getSourceLang() + "_" + direction.getTargetLang();
        CompletableFuture<String> existingTranslation = activeTranslations.get(cacheKey);
        
        if (existingTranslation != null && !existingTranslation.isDone()) {
            // Уже есть активный перевод, добавляем обработчик
            existingTranslation.thenAcceptAsync(translatedContent -> {
                Text translatedText = createTranslatedText(text, translatedContent);
                TranslationCache.put(text, translatedText, direction);
                callback.accept(translatedText);
            }, MinecraftClient.getInstance()::execute);
            return;
        }
        
        // Создаем новую задачу перевода
        CompletableFuture<String> translationFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String sourceLang = direction.getSourceLang();
                String targetLang = direction.getTargetLang();
                
                // Выбираем сервис перевода
                String translatedContent;
                String preferredTranslator = ModConfig.get().getPreferredTranslator();
                
                if ("deepl".equalsIgnoreCase(preferredTranslator) && deeplTranslator != null) {
                    // Используем DeepL
                    try {
                        // Заменяем "auto" на null для автоопределения языка
                        String deeplSourceLang = "auto".equals(sourceLang) ? null : sourceLang;
                        TextResult result = deeplTranslator.translateText(content, deeplSourceLang, targetLang);
                        translatedContent = result.getText();
                        LinguaChatMod.LOGGER.info("DeepL перевод: " + content + " -> " + translatedContent);
                    } catch (Exception e) {
                        LinguaChatMod.LOGGER.error("Ошибка при использовании DeepL API: " + e.getMessage());
                        // Запасной вариант - Google Translate
                        translatedContent = translateWithGoogle(content, sourceLang, targetLang);
                    }
                } else {
                    // Используем Google Translate
                    translatedContent = translateWithGoogle(content, sourceLang, targetLang);
                }
                
                return translatedContent;
            } catch (Exception e) {
                LinguaChatMod.LOGGER.error("Ошибка при асинхронном переводе: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, translationExecutor);
        
        // Сохраняем задачу в активные переводы
        activeTranslations.put(cacheKey, translationFuture);
        
        // Добавляем обработчики
        translationFuture.thenAcceptAsync(translatedContent -> {
            Text translatedText = createTranslatedText(text, translatedContent);
            TranslationCache.put(text, translatedText, direction);
            callback.accept(translatedText);
            // Удаляем из активных переводов
            activeTranslations.remove(cacheKey);
        }, MinecraftClient.getInstance()::execute)
        .exceptionally(e -> {
            LinguaChatMod.LOGGER.error("Ошибка при обработке перевода: " + e.getMessage());
            callback.accept(text); // В случае ошибки возвращаем оригинальный текст
            activeTranslations.remove(cacheKey);
            return null;
        });
    }
    
    private Text createTranslatedText(Text original, String translatedContent) {
        // Создаем новый текст с переводом, сохраняя стиль оригинала
        Text translatedText = Text.literal(translatedContent).setStyle(original.getStyle());
        
        // Если включена опция показа оригинала при наведении, добавляем hover-эффект
        if (ModConfig.get().isShowOriginalOnHover()) {
            String originalContent = original.getString();
            
            // Добавляем hover-эффект только если перевод отличается от оригинала
            if (!originalContent.equals(translatedContent)) {
                Style newStyle = translatedText.getStyle().withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Оригинал: " + originalContent))
                );
                translatedText = translatedText.copy().setStyle(newStyle);
            }
        }
        
        return translatedText;
    }
    
    public Text translateChat(Text message, String playerName, TranslationDirection direction) {
        // Специальная обработка для сообщений чата
        // Можно добавить дополнительную логику, например, сохранение оригинала в MessageStore
        Text translatedText = translate(message, direction);
        
        // Сохраняем связь между оригинальным и переведенным сообщениями
        if (!message.getString().equals(translatedText.getString())) {
            MessageStore.linkMessages(playerName, message.getString(), translatedText.getString());
        }
        
        return translatedText;
    }
    
    private String translateWithGoogle(String text, String sourceLang, String targetLang) throws IOException, InterruptedException {
        // Если sourceLang = "auto", Google автоматически определит язык
        String sourceParam = "auto".equals(sourceLang) ? "auto" : sourceLang;
        
        // Формируем URL запроса
        String url = GOOGLE_TRANSLATE_URL + 
                     "&sl=" + URLEncoder.encode(sourceParam, StandardCharsets.UTF_8) + 
                     "&tl=" + URLEncoder.encode(targetLang, StandardCharsets.UTF_8) + 
                     "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        
        // Отправляем запрос
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Ошибка при запросе к Google Translate API: " + response.statusCode());
        }
        
        // Парсим JSON ответ
        try {
            // Ответ имеет формат [[["переведенный текст","исходный текст",null,null,1]],null,"en"]
            String responseBody = response.body();
            var jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
            
            // Извлекаем переведенный текст
            StringBuilder translatedText = new StringBuilder();
            var translationArray = jsonArray.get(0).getAsJsonArray();
            
            for (int i = 0; i < translationArray.size(); i++) {
                translatedText.append(translationArray.get(i).getAsJsonArray().get(0).getAsString());
            }
            
            LinguaChatMod.LOGGER.info("Google перевод: " + text + " -> " + translatedText);
            return translatedText.toString();
        } catch (JsonParseException e) {
            throw new IOException("Ошибка при парсинге ответа от Google Translate: " + e.getMessage());
        }
    }
    
    @Override
    public void close() {
        if (translationExecutor != null) {
            translationExecutor.shutdown();
        }
    }
} 