package com.linguachat;

import com.linguachat.config.ModConfig;
import com.linguachat.compat.I18nCompat;
import com.linguachat.event.ChatEvents;
import com.linguachat.translation.TranslationCache;
import com.linguachat.translation.TranslationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
//? if >=1.18 {
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//?} else {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?}

public class LinguaChatMod implements ModInitializer {
    public static final String MOD_ID = "linguachat";
    //? if >=1.18 {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    //?} else {
    /*public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    *///?}
    private static TranslationManager translationManager;

    /** Identifying User-Agent for outgoing HTTP requests (Modrinth-style etiquette) */
    public static String getUserAgent() {
        String version = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("dev");
        return "LinguaChat/" + version + " (+https://github.com/Lintech-1/LinguaChat)";
    }

    @Override
    public void onInitialize() {
        LOGGER.info(I18nCompat.translate("linguachat.log.init"));
        
        ModConfig.init();
        translationManager = new TranslationManager();

        ChatEvents.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info(I18nCompat.translate("linguachat.log.ready"));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info(I18nCompat.translate("linguachat.log.shutdown"));
            
            // Clear cache on server stop
            TranslationCache.clear();
        });
        
        LOGGER.info(I18nCompat.translate("linguachat.log.initialized"));
    }

    public static TranslationManager getTranslationManager() {
        if (translationManager == null) {
            LOGGER.info(I18nCompat.translate("linguachat.log.creating_manager"));
            translationManager = new TranslationManager();
        } else {
            translationManager.ensureExecutorRunning();
        }
        return translationManager;
    }
    
    public static void shutdown() {
        if (translationManager != null) {
            translationManager.close();
            translationManager = null;
        }
    }
}
