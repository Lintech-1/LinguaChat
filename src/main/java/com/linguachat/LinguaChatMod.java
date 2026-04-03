package com.linguachat;

import com.linguachat.compat.I18nCompat;
import com.linguachat.config.ModConfig;
import com.linguachat.event.ChatEvents;
import com.linguachat.translation.TranslationCache;
import com.linguachat.translation.TranslationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
        
        // Register server connection/disconnection events - only once
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info(I18nCompat.translate("linguachat.log.server_connect", System.identityHashCode(handler)));
            LOGGER.info(I18nCompat.translate("linguachat.log.server_address", handler.getConnection().getAddress()));
            LOGGER.info(I18nCompat.translate("linguachat.log.server_connection"));
            
            // Clear message store and cache on connection
            // This prevents duplication issues on reconnection
            TranslationCache.clear();
            com.linguachat.translation.MessageStore.clear();
            
            getTranslationManager();
        });
        
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info(I18nCompat.translate("linguachat.log.server_disconnect", System.identityHashCode(handler)));
            LOGGER.info(I18nCompat.translate("linguachat.log.server_disconnection"));
            TranslationCache.clear();
            // Clear MessageStore to prevent duplication issues on reconnection
            com.linguachat.translation.MessageStore.clear();
        });

        LOGGER.info("LinguaChat initialized successfully");
    }

    public static TranslationManager getTranslationManager() {
        if (translationManager == null) {
            LOGGER.info("Creating new TranslationManager instance");
            translationManager = new TranslationManager();
        } else {
            translationManager.ensureExecutorRunning();
            LOGGER.info("Using existing TranslationManager instance");
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
 