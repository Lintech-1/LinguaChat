package com.linguachat.event;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.I18nCompat;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ChatEvents {
    public static void register() {
        // Fabric fires this on server join, safe to init translator here
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_connection"));
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_address", handler.getConnection().getAddress()));
        });

        // cleanup on disconnect to avoid stale cache
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.server_disconnection"));
        });
    }
} 