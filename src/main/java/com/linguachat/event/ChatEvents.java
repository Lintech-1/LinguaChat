package com.linguachat.event;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.I18nCompat;
import com.linguachat.translation.MessageStore;
import com.linguachat.translation.TranslationCache;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ChatEvents {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_connect", System.identityHashCode(handler)));
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_connection"));
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_address", handler.getConnection().getAddress()));
            TranslationCache.clear();
            MessageStore.clear();
            LinguaChatMod.getTranslationManager();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_disconnect", System.identityHashCode(handler)));
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_disconnection"));
            TranslationCache.clear();
            MessageStore.clear();
        });
    }
}
