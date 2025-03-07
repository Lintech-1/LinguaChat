package com.linguachat.config;

import com.linguachat.LinguaChatMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH;
    private static ModConfig INSTANCE;

    static {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        CONFIG_PATH = configDir.resolve("linguachat.json");
        LinguaChatMod.LOGGER.info("Configuration file will be at: " + CONFIG_PATH.toAbsolutePath());
    }

    // Основные настройки
    private boolean enabled = true;
    private boolean translateIncoming = true;
    private boolean translateOutgoing = true;
    private String defaultSourceLang = "auto";
    private String defaultTargetLang = "en";
    private String preferredTranslator = "google";
    private String deeplApiKey = "";
    
    // Дополнительные настройки
    private boolean showOriginalOnHover = true;
    private boolean showTranslatedText = true;
    private boolean asyncTranslation = true;
    private int cacheSize = 512;
    private boolean debugMode = false;

    public static void init() {
        if (INSTANCE == null) {
            load();
        }
    }

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    private static void load() {
        try {
            if (CONFIG_PATH.toFile().exists()) {
                try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                    INSTANCE = GSON.fromJson(reader, ModConfig.class);
                    LinguaChatMod.LOGGER.info("Configuration loaded from: " + CONFIG_PATH.toAbsolutePath());
                }
            } else {
                INSTANCE = new ModConfig();
                save();
                LinguaChatMod.LOGGER.info("Created new configuration at: " + CONFIG_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            LinguaChatMod.LOGGER.error("Failed to load configuration: " + e.getMessage());
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            CONFIG_PATH.toFile().getParentFile().mkdirs();
            try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(INSTANCE, writer);
                LinguaChatMod.LOGGER.info("Configuration saved to: " + CONFIG_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            LinguaChatMod.LOGGER.error("Failed to save configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public boolean isTranslateIncoming() {
        return translateIncoming && enabled;
    }

    public void setTranslateIncoming(boolean translateIncoming) {
        this.translateIncoming = translateIncoming;
        save();
    }

    public boolean isTranslateOutgoing() {
        return translateOutgoing && enabled;
    }

    public void setTranslateOutgoing(boolean translateOutgoing) {
        this.translateOutgoing = translateOutgoing;
        save();
    }

    public String getDefaultSourceLang() {
        return defaultSourceLang;
    }

    public void setDefaultSourceLang(String defaultSourceLang) {
        this.defaultSourceLang = defaultSourceLang;
        save();
    }

    public String getDefaultTargetLang() {
        return defaultTargetLang;
    }

    public void setDefaultTargetLang(String defaultTargetLang) {
        this.defaultTargetLang = defaultTargetLang;
        save();
    }

    public String getPreferredTranslator() {
        return preferredTranslator;
    }

    public void setPreferredTranslator(String preferredTranslator) {
        this.preferredTranslator = preferredTranslator;
        save();
    }

    public String getDeeplApiKey() {
        return deeplApiKey;
    }

    public void setDeeplApiKey(String deeplApiKey) {
        this.deeplApiKey = deeplApiKey;
        save();
    }

    public boolean isShowOriginalOnHover() {
        return showOriginalOnHover;
    }

    public void setShowOriginalOnHover(boolean showOriginalOnHover) {
        this.showOriginalOnHover = showOriginalOnHover;
        save();
    }

    public boolean isShowTranslatedText() {
        return showTranslatedText;
    }

    public void setShowTranslatedText(boolean showTranslatedText) {
        this.showTranslatedText = showTranslatedText;
        save();
    }

    public boolean isAsyncTranslation() {
        return asyncTranslation;
    }

    public void setAsyncTranslation(boolean asyncTranslation) {
        this.asyncTranslation = asyncTranslation;
        save();
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        save();
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        save();
    }
} 