package com.linguachat.mixin.client;

import com.linguachat.LinguaChatMod;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.MessageStore;
import com.linguachat.translation.TranslationDirection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow
    protected TextFieldWidget chatField;

    // Статические флаги для предотвращения рекурсии
    @Unique
    private static volatile boolean IS_PROCESSING = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        LinguaChatMod.LOGGER.info("ChatScreen initialized");
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        // Если мы уже обрабатываем сообщение или мод отключен, просто пропускаем
        if (IS_PROCESSING || !ModConfig.get().isEnabled() || !ModConfig.get().isTranslateOutgoing() || chatText.startsWith("/")) {
            return; // Не вмешиваемся, продолжаем стандартную обработку
        }
        
        // Устанавливаем флаг, что мы обрабатываем сообщение
        IS_PROCESSING = true;
        
        try {
            // ВСЕГДА отменяем оригинальную отправку, чтобы управлять процессом самостоятельно
            ci.cancel();
            
            String playerName = MinecraftClient.getInstance().getSession().getUsername();
            String originalMessage = chatText;
            
            LinguaChatMod.LOGGER.info("Original message: " + originalMessage);
            
            // Проверяем, было ли сообщение уже обработано
            if (MessageStore.wasMessageRecentlyProcessed(playerName, originalMessage)) {
                LinguaChatMod.LOGGER.info("Сообщение уже обработано, отправляем без повторного перевода");
                // Отправляем сообщение напрямую через пакет сети
                sendChatMessageDirectly(originalMessage);
                return;
            }
            
            // Помечаем сообщение как обработанное СРАЗУ
            MessageStore.markMessageAsProcessed(playerName, originalMessage);
            
            // Сохраняем оригинальное сообщение для будущего использования
            String key = MessageStore.createMessageKey(playerName, originalMessage);
            MessageStore.storeOriginalMessage(key, originalMessage);
            
            // Переводим сообщение асинхронно
            LinguaChatMod.getTranslationManager().translateAsync(
                Text.literal(originalMessage),
                TranslationDirection.CLIENT_TO_SERVER,
                translatedText -> {
                    try {
                        String translatedString = translatedText.getString();
                        
                        // Проверяем, отличается ли перевод от оригинала
                        if (translatedText != null && !translatedString.equals(originalMessage)) {
                            // Помечаем переведенное сообщение как обработанное
                            MessageStore.markMessageAsProcessed(playerName, translatedString);
                            
                            // Устанавливаем связь между оригинальным и переведенным сообщениями
                            MessageStore.linkMessages(playerName, originalMessage, translatedString);
                            
                            LinguaChatMod.LOGGER.info("Translated outgoing message: " + translatedString);
                            
                            // Отправляем переведенное сообщение напрямую через пакет
                            MinecraftClient.getInstance().execute(() -> {
                                if (ModConfig.get().isShowOriginalOnHover()) {
                                    LinguaChatMod.LOGGER.info("Оригинальный текст '" + originalMessage + "' сохранен для hover-эффекта");
                                }
                                sendChatMessageDirectly(translatedString);
                            });
                        } else {
                            // Если перевод неудачный или идентичен оригиналу, отправляем оригинал
                            LinguaChatMod.LOGGER.info("Перевод не изменил сообщение, отправляем оригинал: " + originalMessage);
                            MinecraftClient.getInstance().execute(() -> {
                                sendChatMessageDirectly(originalMessage);
                            });
                        }
                    } catch (Exception e) {
                        LinguaChatMod.LOGGER.error("Ошибка при отправке переведенного сообщения", e);
                        // В случае ошибки отправляем оригинальное сообщение
                        MinecraftClient.getInstance().execute(() -> {
                            sendChatMessageDirectly(originalMessage);
                        });
                    }
                }
            );
        } finally {
            // Сбрасываем флаг обработки независимо от результата
            IS_PROCESSING = false;
        }
    }
    
    @Unique
    private static void sendChatMessageDirectly(String message) {
        // Безопасно отправляем сообщение напрямую через сетевой обработчик
        try {
            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(message);
                LinguaChatMod.LOGGER.info("Сообщение отправлено напрямую: " + message);
            } else {
                LinguaChatMod.LOGGER.error("Не удалось отправить сообщение, сетевой обработчик недоступен");
            }
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error("Ошибка при прямой отправке сообщения", e);
        }
    }

    @Unique
    private static String getOriginalMessage(String translatedMessage, String playerName) {
        String key = MessageStore.createMessageKey(playerName, translatedMessage);
        return MessageStore.getOriginalMessage(key);
    }
} 