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
        LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_path", CONFIG_PATH.toAbsolutePath()));
    }

    private boolean enabled = true;
    private boolean translateIncoming = true;
    private boolean translateOutgoing = true;
    private String defaultSourceLang = "auto";
    private String defaultTargetLang = "en";
    private String preferredTranslator = "google";
    private String deeplApiKey = "";
    private String kagiApiKey = "";
    private String kagiSessionToken = "";
    
    private boolean showOriginalOnHover = true;
    private boolean showTranslatedText = true;
    private boolean asyncTranslation = true;
    private int cacheSize = 512;
    private boolean debugMode = false;

    public static void init() {
        if (INSTANCE == null) {
            load();
            ConfigValidator.ValidationResult result = ConfigValidator.validate(INSTANCE);
            ConfigValidator.logValidationResults(result);
            
            if (result.isConfigModified()) {
                save();
            }
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
                try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_PATH.toFile()), "UTF-8")) {
                    INSTANCE = GSON.fromJson(reader, ModConfig.class);
                    LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_loaded", CONFIG_PATH.toAbsolutePath()));
                }
            } else {
                INSTANCE = new ModConfig();
                save();
                LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_created", CONFIG_PATH.toAbsolutePath()));
            }
        } catch (IOException e) {
            LinguaChatMod.LOGGER.error(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_load_error", e.getMessage()));
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            CONFIG_PATH.toFile().getParentFile().mkdirs();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_PATH.toFile()), "UTF-8")) {
                GSON.toJson(INSTANCE, writer);
                LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_saved", CONFIG_PATH.toAbsolutePath()));
            }
        } catch (IOException e) {
            LinguaChatMod.LOGGER.error(com.linguachat.compat.I18nCompat.translate("linguachat.log.config_save_error", e.getMessage()));
            e.printStackTrace();
        }
    }


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

    public String getKagiApiKey() {
        return kagiApiKey;
    }

    public void setKagiApiKey(String kagiApiKey) {
        this.kagiApiKey = kagiApiKey;
        save();
    }

    public String getKagiSessionToken() {
        return kagiSessionToken;
    }

    public void setKagiSessionToken(String kagiSessionToken) {
        this.kagiSessionToken = kagiSessionToken;
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