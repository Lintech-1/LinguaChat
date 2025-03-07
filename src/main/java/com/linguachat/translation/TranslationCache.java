package com.linguachat.translation;

import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Кэш для переведенных сообщений
 */
public class TranslationCache {
    private static final int MAX_CACHE_SIZE = 500;
    
    // Используем обычную HashMap вместо LinkedHashMap с LRU
    private static final Map<CacheKey, Text> cache = new HashMap<>(MAX_CACHE_SIZE);
    private static final Map<CacheKey, Long> accessTimes = new HashMap<>(MAX_CACHE_SIZE);
    
    // Кэш с блокировкой для потокобезопасности
    private static final Object cacheLock = new Object();

    /**
     * Ключ для кэширования
     */
    private static class CacheKey {
        private final String content;
        private final String sourceLang;
        private final String targetLang;

        public CacheKey(Text text, TranslationDirection direction) {
            this.content = text.getString();
            this.sourceLang = direction.getSourceLang();
            this.targetLang = direction.getTargetLang();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CacheKey cacheKey = (CacheKey) obj;
            return content.equals(cacheKey.content) && 
                   sourceLang.equals(cacheKey.sourceLang) && 
                   targetLang.equals(cacheKey.targetLang);
        }

        @Override
        public int hashCode() {
            int result = content.hashCode();
            result = 31 * result + sourceLang.hashCode();
            result = 31 * result + targetLang.hashCode();
            return result;
        }
    }

    /**
     * Получить перевод из кэша
     */
    public static Text get(Text text, TranslationDirection direction) {
        synchronized (cacheLock) {
            CacheKey key = new CacheKey(text, direction);
            Text result = cache.get(key);
            if (result != null) {
                // Обновляем время доступа
                accessTimes.put(key, System.currentTimeMillis());
            }
            return result;
        }
    }

    /**
     * Добавить перевод в кэш
     */
    public static void put(Text original, Text translated, TranslationDirection direction) {
        synchronized (cacheLock) {
            CacheKey key = new CacheKey(original, direction);
            
            // Если достигнут максимальный размер кэша, удаляем самый старый элемент
            if (cache.size() >= MAX_CACHE_SIZE && !cache.containsKey(key)) {
                removeOldestEntry();
            }
            
            cache.put(key, translated);
            accessTimes.put(key, System.currentTimeMillis());
        }
    }
    
    /**
     * Удаляет самую старую запись из кэша
     */
    private static void removeOldestEntry() {
        if (accessTimes.isEmpty()) return;
        
        CacheKey oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<CacheKey, Long> entry : accessTimes.entrySet()) {
            if (entry.getValue() < oldestTime) {
                oldestTime = entry.getValue();
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
            accessTimes.remove(oldestKey);
        }
    }
    
    /**
     * Очистить кэш
     */
    public static void clear() {
        synchronized (cacheLock) {
            cache.clear();
            accessTimes.clear();
        }
    }
    
    /**
     * Получить текущий размер кэша
     */
    public static int size() {
        synchronized (cacheLock) {
            return cache.size();
        }
    }
} 