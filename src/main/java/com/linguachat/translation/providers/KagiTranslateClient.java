package com.linguachat.translation.providers;

import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.TranslationLogger;

import java.io.IOException;
//? if <1.18 {
/*import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
*///?} else {
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
//?}
import java.nio.charset.StandardCharsets;

/**
 * Kagi Translate provider (needs API key or session token)
 */
public class KagiTranslateClient implements TranslationProvider {
    private static final String KAGI_TRANSLATE_URL = "https://translate.kagi.com/api/translate";
    //? if >=1.18 {
    private final HttpClient httpClient;
    private boolean disabled = false;

    public KagiTranslateClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    //?} else {
    /*private boolean disabled = false;

    public KagiTranslateClient(Object unused) {
        // Java 8 doesn't need HttpClient
    }
    *///?}
    
    @Override
    public String translate(String text, String sourceLang, String targetLang) throws TranslationException {
        if (disabled) {
            throw new TranslationException(
                TranslationException.ErrorType.AUTHENTICATION_ERROR,
                "Kagi disabled due to previous authentication errors"
            );
        }

        TranslationLogger.logTranslationRequest(getProviderName(), text, sourceLang, targetLang);

        ModConfig config = ModConfig.get();

        String requestBody = String.format(
            "{\"text\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"model\":\"standard\"}",
            text.replace("\"", "\\\"").replace("\n", "\\n"),
            sourceLang,
            targetLang
        );

        TranslationLogger.logRequestPayload(getProviderName(), requestBody);

        try {
            //? if <1.18 {
            /*// Java 8: use HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) new URL(KAGI_TRANSLATE_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LinguaChat-Minecraft-Mod/1.0");

            String sessionToken = config.getKagiSessionToken();
            String apiKey = config.getKagiApiKey();

            if (sessionToken != null && !sessionToken.isEmpty()) {
                connection.setRequestProperty("Cookie", "kagi_session=" + sessionToken);
            } else if (apiKey != null && !apiKey.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bot " + apiKey);
            } else {
                throw new TranslationException(
                    TranslationException.ErrorType.AUTHENTICATION_ERROR,
                    "Kagi API key or session token not configured"
                );
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int statusCode = connection.getResponseCode();

            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                        StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
            }

            String responseBodyStr = responseBody.toString();
            TranslationLogger.logResponsePayload(getProviderName(), statusCode, responseBodyStr);

            if (statusCode == 401 || statusCode == 403) {
                disabled = true;
                throw new TranslationException(
                    TranslationException.ErrorType.AUTHENTICATION_ERROR,
                    "Authentication error (code " + statusCode + "). Kagi disabled for this session."
                );
            }

            if (statusCode == 429) {
                throw new TranslationException(
                    TranslationException.ErrorType.RATE_LIMIT_ERROR,
                    "Rate limit exceeded (429)"
                );
            }

            if (statusCode >= 500) {
                String errorMsg = "Server error: " + statusCode;
                try {
                    com.google.gson.JsonObject errorJson = new JsonParser().parse(responseBodyStr).getAsJsonObject();
                    if (errorJson.has("error")) {
                        errorMsg += " - " + errorJson.get("error").getAsString();
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                throw new TranslationException(
                    TranslationException.ErrorType.SERVER_ERROR,
                    errorMsg
                );
            }

            if (statusCode != 200) {
                throw new TranslationException(
                    TranslationException.ErrorType.NETWORK_ERROR,
                    "Unexpected response code: " + statusCode
                );
            }

            com.google.gson.JsonObject jsonResponse = new JsonParser().parse(responseBodyStr).getAsJsonObject();
            String translatedText = jsonResponse.get("translation").getAsString();

            TranslationLogger.logTranslationResponse(getProviderName(), text, translatedText);
            return translatedText;
            *///?} else {
            // Java 11+: HttpClient
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(KAGI_TRANSLATE_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("User-Agent", "LinguaChat-Minecraft-Mod/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            String sessionToken = config.getKagiSessionToken();
            String apiKey = config.getKagiApiKey();

            if (sessionToken != null && !sessionToken.isEmpty()) {
                requestBuilder.header("Cookie", "kagi_session=" + sessionToken);
            } else if (apiKey != null && !apiKey.isEmpty()) {
                requestBuilder.header("Authorization", "Bot " + apiKey);
            } else {
                throw new TranslationException(
                    TranslationException.ErrorType.AUTHENTICATION_ERROR,
                    "Kagi API key or session token not configured"
                );
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            TranslationLogger.logResponsePayload(getProviderName(), response.statusCode(), response.body());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                disabled = true;
                throw new TranslationException(
                    TranslationException.ErrorType.AUTHENTICATION_ERROR,
                    "Authentication error (code " + response.statusCode() + "). Kagi disabled for this session."
                );
            }

            if (response.statusCode() == 429) {
                throw new TranslationException(
                    TranslationException.ErrorType.RATE_LIMIT_ERROR,
                    "Rate limit exceeded (429)"
                );
            }

            if (response.statusCode() >= 500) {
                String errorMsg = "Server error: " + response.statusCode();
                try {
                    com.google.gson.JsonObject errorJson = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (errorJson.has("error")) {
                        errorMsg += " - " + errorJson.get("error").getAsString();
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                throw new TranslationException(
                    TranslationException.ErrorType.SERVER_ERROR,
                    errorMsg
                );
            }

            if (response.statusCode() != 200) {
                throw new TranslationException(
                    TranslationException.ErrorType.NETWORK_ERROR,
                    "Unexpected response code: " + response.statusCode()
                );
            }

            com.google.gson.JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            String translatedText = jsonResponse.get("translation").getAsString();

            TranslationLogger.logTranslationResponse(getProviderName(), text, translatedText);
            return translatedText;
            //?}

        } catch (JsonParseException e) {
            throw new TranslationException(
                TranslationException.ErrorType.PARSE_ERROR,
                "Error parsing response: " + e.getMessage(),
                e
            );
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
        if (disabled) {
            return false;
        }

        ModConfig config = ModConfig.get();
        String apiKey = config.getKagiApiKey();
        String sessionToken = config.getKagiSessionToken();

        return (apiKey != null && !apiKey.isEmpty()) || (sessionToken != null && !sessionToken.isEmpty());
    }

    @Override
    public String getProviderName() {
        return "Kagi";
    }

    @Override
    public boolean validateLanguageCode(String languageCode) {
        // Kagi supports most languages
        return languageCode != null && !languageCode.isEmpty();
    }
}
