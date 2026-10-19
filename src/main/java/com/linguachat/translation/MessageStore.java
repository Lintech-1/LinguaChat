package com.linguachat.translation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.I18nCompat;
import com.linguachat.config.ModConfig;

/**
 * Class for storing and retrieving original message texts
 * Used to display original text when hovering over translated message
 */
public class MessageStore {
    private static final Map<String, String> originalMessages = new ConcurrentHashMap<>();
    private static final Map<String, Long> originalMessageTimes = new ConcurrentHashMap<>();
    
    // Cache for tracking recently processed messages with timestamps
    private static final Map<String, Long> recentlyProcessedMessages = new ConcurrentHashMap<>();
    
    // Map for tracking related messages (original -> translation and translation -> original)
    private static final Map<String, String> relatedMessages = new ConcurrentHashMap<>();
    private static final Map<String, Long> relatedMessageTimes = new ConcurrentHashMap<>();
    
    // Entry lifetime in recently processed messages cache (in milliseconds)
    private static final long CACHE_EXPIRY_TIME_MS = 5000; // 5 seconds
    private static final long MESSAGE_STORE_EXPIRY_TIME_MS = 10 * 60 * 1000; // 10 minutes
    
    /**
     * Stores original message text
     * @param key message key (usually in format "player_name:message_text")
     * @param originalMessage original message text
     */
    public static void storeOriginalMessage(String key, String originalMessage) {
        cleanExpiredEntries();
        originalMessages.put(key, originalMessage);
        originalMessageTimes.put(key, System.currentTimeMillis());
        
        debugLog(I18nCompat.translate("linguachat.log.debug.message_stored", key, originalMessage));
    }
    
    /**
     * Gets original message text by key
     * @param key message key
     * @return original text or null if not found
     */
    public static String getOriginalMessage(String key) {
        cleanExpiredEntries();
        if (isExpired(originalMessageTimes.get(key), MESSAGE_STORE_EXPIRY_TIME_MS)) {
            originalMessages.remove(key);
            originalMessageTimes.remove(key);
            return null;
        }
        return originalMessages.get(key);
    }
    
    /**
     * Clears all message stores
     */
    public static void clear() {
        debugLog(I18nCompat.translate("linguachat.log.message_store_clear"));
        originalMessages.clear();
        originalMessageTimes.clear();
        recentlyProcessedMessages.clear();
        relatedMessages.clear();
        relatedMessageTimes.clear();
    }
    
    /**
     * Creates message key
     * @param playerName player name
     * @param messageText message text
     * @return formatted key
     */
    public static String createMessageKey(String playerName, String messageText) {
        return playerName + ":" + messageText;
    }
    
    /**
     * Links original message with translated one
     * @param playerName player name
     * @param originalText original text
     * @param translatedText translated text
     */
    public static void linkMessages(String playerName, String originalText, String translatedText) {
        cleanExpiredEntries();
        String originalKey = createMessageKey(playerName, originalText);
        String translatedKey = createMessageKey(playerName, translatedText);
        long now = System.currentTimeMillis();
        
        relatedMessages.put(originalKey, translatedKey);
        relatedMessages.put(translatedKey, originalKey);
        relatedMessageTimes.put(originalKey, now);
        relatedMessageTimes.put(translatedKey, now);
        
        storeOriginalMessage(translatedKey, originalText);
    }
    
    /**
     * Checks if message is linked to another
     * @param playerName player name
     * @param messageText message text
     * @return true if message is linked to another
     */
    public static boolean isLinkedMessage(String playerName, String messageText) {
        cleanExpiredEntries();
        String key = createMessageKey(playerName, messageText);
        if (isExpired(relatedMessageTimes.get(key), MESSAGE_STORE_EXPIRY_TIME_MS)) {
            relatedMessages.remove(key);
            relatedMessageTimes.remove(key);
            return false;
        }
        return relatedMessages.containsKey(key);
    }
    
    /**
     * Marks message as recently processed
     * @param playerName player name
     * @param messageText message text
     */
    public static void markMessageAsProcessed(String playerName, String messageText) {
        String key = createMessageKey(playerName, messageText);
        recentlyProcessedMessages.put(key, System.currentTimeMillis());
        
        // Clean expired entries (can be called periodically to save memory)
        cleanExpiredEntries();
    }
    
    /**
     * Removes the processed mark so a failed send can be retried normally
     */
    public static void unmarkMessageAsProcessed(String playerName, String messageText) {
        recentlyProcessedMessages.remove(createMessageKey(playerName, messageText));
    }

    /**
     * Checks if message was recently processed
     * @param playerName player name
     * @param messageText message text
     * @return true if message was recently processed
     */
    public static boolean wasMessageRecentlyProcessed(String playerName, String messageText) {
        String key = createMessageKey(playerName, messageText);
        Long timestamp = recentlyProcessedMessages.get(key);
        
        if (timestamp == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        boolean isRecent = (currentTime - timestamp) <= CACHE_EXPIRY_TIME_MS;
        
        if (!isRecent) {
            recentlyProcessedMessages.remove(key);
        }
        
        return isRecent;
    }
    
    /**
     * Cleans expired entries from recently processed messages cache
     */
    private static void cleanExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        // Use copy of keys for safe removal during iteration
        Set<String> keys = recentlyProcessedMessages.keySet();
        keys.forEach(key -> {
            Long timestamp = recentlyProcessedMessages.get(key);
            if (timestamp != null && (currentTime - timestamp) > CACHE_EXPIRY_TIME_MS) {
                recentlyProcessedMessages.remove(key);
            }
        });

        originalMessageTimes.keySet().forEach(key -> {
            Long timestamp = originalMessageTimes.get(key);
            if (timestamp != null && (currentTime - timestamp) > MESSAGE_STORE_EXPIRY_TIME_MS) {
                originalMessages.remove(key);
                originalMessageTimes.remove(key);
            }
        });

        relatedMessageTimes.keySet().forEach(key -> {
            Long timestamp = relatedMessageTimes.get(key);
            if (timestamp != null && (currentTime - timestamp) > MESSAGE_STORE_EXPIRY_TIME_MS) {
                relatedMessages.remove(key);
                relatedMessageTimes.remove(key);
            }
        });
    }

    private static boolean isExpired(Long timestamp, long maxAgeMs) {
        return timestamp != null && (System.currentTimeMillis() - timestamp) > maxAgeMs;
    }

    private static void debugLog(String message) {
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(message);
        }
    }
}
