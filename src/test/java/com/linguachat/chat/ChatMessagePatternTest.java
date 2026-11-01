package com.linguachat.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessagePatternTest {

    private static final Pattern PLAYER_MESSAGE_PATTERN = Pattern.compile("<([^>]+)>\\s*(.*)");
    private static final Pattern EXTENDED_MESSAGE_PATTERN = Pattern.compile("(?:<([^>]+)>|\\[([^\\]]+)\\]|\\(([^)]+)\\)|(?:^|\\s+)([\\w\\d_-]+):)\\s*(.*)");

    @ParameterizedTest(name = "Standard chat \"{0}\" -> sender: \"{1}\", message: \"{2}\"")
    @CsvSource({
        "'<Steve> Hello world', Steve, Hello world",
        "'<Alex_123> How are you doing?', Alex_123, How are you doing?",
        "'<Player-Name> [Coordinates: 100, 64, -200]', Player-Name, '[Coordinates: 100, 64, -200]'"
    })
    @DisplayName("Should extract sender and message from standard <Player> format")
    void testStandardPlayerMessagePattern(String chat, String expectedSender, String expectedMessage) {
        Matcher matcher = PLAYER_MESSAGE_PATTERN.matcher(chat);
        assertTrue(matcher.matches(), "Pattern should match standard chat format: " + chat);
        assertEquals(expectedSender, matcher.group(1));
        assertEquals(expectedMessage, matcher.group(2));
    }

    @ParameterizedTest(name = "Extended chat \"{0}\" -> sender: \"{1}\"")
    @CsvSource({
        "'[Admin] Steve: Test message', Admin",
        "'(Clan) Alex: Hi everyone', Clan",
        "'Moderator: Server restarting in 5m', Moderator",
        "'[VIP] Player_42: Selling diamonds', VIP"
    })
    @DisplayName("Should extract prefix/sender identifier from extended message format")
    void testExtendedMessagePattern(String chat, String expectedGroupMatch) {
        Matcher matcher = EXTENDED_MESSAGE_PATTERN.matcher(chat);
        assertTrue(matcher.find(), "Pattern should match extended chat format: " + chat);
        
        String matchedName = null;
        for (int i = 1; i <= 4; i++) {
            if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                matchedName = matcher.group(i);
                break;
            }
        }
        assertEquals(expectedGroupMatch, matchedName);
        assertNotNull(matcher.group(5), "Message body group 5 should not be null");
    }

    @Test
    @DisplayName("Should not match plain text or server system notifications")
    void testNonMatchingMessages() {
        String serverMsg = "Server backup completed successfully.";
        Matcher matcher = PLAYER_MESSAGE_PATTERN.matcher(serverMsg);
        assertFalse(matcher.matches());
    }

    @Test
    @DisplayName("Colon-separated extraction fallback logic")
    void testColonFallbackExtraction() {
        String msg = "GamerTag: Good game!";
        int colonIndex = msg.indexOf(": ");
        assertTrue(colonIndex > 0);
        assertEquals("GamerTag", msg.substring(0, colonIndex));
        assertEquals("Good game!", msg.substring(colonIndex + 2));
    }
}
