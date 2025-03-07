package com.linguachat.translation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.linguachat.LinguaChatMod;

/**
 * Класс для хранения и получения оригинальных текстов сообщений
 * Используется для отображения оригинального текста при наведении на переведенное сообщение
 */
public class MessageStore {
    // Статическая карта для хранения оригинальных текстов сообщений
    private static final Map<String, String> originalMessages = new ConcurrentHashMap<>();
    
    // Кэш для отслеживания недавно обработанных сообщений с временными метками
    private static final Map<String, Long> recentlyProcessedMessages = new ConcurrentHashMap<>();
    
    // Карта для отслеживания связанных сообщений (оригинал -> перевод и перевод -> оригинал)
    private static final Map<String, String> relatedMessages = new ConcurrentHashMap<>();
    
    // Время жизни записи в кэше недавно обработанных сообщений (в миллисекундах)
    private static final long CACHE_EXPIRY_TIME_MS = 5000; // 5 секунд
    
    /**
     * Сохраняет оригинальный текст сообщения
     * @param key ключ для сообщения (обычно формата "имя_игрока:текст_сообщения")
     * @param originalMessage оригинальный текст сообщения
     */
    public static void storeOriginalMessage(String key, String originalMessage) {
        originalMessages.put(key, originalMessage);
        
        // Логируем для отладки
        LinguaChatMod.LOGGER.info("MessageStore: сохранено [" + key + " -> " + originalMessage + "]");
    }
    
    /**
     * Получает оригинальный текст сообщения по ключу
     * @param key ключ сообщения
     * @return оригинальный текст или null, если не найден
     */
    public static String getOriginalMessage(String key) {
        return originalMessages.get(key);
    }
    
    /**
     * Очищает все хранилища сообщений
     */
    public static void clear() {
        LinguaChatMod.LOGGER.info("MessageStore: очистка всех хранилищ сообщений");
        originalMessages.clear();
        recentlyProcessedMessages.clear();
        relatedMessages.clear();
    }
    
    /**
     * Создает ключ для сообщения
     * @param playerName имя игрока
     * @param messageText текст сообщения
     * @return сформированный ключ
     */
    public static String createMessageKey(String playerName, String messageText) {
        return playerName + ":" + messageText;
    }
    
    /**
     * Связывает оригинальное сообщение с переведенным
     * @param playerName имя игрока
     * @param originalText оригинальный текст
     * @param translatedText переведенный текст
     */
    public static void linkMessages(String playerName, String originalText, String translatedText) {
        String originalKey = createMessageKey(playerName, originalText);
        String translatedKey = createMessageKey(playerName, translatedText);
        
        // Сохраняем двунаправленную связь
        relatedMessages.put(originalKey, translatedKey);
        relatedMessages.put(translatedKey, originalKey);
        
        // Также сохраняем исходные сообщения
        storeOriginalMessage(translatedKey, originalText);
    }
    
    /**
     * Проверяет, связано ли сообщение с другим
     * @param playerName имя игрока
     * @param messageText текст сообщения
     * @return true, если сообщение связано с другим
     */
    public static boolean isLinkedMessage(String playerName, String messageText) {
        String key = createMessageKey(playerName, messageText);
        return relatedMessages.containsKey(key);
    }
    
    /**
     * Помечает сообщение как недавно обработанное
     * @param playerName имя игрока
     * @param messageText текст сообщения
     */
    public static void markMessageAsProcessed(String playerName, String messageText) {
        String key = createMessageKey(playerName, messageText);
        recentlyProcessedMessages.put(key, System.currentTimeMillis());
        
        // Очистка устаревших записей (можно вызывать периодически для экономии памяти)
        cleanExpiredEntries();
    }
    
    /**
     * Проверяет, было ли сообщение недавно обработано
     * @param playerName имя игрока
     * @param messageText текст сообщения
     * @return true, если сообщение было недавно обработано
     */
    public static boolean wasMessageRecentlyProcessed(String playerName, String messageText) {
        String key = createMessageKey(playerName, messageText);
        Long timestamp = recentlyProcessedMessages.get(key);
        
        if (timestamp == null) {
            return false;
        }
        
        // Проверяем, не истек ли срок жизни записи
        long currentTime = System.currentTimeMillis();
        boolean isRecent = (currentTime - timestamp) <= CACHE_EXPIRY_TIME_MS;
        
        // Если срок истек, удаляем запись
        if (!isRecent) {
            recentlyProcessedMessages.remove(key);
        }
        
        return isRecent;
    }
    
    /**
     * Очищает устаревшие записи из кэша недавно обработанных сообщений
     */
    private static void cleanExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        // Используем копию ключей для безопасного удаления во время итерации
        Set<String> keys = recentlyProcessedMessages.keySet();
        keys.forEach(key -> {
            Long timestamp = recentlyProcessedMessages.get(key);
            if (timestamp != null && (currentTime - timestamp) > CACHE_EXPIRY_TIME_MS) {
                recentlyProcessedMessages.remove(key);
            }
        });
    }
} 