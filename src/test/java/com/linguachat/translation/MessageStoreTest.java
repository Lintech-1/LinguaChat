package com.linguachat.translation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageStoreTest {

    @BeforeEach
    void setUp() {
        MessageStore.clear();
    }

    @Test
    @DisplayName("Should create consistent message keys in format 'player:text'")
    void testCreateMessageKey() {
        String key = MessageStore.createMessageKey("Steve", "Hello world");
        assertEquals("Steve:Hello world", key);
    }

    @Test
    @DisplayName("Should store and retrieve original message by key")
    void testStoreAndGetOriginalMessage() {
        String key = MessageStore.createMessageKey("Alex", "Good morning");
        MessageStore.storeOriginalMessage(key, "Good morning");

        String retrieved = MessageStore.getOriginalMessage(key);
        assertEquals("Good morning", retrieved);
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void testGetNonExistentKeyReturnsNull() {
        assertNull(MessageStore.getOriginalMessage("NonExistentPlayer:MissingText"));
    }

    @Test
    @DisplayName("Should link original and translated messages bidirectionally")
    void testLinkMessages() {
        String player = "TranslatorBot";
        String original = "Привет";
        String translated = "Hello";

        MessageStore.linkMessages(player, original, translated);

        String originalKey = MessageStore.createMessageKey(player, original);
        String translatedKey = MessageStore.createMessageKey(player, translated);

        // Store should return original text when querying translated key (for hover popup)
        assertEquals(original, MessageStore.getOriginalMessage(translatedKey));
    }

    @Test
    @DisplayName("Should mark and track recently processed messages")
    void testRecentlyProcessedMessages() {
        String player = "Steve";
        String message = "Quick brown fox";

        assertFalse(MessageStore.wasMessageRecentlyProcessed(player, message));

        MessageStore.markMessageAsProcessed(player, message);
        assertTrue(MessageStore.wasMessageRecentlyProcessed(player, message));

        MessageStore.unmarkMessageAsProcessed(player, message);
        assertFalse(MessageStore.wasMessageRecentlyProcessed(player, message));
    }

    @Test
    @DisplayName("Clear should wipe all cached entries and links")
    void testClearWipesStore() {
        String key = MessageStore.createMessageKey("Steve", "Test");
        MessageStore.storeOriginalMessage(key, "Test");
        MessageStore.markMessageAsProcessed("Steve", "Test");

        MessageStore.clear();

        assertNull(MessageStore.getOriginalMessage(key));
        assertFalse(MessageStore.wasMessageRecentlyProcessed("Steve", "Test"));
    }
}
