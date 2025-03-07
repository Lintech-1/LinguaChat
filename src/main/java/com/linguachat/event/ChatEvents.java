package com.linguachat.event;

import com.linguachat.LinguaChatMod;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ChatEvents {
    public static void register() {
        // Регистрируем обработчик подключения к серверу
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LinguaChatMod.LOGGER.info("=== Подключение к серверу ===");
            LinguaChatMod.LOGGER.info("Сервер: " + handler.getConnection().getAddress());
        });

        // Регистрируем обработчик отключения от сервера
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LinguaChatMod.LOGGER.info("=== Отключение от сервера ===");
        });
    }
} 