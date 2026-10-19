package com.linguachat.translation.providers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.linguachat.LinguaChatMod;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kagi Translate provider (needs API key or session token)
 */
public class KagiTranslateClient implements TranslationProvider {
    private static final String KAGI_TRANSLATE_URL = "https://translate.kagi.com/api/translate";
    // users paste the whole magic-link URL "https://kagi.com/search?token=..." - grab the token out of it
    private static final Pattern SESSION_TOKEN_IN_URL = Pattern.compile("token=([A-Za-z0-9_-]+)");

    private String lastSessionToken = null;
    private boolean disabled = false;

    //? if >=1.18 {
    private final HttpClient httpClient;

    public KagiTranslateClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    //?} else {
    /*public KagiTranslateClient(Object unused) {
        // Java 8 doesn't need HttpClient
    }
    *///?}

    private String extractSessionToken(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        Matcher matcher = SESSION_TOKEN_IN_URL.matcher(trimmed);
        return matcher.find() ? matcher.group(1) : trimmed;
    }

    private String sessionCookieFor(String token) {
        if (!token.equals(lastSessionToken)) {
            // token changed (or first run) - give Kagi another chance after a previous 401/403
            lastSessionToken = token;
            disabled = false;
        }
        return token;
    }
    
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

        String sessionToken = extractSessionToken(config.getKagiSessionToken());
        String sessionCookie = sessionCookieFor(sessionToken);

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("text", text);
        requestJson.addProperty("from", sourceLang);
        requestJson.addProperty("to", targetLang);
        requestJson.addProperty("model", "standard");
        String requestBody = requestJson.toString();

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

            String apiKey = config.getKagiApiKey();

            if (!sessionToken.isEmpty()) {
                connection.setRequestProperty("Cookie", "kagi_session=" + sessionCookie);
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
                // session LINKS (kagi.com/search?token=...) require interactive browser login and
                // can't authorize the API - the user needs the kagi_session cookie value or an API token
                String hint = responseBodyStr.contains("login_required")
                    ? " Paste the kagi_session cookie value from your browser (DevTools) or use an API token."
                    : "";
                throw new TranslationException(
                    TranslationException.ErrorType.AUTHENTICATION_ERROR,
                    "Authentication error (code " + statusCode + "). Kagi disabled for this session." + hint
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

            JsonObject jsonResponse = new JsonParser().parse(responseBodyStr).getAsJsonObject();
            if (!jsonResponse.has("translation") || jsonResponse.get("translation").isJsonNull()) {
                throw new TranslationException(
                    TranslationException.ErrorType.PARSE_ERROR,
                    "Missing translation field in Kagi response"
                );
            }
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

            String apiKey = config.getKagiApiKey();

            if (!sessionToken.isEmpty()) {
                requestBuilder.header("Cookie", "kagi_session=" + sessionCookie);
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
                // session LINKS (kagi.com/search?token=...) require interactive browser login and
                // can't authorize the API - the user needs the kagi_session cookie value or an API token
                String hint = response.body() != null && response.body().contains("login_required")
                    ? " Paste the kagi_session cookie value from your browser (DevTools) or use an API token."
                    : "";
                throw new TranslationException(
                    TranslationException.ErrorType.AUTHENTICATION_ERROR,
                    "Authentication error (code " + response.statusCode() + "). Kagi disabled for this session." + hint
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

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!jsonResponse.has("translation") || jsonResponse.get("translation").isJsonNull()) {
                throw new TranslationException(
                    TranslationException.ErrorType.PARSE_ERROR,
                    "Missing translation field in Kagi response"
                );
            }
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
        } catch (IllegalStateException | NullPointerException e) {
            throw new TranslationException(
                TranslationException.ErrorType.PARSE_ERROR,
                "Unexpected response format: " + e.getMessage(),
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
