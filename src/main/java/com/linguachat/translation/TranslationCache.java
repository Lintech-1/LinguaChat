package com.linguachat.translation;

import com.linguachat.config.ModConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for translated messages
 */
//? if >=26.1 {
//import net.minecraft.network.chat.Component;
//
//public class TranslationCache {
//    private static final int DEFAULT_MAX_CACHE_SIZE = 512;
//
//    // Use regular HashMap instead of LinkedHashMap with LRU
//    private static final Map<CacheKey, Component> cache = new HashMap<>(DEFAULT_MAX_CACHE_SIZE);
//    private static final Map<CacheKey, Long> accessTimes = new HashMap<>(DEFAULT_MAX_CACHE_SIZE);
//
//    // Cache with locking for thread safety
//    private static final Object cacheLock = new Object();
//
//    /**
//     * Key for caching
//     */
//    private static class CacheKey {
//        private final String content;
//        private final String sourceLang;
//        private final String targetLang;
//
//        public CacheKey(Component text, TranslationDirection direction) {
//            this.content = text.getString();
//            this.sourceLang = direction.getSourceLang();
//            this.targetLang = direction.getTargetLang();
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) return true;
//            if (obj == null || getClass() != obj.getClass()) return false;
//            CacheKey cacheKey = (CacheKey) obj;
//            return content.equals(cacheKey.content) &&
//                   sourceLang.equals(cacheKey.sourceLang) &&
//                   targetLang.equals(cacheKey.targetLang);
//        }
//
//        @Override
//        public int hashCode() {
//            int result = content.hashCode();
//            result = 31 * result + sourceLang.hashCode();
//            result = 31 * result + targetLang.hashCode();
//            return result;
//        }
//    }
//
//    /**
//     * Get translation from cache
//     */
//    public static Component get(Component text, TranslationDirection direction) {
//        synchronized (cacheLock) {
//            CacheKey key = new CacheKey(text, direction);
//            Component result = cache.get(key);
//            if (result != null) {
//                accessTimes.put(key, System.currentTimeMillis());
//            }
//            return result;
//        }
//    }
//
//    /**
//     * Add translation to cache
//     */
//    public static void put(Component original, Component translated, TranslationDirection direction) {
//        synchronized (cacheLock) {
//            CacheKey key = new CacheKey(original, direction);
//            int maxCacheSize = getMaxCacheSize();
//
//            while (cache.size() >= maxCacheSize && !cache.containsKey(key) && !accessTimes.isEmpty()) {
//                removeOldestEntry();
//            }
//
//            cache.put(key, translated);
//            accessTimes.put(key, System.currentTimeMillis());
//        }
//    }
//
//    private static int getMaxCacheSize() {
//        try {
//            return Math.max(1, ModConfig.get().getCacheSize());
//        } catch (Exception e) {
//            return DEFAULT_MAX_CACHE_SIZE;
//        }
//    }
//
//    /**
//     * Removes oldest entry from cache
//     */
//    private static void removeOldestEntry() {
//        if (accessTimes.isEmpty()) return;
//
//        CacheKey oldestKey = null;
//        long oldestTime = Long.MAX_VALUE;
//
//        for (Map.Entry<CacheKey, Long> entry : accessTimes.entrySet()) {
//            if (entry.getValue() < oldestTime) {
//                oldestTime = entry.getValue();
//                oldestKey = entry.getKey();
//            }
//        }
//
//        if (oldestKey != null) {
//            cache.remove(oldestKey);
//            accessTimes.remove(oldestKey);
//        }
//    }
//
//    /**
//     * Clear cache
//     */
//    public static void clear() {
//        synchronized (cacheLock) {
//            cache.clear();
//            accessTimes.clear();
//        }
//    }
//
//    /**
//     * Get current cache size
//     */
//    public static int size() {
//        synchronized (cacheLock) {
//            return cache.size();
//        }
//    }
//}
//?} else {
import net.minecraft.text.Text;

public class TranslationCache {
    private static final int DEFAULT_MAX_CACHE_SIZE = 512;

    // Use regular HashMap instead of LinkedHashMap with LRU
    private static final Map<CacheKey, Text> cache = new HashMap<>(DEFAULT_MAX_CACHE_SIZE);
    private static final Map<CacheKey, Long> accessTimes = new HashMap<>(DEFAULT_MAX_CACHE_SIZE);

    // Cache with locking for thread safety
    private static final Object cacheLock = new Object();

    // Key for caching
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

    // Get translation from cache
    public static Text get(Text text, TranslationDirection direction) {
        synchronized (cacheLock) {
            CacheKey key = new CacheKey(text, direction);
            Text result = cache.get(key);
            if (result != null) {
                accessTimes.put(key, System.currentTimeMillis());
            }
            return result;
        }
    }

    // Add translation to cache
    public static void put(Text original, Text translated, TranslationDirection direction) {
        synchronized (cacheLock) {
            CacheKey key = new CacheKey(original, direction);
            int maxCacheSize = getMaxCacheSize();

            while (cache.size() >= maxCacheSize && !cache.containsKey(key) && !accessTimes.isEmpty()) {
                removeOldestEntry();
            }

            cache.put(key, translated);
            accessTimes.put(key, System.currentTimeMillis());
        }
    }

    private static int getMaxCacheSize() {
        try {
            return Math.max(1, ModConfig.get().getCacheSize());
        } catch (Exception e) {
            return DEFAULT_MAX_CACHE_SIZE;
        }
    }

    // Removes oldest entry from cache
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

    // Clear cache
    public static void clear() {
        synchronized (cacheLock) {
            cache.clear();
            accessTimes.clear();
        }
    }

    // Get current cache size
    public static int size() {
        synchronized (cacheLock) {
            return cache.size();
        }
    }
}
//?}
