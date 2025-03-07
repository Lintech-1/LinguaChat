package com.linguachat.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.linguachat.LinguaChatMod;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.MessageStore;
import com.linguachat.translation.TranslationDirection;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.HoverEvent;

import java.time.Instant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(MessageHandler.class)
@SuppressWarnings({"ClassWithoutNoArgConstructor", "ClassHasNoToStringMethod", "MissingClassJavaDoc", "NonStaticInnerClassInSecureContext", "MixinClassInNonMixinPackage", "StaticMixinClass"})
public class MessageHandlerMixin {
    
    @Shadow @Final private MinecraftClient client;
    
    @Unique private static final Pattern PLAYER_MESSAGE_PATTERN = Pattern.compile("<([^>]+)>\\s*(.*)");
    @Unique private static final Pattern EXTENDED_MESSAGE_PATTERN = Pattern.compile("(?:<([^>]+)>|\\[([^\\]]+)\\]|\\(([^)]+)\\)|(?:^|\\s+)([\\w\\d_-]+):)\\s*(.*)");
    @Unique private static final boolean DEBUG = true;
    
    // Используем ThreadLocal для хранения информации об отправителе
    @Unique private static final ThreadLocal<GameProfile> CURRENT_SENDER = new ThreadLocal<>();
    
    @Unique
    private boolean shouldTranslateMessage(GameProfile sender) {
        return shouldTranslateMessage(sender, null);
    }
    
    @Unique
    private boolean shouldTranslateMessage(GameProfile sender, Text message) {
        // Если перевод отключен в настройках, не переводим
        if (!ModConfig.get().isEnabled()) {
            return false;
        }
        
        // Определяем, является ли сообщение собственным
        boolean isOwnMessage = false;
        String senderName = null;
        String playerName = null;
        
        if (client != null && client.player != null && sender != null) {
            senderName = sender.getName();
            playerName = client.player.getName().getString();
            isOwnMessage = senderName.equalsIgnoreCase(playerName);
        }
        
        // Проверка на внутренние сообщения системы, которые не нужно переводить
        if (message != null) {
            String messageText = message.getString();
            
            // Проверяем системные сообщения и сообщения о достижениях
            if (messageText.contains("[System]") || 
                messageText.contains("[CHAT]") ||
                messageText.contains("получил достижение") ||
                messageText.contains("выполнил достижение") ||
                messageText.contains("разблокировал достижение") ||
                messageText.contains("has made the advancement") ||
                messageText.contains("earned the achievement") ||
                messageText.contains("joined the game") || 
                messageText.contains("left the game") ||
                messageText.contains("присоединился к игре") ||
                messageText.contains("покинул игру") ||
                messageText.startsWith("* ") ||
                messageText.startsWith("-> ")) {
                LinguaChatMod.LOGGER.info("shouldTranslateMessage: НЕТ - это системное сообщение или сообщение о достижении");
                return false;
            }
            
            // Извлекаем информацию о сообщении для проверки дубликатов
            if (sender != null) {
                String extractedPlayerName = extractPlayerName(messageText, sender);
                String extractedMessageText = extractMessageText(messageText, extractedPlayerName);
                
                if (extractedPlayerName != null && extractedMessageText != null) {
                    // Проверяем, существует ли оригинал для этого сообщения в кэше
                    String key = MessageStore.createMessageKey(extractedPlayerName, extractedMessageText);
                    String original = MessageStore.getOriginalMessage(key);
                    
                    if (original != null) {
                        // Это переведенное сообщение, которое уже обрабатывается в другом месте
                        LinguaChatMod.LOGGER.info("shouldTranslateMessage: НЕТ - сообщение уже переведено: " + 
                                                    extractedPlayerName + " -> " + extractedMessageText);
                        return false;
                    }
                    
                    // Проверяем, было ли сообщение недавно обработано
                    if (MessageStore.wasMessageRecentlyProcessed(extractedPlayerName, extractedMessageText)) {
                        LinguaChatMod.LOGGER.info("shouldTranslateMessage: НЕТ - сообщение недавно обрабатывалось: " + 
                                                    extractedPlayerName + " -> " + extractedMessageText);
                        return false;
                    }
                    
                    // Проверяем, есть ли оригинал в кэше для связанного переведенного сообщения
                    // Это случай, когда мы уже видели оригинал и ожидаем его перевод
                    String originalKey = MessageStore.createMessageKey(extractedPlayerName, extractedMessageText);
                    if (MessageStore.getOriginalMessage(originalKey) != null) {
                        LinguaChatMod.LOGGER.info("shouldTranslateMessage: НЕТ - найден оригинал в кэше для: " + originalKey);
                        return false;
                    }
                    
                    // Если это сообщение от самого игрока и оно уже на целевом языке
                    if (isOwnMessage) {
                        // Дополнительная проверка для собственных сообщений
                        LinguaChatMod.LOGGER.info("shouldTranslateMessage: проверяем собственное сообщение от " + 
                                                   extractedPlayerName);
                        
                        // Маркируем это сообщение как обработанное, чтобы избежать повторного перевода
                        MessageStore.markMessageAsProcessed(extractedPlayerName, extractedMessageText);
                    }
                }
            }
        }
        
        // Проверяем тип сообщения
        if (isOwnMessage) {
            // Свои сообщения переводим только если включена опция
            boolean shouldTranslate = ModConfig.get().isTranslateOutgoing();
            if (DEBUG) {
                LinguaChatMod.LOGGER.info("shouldTranslateMessage: " + (shouldTranslate ? "ДА" : "НЕТ") + 
                                           " - собственное сообщение");
            }
            return shouldTranslate;
        } else {
            // Чужие сообщения переводим только если включена опция
            boolean shouldTranslate = ModConfig.get().isTranslateIncoming();
            if (DEBUG) {
                LinguaChatMod.LOGGER.info("shouldTranslateMessage: " + (shouldTranslate ? "ДА" : "НЕТ") + 
                                           " - чужое сообщение");
            }
            return shouldTranslate;
        }
    }
    
    // Перехватываем processChatMessageInternal для сохранения отправителя
    @Inject(method = "processChatMessageInternal", at = @At("HEAD"))
    private void onProcessChatMessageInternal(
            MessageType.Parameters typeParameters,
            SignedMessage signedMessage,
            Text content,
            GameProfile profile,
            boolean isSystem,
            Instant timestamp,
            CallbackInfoReturnable<Boolean> cir) {
        
        LinguaChatMod.LOGGER.info("****************************************************************");
        LinguaChatMod.LOGGER.info("=== МЕТОД ВЫЗВАН: processChatMessageInternal ===");
        LinguaChatMod.LOGGER.info("****************************************************************");
        
        String messageContent = content.getString();
        LinguaChatMod.LOGGER.info("Сообщение: '" + messageContent + "'");
        LinguaChatMod.LOGGER.info("Системное: " + isSystem);
        
        // Проверка на сообщения о достижениях - не сохраняем профиль для них
        if (messageContent.contains("получил достижение") || 
            messageContent.contains("выполнил достижение") ||
            messageContent.contains("разблокировал достижение") ||
            messageContent.contains("has made the advancement") ||
            messageContent.contains("earned the achievement") ||
            messageContent.contains("[System]") ||
            messageContent.contains("[CHAT]") ||
            isSystem) {
            
            LinguaChatMod.LOGGER.info("Обнаружено системное сообщение или сообщение о достижении - не сохраняем профиль");
            CURRENT_SENDER.remove(); // Очищаем ThreadLocal на всякий случай
            return;
        }
        
        if (profile != null) {
            // Сохраняем профиль отправителя для использования в методах перехвата
            CURRENT_SENDER.set(profile);
            LinguaChatMod.LOGGER.info("Сохранен профиль отправителя: " + profile.getName() + " (" + profile.getId() + ")");
        } else {
            // Очищаем ThreadLocal, если профиль не определен
            CURRENT_SENDER.remove();
            LinguaChatMod.LOGGER.info("Профиль отправителя не определен, ThreadLocal очищен");
        }
    }
    
    // Перехват addMessage(Text) для ChatHud
    @Redirect(method = "*", 
              at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V"))
    private void redirectAddMessage(ChatHud instance, Text message) {
        LinguaChatMod.LOGGER.info("=== ПЕРЕХВАТ MessageHandler -> ChatHud.addMessage ===");
        LinguaChatMod.LOGGER.info("Оригинальный текст: '" + message.getString() + "'");
        
        // Получаем текущего отправителя
        GameProfile sender = CURRENT_SENDER.get();
        
        if (shouldTranslateMessage(sender, message)) {
            // Выполняем асинхронный перевод
            translateMessageAndAdd(instance, message, sender);
        } else {
            // Просто добавляем оригинальное сообщение
            instance.addMessage(message);
        }
    }
    
    // Перехват addMessage(Text, MessageSignatureData, MessageIndicator) для ChatHud
    @Redirect(method = "*", 
              at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"))
    private void redirectAddMessageWithMeta(ChatHud instance, Text message, MessageSignatureData messageSignatureData, MessageIndicator indicator) {
        LinguaChatMod.LOGGER.info("=== ПЕРЕХВАТ MessageHandler -> ChatHud.addMessage с метаданными ===");
        LinguaChatMod.LOGGER.info("Оригинальный текст: '" + message.getString() + "'");
        
        // Получаем текущего отправителя
        GameProfile sender = CURRENT_SENDER.get();
        
        if (shouldTranslateMessage(sender, message)) {
            // Выполняем асинхронный перевод с сохранением метаданных
            translateMessageAndAddWithMeta(instance, message, messageSignatureData, indicator);
        } else {
            // Просто добавляем оригинальное сообщение
            instance.addMessage(message, messageSignatureData, indicator);
        }
    }
    
    // Перехват setOverlayMessage в InGameHud
    @Redirect(method = "*", 
              at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"))
    private void redirectSetOverlayMessage(InGameHud instance, Text message, boolean tinted) {
        LinguaChatMod.LOGGER.info("=== ПЕРЕХВАТ InGameHud.setOverlayMessage ===");
        LinguaChatMod.LOGGER.info("Оригинальный текст: '" + message.getString() + "'");
        
        // Если включен перевод всплывающих сообщений
        if (shouldTranslateMessage(null, message)) {
            // Перевод и отображение всплывающего сообщения
            translateOverlayMessage(instance, message, tinted);
        } else {
            // Просто отображаем оригинальное сообщение
            instance.setOverlayMessage(message, tinted);
        }
    }
    
    // Метод для асинхронного перевода и добавления сообщения в чат
    @Unique
    private void translateMessageAndAdd(ChatHud chatHud, Text originalMessage, GameProfile sender) {
        // Определяем имя игрока из профиля отправителя
        String senderName = extractSenderName(sender, originalMessage.getString());
        String messageText = extractMessageText(originalMessage.getString(), senderName);
        
        // Если не удалось выделить текст сообщения, просто добавляем оригинал
        if (messageText == null) {
            chatHud.addMessage(originalMessage);
            return;
        }
        
        Text textToTranslate = Text.literal(messageText);
        
        LinguaChatMod.LOGGER.info("Переводим сообщение: " + messageText);
        
        // Выполняем асинхронный перевод
        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();
                
                // Переводим только если получился другой текст
                if (!translatedString.equals(messageText)) {
                    // Восстанавливаем формат сообщения
                    String originalString = originalMessage.getString();
                    String formattedMessage = formatTranslatedMessage(originalString, messageText, translatedString);
                    
                    // Создаем новое сообщение
                    Text newMessage;
                    
                    // Добавляем hover-эффект с оригинальным текстом, если нужно
                    if (ModConfig.get().isShowOriginalOnHover()) {
                        Style newStyle = originalMessage.getStyle().withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Оригинал: " + messageText))
                        );
                        newMessage = Text.literal(formattedMessage).setStyle(newStyle);
                    } else {
                        newMessage = Text.literal(formattedMessage).setStyle(originalMessage.getStyle());
                    }
                    
                    // Связываем оригинальное и переведенное сообщения в хранилище
                    if (senderName != null) {
                        MessageStore.linkMessages(senderName, messageText, translatedString);
                    }
                    
                    // Добавляем сообщение в чат
                    chatHud.addMessage(newMessage);
                } else {
                    // Если перевод идентичен оригиналу, используем оригинальное сообщение
                    chatHud.addMessage(originalMessage);
                }
            }
        );
    }
    
    // Метод для асинхронного перевода и добавления сообщения в чат с метаданными
    @Unique
    private void translateMessageAndAddWithMeta(ChatHud chatHud, Text originalMessage, 
                                        MessageSignatureData signature, MessageIndicator indicator) {
        // Определяем имя игрока из профиля отправителя
        String senderName = extractSenderName(CURRENT_SENDER.get(), originalMessage.getString());
        String messageText = extractMessageText(originalMessage.getString(), senderName);
        
        // Если не удалось выделить текст сообщения, просто добавляем оригинал
        if (messageText == null) {
            chatHud.addMessage(originalMessage, signature, indicator);
            return;
        }
        
        Text textToTranslate = Text.literal(messageText);
        
        LinguaChatMod.LOGGER.info("Переводим сообщение с метаданными: " + messageText);
        
        // Выполняем асинхронный перевод
        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();
                
                // Переводим только если получился другой текст
                if (!translatedString.equals(messageText)) {
                    // Восстанавливаем формат сообщения
                    String originalString = originalMessage.getString();
                    String formattedMessage = formatTranslatedMessage(originalString, messageText, translatedString);
                    
                    // Создаем новое сообщение
                    Text newMessage;
                    
                    // Добавляем hover-эффект с оригинальным текстом, если нужно
                    if (ModConfig.get().isShowOriginalOnHover()) {
                        Style newStyle = originalMessage.getStyle().withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Оригинал: " + messageText))
                        );
                        newMessage = Text.literal(formattedMessage).setStyle(newStyle);
                    } else {
                        newMessage = Text.literal(formattedMessage).setStyle(originalMessage.getStyle());
                    }
                    
                    // Связываем оригинальное и переведенное сообщения в хранилище
                    if (senderName != null) {
                        MessageStore.linkMessages(senderName, messageText, translatedString);
                    }
                    
                    // Добавляем сообщение в чат с сохранением метаданных
                    chatHud.addMessage(newMessage, signature, indicator);
                } else {
                    // Если перевод идентичен оригиналу, используем оригинальное сообщение
                    chatHud.addMessage(originalMessage, signature, indicator);
                }
            }
        );
    }
    
    // Метод для асинхронного перевода и отображения всплывающего сообщения
    @Unique
    private void translateOverlayMessage(InGameHud hud, Text originalMessage, boolean tinted) {
        String originalText = originalMessage.getString();
        
        // Проверка на пустое сообщение
        if (originalText.isEmpty()) {
            hud.setOverlayMessage(originalMessage, tinted);
            return;
        }
        
        // Выполняем перевод
        LinguaChatMod.getTranslationManager().translateAsync(
            originalMessage,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();
                
                // Переводим только если получился другой текст
                if (!translatedString.equals(originalText)) {
                    // Создаем новое сообщение
                    Text newMessage;
                    
                    // Добавляем hover-эффект с оригинальным текстом, если нужно
                    if (ModConfig.get().isShowOriginalOnHover()) {
                        Style newStyle = originalMessage.getStyle().withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Оригинал: " + originalText))
                        );
                        newMessage = Text.literal(translatedString).setStyle(newStyle);
                    } else {
                        newMessage = Text.literal(translatedString).setStyle(originalMessage.getStyle());
                    }
                    
                    // Отображаем переведенное сообщение
                    hud.setOverlayMessage(newMessage, tinted);
                } else {
                    // Если перевод идентичен оригиналу, используем оригинальное сообщение
                    hud.setOverlayMessage(originalMessage, tinted);
                }
            }
        );
    }
    
    // Вспомогательный метод для форматирования переведенного сообщения
    @Unique
    private String formatTranslatedMessage(String originalString, String originalMessage, String translatedMessage) {
        // Если оригинальное сообщение является частью строки, заменяем его на перевод
        if (originalString.contains(originalMessage)) {
            return originalString.replace(originalMessage, translatedMessage);
        }
        
        // Проверяем стандартный формат чата <player> message
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(originalString);
        if (chatMatcher.find()) {
            String playerName = chatMatcher.group(1);
            return "<" + playerName + "> " + translatedMessage;
        }
        
        // Проверяем расширенный формат сообщения
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(originalString);
        if (extendedMatcher.find()) {
            String playerName = null;
            // Ищем имя игрока в группах 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                    playerName = extendedMatcher.group(i);
                    break;
                }
            }
            
            if (playerName != null) {
                // Восстанавливаем формат
                if (originalString.startsWith("<")) {
                    return "<" + playerName + "> " + translatedMessage;
                } else if (originalString.startsWith("[")) {
                    return "[" + playerName + "] " + translatedMessage;
                } else if (originalString.startsWith("(")) {
                    return "(" + playerName + ") " + translatedMessage;
                } else if (originalString.contains(": ")) {
                    return playerName + ": " + translatedMessage;
                }
            }
        }
        
        // Если специальный формат не обнаружен, просто возвращаем переведенное сообщение
        return translatedMessage;
    }
    
    // Вспомогательный метод для извлечения имени отправителя из сообщения
    @Unique
    private String extractSenderName(GameProfile sender, String messageText) {
        // Если есть профиль отправителя, используем его
        if (sender != null) {
            return sender.getName();
        }
        
        // Пробуем извлечь имя из формата сообщения
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(messageText);
        if (chatMatcher.find()) {
            return chatMatcher.group(1);
        }
        
        // Проверяем расширенный формат сообщения
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(messageText);
        if (extendedMatcher.find()) {
            // Ищем имя игрока в группах 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                    return extendedMatcher.group(i);
                }
            }
        }
        
        // Если специальный формат не обнаружен, не удалось определить имя отправителя
        return null;
    }
    
    // Вспомогательный метод для извлечения текста сообщения, исключая имя отправителя
    @Unique
    private String extractMessageText(String fullMessage, String senderName) {
        if (senderName == null) {
            // Если имя отправителя не определено, возвращаем всё сообщение
            return fullMessage;
        }
        
        // Проверяем стандартный формат чата <player> message
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(fullMessage);
        if (chatMatcher.find() && chatMatcher.group(1).equals(senderName)) {
            return chatMatcher.group(2);
        }
        
        // Проверяем расширенный формат сообщения
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(fullMessage);
        if (extendedMatcher.find()) {
            // Ищем имя игрока в группах 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null && 
                    !extendedMatcher.group(i).isEmpty() && 
                    extendedMatcher.group(i).equals(senderName)) {
                    // Группа 5 содержит текст сообщения
                    return extendedMatcher.group(5);
                }
            }
        }
        
        // Проверяем формат PlayerName: message
        String prefix = senderName + ": ";
        if (fullMessage.startsWith(prefix)) {
            return fullMessage.substring(prefix.length());
        }
        
        // Если специальный формат не обнаружен, возвращаем всё сообщение
        return fullMessage;
    }
    
    // Вспомогательный метод для извлечения имени игрока из сообщения
    @Unique
    private String extractPlayerName(String messageText, GameProfile sender) {
        // Используем имя профиля отправителя, если доступно
        if (sender != null) {
            return sender.getName();
        }
        
        // Пробуем извлечь имя из формата сообщения
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(messageText);
        if (chatMatcher.find()) {
            return chatMatcher.group(1);
        }
        
        // Проверяем расширенный формат сообщения
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(messageText);
        if (extendedMatcher.find()) {
            // Ищем имя игрока в группах 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                    return extendedMatcher.group(i);
                }
            }
        }
        
        // Если сообщение содержит двоеточие, попробуем извлечь имя из начала
        int colonIndex = messageText.indexOf(": ");
        if (colonIndex > 0) {
            return messageText.substring(0, colonIndex);
        }
        
        // Не удалось определить имя отправителя
        return null;
    }
} 