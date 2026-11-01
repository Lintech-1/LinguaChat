package com.linguachat.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProviderResponseParserTest {

    private String parseGooglePayload(String responseBodyStr) {
        JsonArray jsonArray = JsonParser.parseString(responseBodyStr).getAsJsonArray();
        StringBuilder translatedText = new StringBuilder();
        JsonArray translationArray = jsonArray.get(0).getAsJsonArray();

        for (int i = 0; i < translationArray.size(); i++) {
            translatedText.append(translationArray.get(i).getAsJsonArray().get(0).getAsString());
        }
        return translatedText.toString();
    }

    @Test
    @DisplayName("Should parse standard single-sentence Google Translate response")
    void testSingleSentenceGoogleResponse() {
        String json = "[[[\"Привет, мир!\",\"Hello, world!\",null,null,10]],null,\"en\",null,null,null,null,[]]";
        assertEquals("Привет, мир!", parseGooglePayload(json));
    }

    @Test
    @DisplayName("Should parse multi-sentence Google Translate response concatenated")
    void testMultiSentenceGoogleResponse() {
        String json = "[[[\"Привет. \",\"Hello. \",null,null,10],[\"Как дела?\",\"How are you?\",null,null,10]],null,\"en\"]";
        assertEquals("Привет. Как дела?", parseGooglePayload(json));
    }

    @Test
    @DisplayName("Should parse special characters and punctuation in response")
    void testSpecialCharacters() {
        String json = "[[[\"Цена: 100$ & <тест>!\",\"Price: 100$ & <test>!\",null,null,10]],null,\"en\"]";
        assertEquals("Цена: 100$ & <тест>!", parseGooglePayload(json));
    }
}
