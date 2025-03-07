package com.linguachat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.linguachat.LinguaChatMod;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.TranslationDirection;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.HoverEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
@SuppressWarnings({"ClassWithoutNoArgConstructor", "ClassHasNoToStringMethod", "MissingClassJavaDoc", "NonStaticInnerClassInSecureContext", "MixinClassInNonMixinPackage", "StaticMixinClass"})
public class ClientPlayNetworkHandlerMixin {
    
    private static final Pattern PLAYER_MESSAGE_PATTERN = Pattern.compile("<([^>]+)>\\s*(.*)");
    private static final Pattern EXTENDED_MESSAGE_PATTERN = Pattern.compile("(?:<([^>]+)>|\\[([^\\]]+)\\]|\\(([^)]+)\\)|(?:^|\\s+)([\\w\\d_-]+):)\\s*(.*)");

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        LinguaChatMod.LOGGER.error("***************************************************************");
        LinguaChatMod.LOGGER.error("=== КРИТИЧЕСКАЯ ТОЧКА: ClientPlayNetworkHandlerMixin.onGameMessage ===");
        LinguaChatMod.LOGGER.error("***************************************************************");
        
        // Получаем текст сообщения
        final Text message = packet.content();
        final String originalText = message.getString();
        
        LinguaChatMod.LOGGER.error("Сетевое сообщение: '" + originalText + "'");
        LinguaChatMod.LOGGER.error("Класс сообщения: " + message.getClass().getName());
        LinguaChatMod.LOGGER.error("Стиль сообщения: " + message.getStyle());
        
        // Проверяем включение мода и опцию перевода входящих сообщений
        if (!ModConfig.get().isEnabled()) {
            LinguaChatMod.LOGGER.error("Мод отключен в настройках");
            return;
        }
        
        if (!ModConfig.get().isTranslateIncoming()) {
            LinguaChatMod.LOGGER.error("Перевод входящих сообщений отключен в настройках");
            return;
        }
        
        // Пропускаем системные сообщения
        if (isSystemMessage(originalText)) {
            LinguaChatMod.LOGGER.error("Пропуск системного сообщения: " + originalText);
            return;
        }
        
        // Проверяем различные форматы сообщений от игроков
        String playerName = null;
        String messageText = null;
        
        // Стандартный формат <Player> message
        Matcher standardMatcher = PLAYER_MESSAGE_PATTERN.matcher(originalText);
        if (standardMatcher.find()) {
            playerName = standardMatcher.group(1);
            messageText = standardMatcher.group(2);
            LinguaChatMod.LOGGER.error("Обнаружено сообщение стандартного формата от: " + playerName);
        } else {
            // Расширенный формат поиска
            Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(originalText);
            if (extendedMatcher.find()) {
                // Ищем имя игрока в группах 1-4
                for (int i = 1; i <= 4; i++) {
                    if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                        playerName = extendedMatcher.group(i);
                        break;
                    }
                }
                messageText = extendedMatcher.group(5);
                LinguaChatMod.LOGGER.error("Обнаружено сообщение расширенного формата от: " + playerName);
            } else if (originalText.contains(": ")) {
                // Еще один часто встречающийся формат "PlayerName: message"
                int colonIndex = originalText.indexOf(": ");
                if (colonIndex > 0) {
                    playerName = originalText.substring(0, colonIndex);
                    messageText = originalText.substring(colonIndex + 2);
                    LinguaChatMod.LOGGER.error("Обнаружено сообщение с двоеточием от: " + playerName);
                }
            }
        }
        
        // Если не удалось распознать формат сообщения
        if (playerName == null || messageText == null || messageText.isEmpty()) {
            LinguaChatMod.LOGGER.error("Не удалось распознать формат сообщения: " + originalText);
            return;
        }
        
        // Проверяем, что это не наше собственное сообщение
        String currentPlayer = MinecraftClient.getInstance().getSession().getUsername();
        if (playerName.equals(currentPlayer)) {
            LinguaChatMod.LOGGER.error("Пропуск собственного сообщения от: " + currentPlayer);
            return;
        }
        
        LinguaChatMod.LOGGER.error("Текст для перевода: '" + messageText + "'");
        
        // Для использования в лямбда-выражении переменные должны быть final
        final String finalPlayerName = playerName;
        final String finalMessageText = messageText;
        
        // Переводим сообщение
        Text textToTranslate = Text.literal(finalMessageText);
        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();
                LinguaChatMod.LOGGER.error("Переведено на " + ModConfig.get().getDefaultTargetLang() + ": '" + translatedString + "'");
                
                if (!translatedString.equals(finalMessageText)) {
                    // Восстанавливаем формат сообщения
                    String formattedMessage;
                    if (originalText.startsWith("<")) {
                        // Стандартный формат <Player> message
                        formattedMessage = "<" + finalPlayerName + "> " + translatedString;
                    } else if (originalText.contains(": ")) {
                        // Формат PlayerName: message
                        formattedMessage = finalPlayerName + ": " + translatedString;
                    } else {
                        // Другой формат - пытаемся сохранить оригинальную структуру
                        int msgStart = originalText.indexOf(finalMessageText);
                        String prefix = originalText.substring(0, msgStart);
                        formattedMessage = prefix + translatedString;
                    }
                    
                    // Создаем новое сообщение и добавляем его в чат
                    Text newMessage;
                    
                    // Добавляем hover-эффект для отображения оригинального текста
                    if (ModConfig.get().isShowOriginalOnHover()) {
                        // Создаем текст для hover-эффекта
                        Text hoverText = Text.literal("Оригинал: " + finalMessageText);
                        
                        // Применяем hover-эффект к стилю
                        Style newStyle = message.getStyle().withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
                        );
                        
                        // Создаем сообщение с hover-эффектом
                        newMessage = Text.literal(formattedMessage).setStyle(newStyle);
                        LinguaChatMod.LOGGER.error("Добавлен hover-эффект с оригинальным текстом: " + finalMessageText);
                    } else {
                        // Без hover-эффекта
                        newMessage = Text.literal(formattedMessage).setStyle(message.getStyle());
                    }
                    
                    // Отменяем оригинальное сообщение
                    ci.cancel();
                    
                    // Добавляем переведенное сообщение в чат
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(newMessage);
                    LinguaChatMod.LOGGER.error("Переведенное сообщение добавлено в чат: " + formattedMessage);
                }
            }
        );
    }
    
    private boolean isSystemMessage(String text) {
        return text.contains("joined the game") || 
               text.contains("left the game") ||
               text.contains("присоединился к игре") ||
               text.contains("вышел из игры") ||
               text.startsWith("[Server]") ||
               text.startsWith("[Сервер]") ||
               text.startsWith("/") ||
               text.startsWith("*") ||
               text.contains("[System]") ||
               text.contains("[CHAT]") ||
               text.contains("получил достижение") ||
               text.contains("выполнил достижение") ||
               text.contains("разблокировал достижение") ||
               text.contains("has made the advancement") ||
               text.contains("earned the achievement");
    }
} 