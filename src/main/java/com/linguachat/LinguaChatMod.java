package com.linguachat;

import com.linguachat.config.ModConfig;
import com.linguachat.event.ChatEvents;
import com.linguachat.translation.TranslationCache;
import com.linguachat.translation.TranslationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinguaChatMod implements ModInitializer {
    public static final String MOD_ID = "linguachat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static TranslationManager translationManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Инициализация мода LinguaChat");
        
        // Load config
        ModConfig.init();
        translationManager = new TranslationManager();

        // Регистрируем обработчики событий
        ChatEvents.register();

        // Register server startup event
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("LinguaChat готов к работе!");
        });

        // Register server stopping event
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Завершение работы мода LinguaChat");
            
            // Очищаем кэш при остановке сервера
            TranslationCache.clear();
        });
        
        // Регистрируем события подключения и отключения от сервера - только один раз
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("=== Подключение к серверу [" + System.identityHashCode(handler) + "] ===");
            LOGGER.info("Сервер: " + handler.getConnection().getAddress());
            LOGGER.info("Подключение к серверу - инициализация переводчика");
            
            // Очищаем хранилище сообщений и кэш при подключении
            // Это предотвращает проблемы с дублированием при переподключении
            TranslationCache.clear();
            com.linguachat.translation.MessageStore.clear();
            
            // Убедимся, что переводчик готов к работе
            getTranslationManager();
        });
        
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("=== Отключение от сервера [" + System.identityHashCode(handler) + "] ===");
            LOGGER.info("Отключение от сервера - очистка кэша переводов");
            // Очищаем кэш переводов и хранилище оригинальных сообщений
            TranslationCache.clear();
            // Очищаем хранилище MessageStore для предотвращения проблем с дублированием при переподключении
            com.linguachat.translation.MessageStore.clear();
        });

        LOGGER.info("LinguaChat инициализирован успешно");
    }

    public static TranslationManager getTranslationManager() {
        if (translationManager == null) {
            LOGGER.info("Создание нового экземпляра TranslationManager");
            translationManager = new TranslationManager();
        } else {
            // Убедимся, что транслятор работает
            translationManager.ensureExecutorRunning();
            LOGGER.info("Использование существующего экземпляра TranslationManager");
        }
        return translationManager;
    }
    
    // Метод для корректного завершения работы мода
    public static void shutdown() {
        if (translationManager != null) {
            translationManager.close();
            translationManager = null;
        }
    }
} 