package com.linguachat.translation.providers;

import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.linguachat.translation.TranslationLogger;

import java.io.IOException;
//? if <1.18 {
/*import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
*///?} else {
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
//?}
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Google Translate provider (free, no key needed)
 */
public class GoogleTranslateClient implements TranslationProvider {
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t";
    //? if >=1.18 {
    private final HttpClient httpClient;
    
    public GoogleTranslateClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    //?} else {
    /*public GoogleTranslateClient(Object unused) {
        // Java 8 doesn't need HttpClient
    }
    *///?}
    
    @Override
    public String translate(String text, String sourceLang, String targetLang) throws TranslationException {
        TranslationLogger.logTranslationRequest(getProviderName(), text, sourceLang, targetLang);

        try {
            String sourceParam = "auto".equals(sourceLang) ? "auto" : sourceLang;

            //? if <1.18 {
            /*String url = GOOGLE_TRANSLATE_URL +
                         "&sl=" + URLEncoder.encode(sourceParam, "UTF-8") +
                         "&tl=" + URLEncoder.encode(targetLang, "UTF-8") +
                         "&q=" + URLEncoder.encode(text, "UTF-8");
            *///?} else {
            String url = GOOGLE_TRANSLATE_URL +
                         "&sl=" + URLEncoder.encode(sourceParam, StandardCharsets.UTF_8) +
                         "&tl=" + URLEncoder.encode(targetLang, StandardCharsets.UTF_8) +
                         "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            //?}

            //? if <1.18 {
            /*// Java 8: HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int statusCode = connection.getResponseCode();

            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
            }

            String responseBodyStr = responseBody.toString();
            TranslationLogger.logResponsePayload(getProviderName(), statusCode, responseBodyStr);

            if (statusCode != 200) {
                throw new TranslationException(
                    TranslationException.ErrorType.NETWORK_ERROR,
                    "HTTP code: " + statusCode
                );
            }
            *///?} else {
            // Java 11+: HttpClient
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            TranslationLogger.logResponsePayload(getProviderName(), response.statusCode(), response.body());

            if (response.statusCode() != 200) {
                throw new TranslationException(
                    TranslationException.ErrorType.NETWORK_ERROR,
                    "HTTP code: " + response.statusCode()
                );
            }

            String responseBodyStr = response.body();
            //?}

            //? if <1.18 {
            /*com.google.gson.JsonArray jsonArray = new JsonParser().parse(responseBodyStr).getAsJsonArray();
            *///?} else {
            com.google.gson.JsonArray jsonArray = JsonParser.parseString(responseBodyStr).getAsJsonArray();
            //?}

            StringBuilder translatedText = new StringBuilder();
            com.google.gson.JsonArray translationArray = jsonArray.get(0).getAsJsonArray();

            for (int i = 0; i < translationArray.size(); i++) {
                translatedText.append(translationArray.get(i).getAsJsonArray().get(0).getAsString());
            }

            String result = translatedText.toString();
            TranslationLogger.logTranslationResponse(getProviderName(), text, result);
            return result;

        } catch (JsonParseException e) {
            throw new TranslationException(
                TranslationException.ErrorType.PARSE_ERROR,
                "Error parsing response: " + e.getMessage(),
                e
            );
        //? if <1.18 {
        /*} catch (java.io.UnsupportedEncodingException e) {
            throw new TranslationException(
                TranslationException.ErrorType.NETWORK_ERROR,
                "Encoding error: " + e.getMessage(),
                e
            );
        *///?}
        } catch (IOException e) {
            throw new TranslationException(
                TranslationException.ErrorType.NETWORK_ERROR,
                "Network error: " + e.getMessage(),
                e
            );
        //? if >=1.18 {
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TranslationException(
                TranslationException.ErrorType.TIMEOUT_ERROR,
                "Request interrupted",
                e
            );
        //?}
        }
    }

    @Override
    public boolean isAvailable() {
        // Google is always available (no key needed)
        return true;
    }

    @Override
    public String getProviderName() {
        return "Google";
    }

    @Override
    public boolean validateLanguageCode(String languageCode) {
        // Google supports almost everything, including "auto"
        return languageCode != null && !languageCode.isEmpty();
    }
}
