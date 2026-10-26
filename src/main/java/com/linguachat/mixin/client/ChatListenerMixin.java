package com.linguachat.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.TextCompat;
import com.linguachat.config.ModConfig;
import com.linguachat.compat.I18nCompat;
import com.linguachat.translation.MessageStore;
import com.linguachat.translation.TranslationDirection;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.time.Instant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 26.1+ port of MessageHandlerMixin: MessageHandler (Yarn) became ChatListener (Mojang naming),
 * ChatHud became ChatComponent and InGameHud became Hud.
 */
@Mixin(ChatListener.class)
@SuppressWarnings({"ClassWithoutNoArgConstructor", "ClassHasNoToStringMethod", "MissingClassJavaDoc", "NonStaticInnerClassInSecureContext", "MixinClassInNonMixinPackage", "StaticMixinClass"})
public class ChatListenerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique private static final Pattern PLAYER_MESSAGE_PATTERN = Pattern.compile("<([^>]+)>\\s*(.*)");
    @Unique private static final Pattern EXTENDED_MESSAGE_PATTERN = Pattern.compile("(?:<([^>]+)>|\\[([^\\]]+)\\]|\\(([^)]+)\\)|(?:^|\\s+)([\\w\\d_-]+):)\\s*(.*)");

    @Unique
    private static void debugLog(String key, Object... args) {
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate(key, args));
        }
    }

    // ThreadLocal stores sender info per-thread to avoid race conditions
    @Unique private static final ThreadLocal<GameProfile> CURRENT_SENDER = new ThreadLocal<>();

    // true when the message arrived via a system path (handleSystemMessage / handleDisguisedChatMessage) -
    // these keep vanilla localization and must not be translated like player chat
    @Unique private static final ThreadLocal<Boolean> CURRENT_IS_SYSTEM = new ThreadLocal<>();

    // Intercept showMessageToPlayer to save sender (26.x equivalent of processChatMessageInternal)
    @Inject(method = "showMessageToPlayer", at = @At("HEAD"))
    private void onShowMessageToPlayer(
            ChatType.Bound typeParameters,
            PlayerChatMessage signedMessage,
            Component content,
            GameProfile profile,
            boolean isSystem,
            Instant timestamp,
            CallbackInfoReturnable<Boolean> cir) {

        debugLog("linguachat.log.separator");
        debugLog("linguachat.log.method_called_26");
        debugLog("linguachat.log.separator");

        String messageContent = content.getString();
        debugLog("linguachat.log.message_content", messageContent);
        debugLog("linguachat.log.is_system", isSystem);

        // signed chat path - explicitly not a system message
        CURRENT_IS_SYSTEM.set(false);

        // Check for system messages
        if (messageContent.contains("получил достижение") ||
            messageContent.contains("выполнил достижение") ||
            messageContent.contains("разблокировал достижение") ||
            messageContent.contains("has made the advancement") ||
            messageContent.contains("earned the achievement") ||
            messageContent.contains("[System]") ||
            messageContent.contains("[CHAT]") ||
            isSystem) {

            debugLog("linguachat.log.debug.system_no_profile");
            CURRENT_SENDER.remove();
            return;
        }

        if (profile != null) {
            CURRENT_SENDER.set(profile);
            debugLog("linguachat.log.sender_profile_saved", profile.name());
        } else {
            CURRENT_SENDER.remove();
            debugLog("linguachat.log.sender_profile_not_determined");
        }
    }

    // System path keeps vanilla localization; disguised (plugin tellraw) is treated the same
    @Inject(method = "handleSystemMessage", at = @At("HEAD"))
    private void onHandleSystemMessage(Component message, boolean overlay, CallbackInfo ci) {
        CURRENT_IS_SYSTEM.set(true);
        // drop stale sender from the previous chat message - otherwise a system message
        // (e.g. plugin tellraw "<Shop> text") is treated as own chat and translated whole
        CURRENT_SENDER.remove();
    }

    @Inject(method = "handleDisguisedChatMessage", at = @At("HEAD"))
    private void onHandleDisguisedChatMessage(Component message, ChatType.Bound type, CallbackInfo ci) {
        CURRENT_IS_SYSTEM.set(true);
        CURRENT_SENDER.remove();
    }

    // Intercept addPlayerMessage(Component, MessageSignature, GuiMessageTag) for ChatComponent
    @Redirect(method = "*",
              at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"))
    private void redirectAddPlayerMessage(ChatComponent instance, Component message, MessageSignature signature, GuiMessageTag tag) {
        debugLog("linguachat.log.debug.intercept_add_message_meta_26");
        debugLog("linguachat.log.debug.original_text", message.getString());

        GameProfile sender = CURRENT_SENDER.get();

        // Check if we need to add hover effect for already translated message
        if (ModConfig.get().isShowOriginalOnHover()) {
            String senderName = extractSenderNameFromMessage(message.getString());
            String messageText = extractMessageText(message.getString(), senderName);

            if (messageText != null && senderName != null) {
                // Check if original exists in MessageStore
                String key = MessageStore.createMessageKey(senderName, messageText);
                String original = MessageStore.getOriginalMessage(key);

                if (original != null && !original.equals(messageText)) {
                    // This is a translated message, add hover effect with original
                    debugLog("linguachat.log.debug.hover_added", messageText, original);
                    Component messageWithHover = createMessageWithHover(message.getString(), original, messageText, message.getStyle());
                    instance.addPlayerMessage(messageWithHover, signature, tag);
                    return;
                }
            }
        }

        if (shouldTranslateMessage(sender, message)) {
            // Perform async translation with metadata preservation
            translateMessageAndAddWithMeta(instance, message, signature, tag);
        } else {
            // Just add original message
            instance.addPlayerMessage(message, signature, tag);
        }
    }

    // System messages: translate only if the text looks like player chat
    // (some servers/plugins send real chat as system messages)
    @Redirect(method = "*",
              at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/components/ChatComponent;addServerSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void redirectAddServerSystemMessage(ChatComponent instance, Component message) {
        if (shouldTranslateMessage(null, message)) {
            translateMessageAndAdd(instance, message, null);
        } else {
            instance.addServerSystemMessage(message);
        }
    }

    @Redirect(method = "*",
              at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/components/ChatComponent;addClientSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void redirectAddClientSystemMessage(ChatComponent instance, Component message) {
        if (shouldTranslateMessage(null, message)) {
            translateMessageAndAdd(instance, message, null);
        } else {
            instance.addClientSystemMessage(message);
        }
    }

    // Intercept setOverlayMessage in Hud
    @Redirect(method = "*",
              at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/Hud;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void redirectSetOverlayMessage(Hud instance, Component message, boolean tinted) {
        debugLog("linguachat.log.debug.intercept_overlay_26");
        debugLog("linguachat.log.debug.original_text", message.getString());

        // If overlay message translation is enabled
        if (shouldTranslateMessage(null, message)) {
            // Translate and display overlay message
            translateOverlayMessage(instance, message, tinted);
        } else {
            // Just display original message
            instance.setOverlayMessage(message, tinted);
        }
    }

    @Unique
    private boolean shouldTranslateMessage(GameProfile sender, Component message) {
        // system messages keep vanilla localization; translate only if text looks like
        // player chat (some servers/plugins send real chat as system messages)
        if (Boolean.TRUE.equals(CURRENT_IS_SYSTEM.get()) &&
            (message == null || extractPlayerNameFromMessage(message.getString()) == null)) {
            return false;
        }

        if (!ModConfig.get().isEnabled()) {
            return false;
        }

        boolean isOwnMessage = false;
        String senderName = null;
        String playerName = null;

        if (minecraft != null && minecraft.player != null && sender != null) {
            senderName = sender.name();
            playerName = minecraft.player.getName().getString();
            isOwnMessage = senderName.equalsIgnoreCase(playerName);
        }

        // Skip system messages (achievements, join/leave, etc)
        if (message != null) {
            String messageText = message.getString();

            // Check for internal system messages (including both English and Russian variants)
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
                debugLog("linguachat.log.system_message_detected");
                return false;
            }

            // Check for duplicate/already-translated messages
            if (sender != null) {
                String extractedPlayerName = extractPlayerName(messageText, sender);
                String extractedMessageText = extractMessageText(messageText, extractedPlayerName);

                if (extractedPlayerName != null && extractedMessageText != null) {
                    String key = MessageStore.createMessageKey(extractedPlayerName, extractedMessageText);
                    String original = MessageStore.getOriginalMessage(key);

                    if (original != null) {
                        debugLog("linguachat.log.message_already_translated", extractedPlayerName, extractedMessageText);
                        return false;
                    }

                    if (MessageStore.wasMessageRecentlyProcessed(extractedPlayerName, extractedMessageText)) {
                        debugLog("linguachat.log.message_recently_processed", extractedPlayerName, extractedMessageText);
                        return false;
                    }

                    String originalKey = MessageStore.createMessageKey(extractedPlayerName, extractedMessageText);
                    if (MessageStore.getOriginalMessage(originalKey) != null) {
                        debugLog("linguachat.log.original_in_cache", originalKey);
                        return false;
                    }

                    if (isOwnMessage) {
                        debugLog("linguachat.log.checking_own_message", extractedPlayerName);

                        MessageStore.markMessageAsProcessed(extractedPlayerName, extractedMessageText);
                    }
                }
            }
        }

        if (isOwnMessage) {
            boolean shouldTranslate = ModConfig.get().isTranslateOutgoing();
            if (ModConfig.get().isDebugMode()) {
                if (shouldTranslate) {
                    debugLog("linguachat.log.should_translate_yes_own");
                } else {
                    debugLog("linguachat.log.should_translate_no_own");
                }
            }
            return shouldTranslate;
        } else {
            boolean shouldTranslate = ModConfig.get().isTranslateIncoming();
            if (ModConfig.get().isDebugMode()) {
                if (shouldTranslate) {
                    debugLog("linguachat.log.should_translate_yes_other");
                } else {
                    debugLog("linguachat.log.should_translate_no_other");
                }
            }
            return shouldTranslate;
        }
    }

    // Method for async translation and adding message to chat
    @Unique
    private void translateMessageAndAdd(ChatComponent chatComponent, Component originalMessage, GameProfile sender) {
        // Determine player name from sender profile
        String senderName = extractSenderName(sender, originalMessage.getString());
        String messageText = extractMessageText(originalMessage.getString(), senderName);

        // If couldn't extract message text, just add original
        if (messageText == null) {
            chatComponent.addClientSystemMessage(originalMessage);
            return;
        }

        Component textToTranslate = TextCompat.literal(messageText);

        debugLog("linguachat.log.debug.translating_message", messageText);

        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();

                if (!translatedString.equals(messageText)) {
                    String originalString = originalMessage.getString();
                    Component newMessage = createMessageWithHover(originalString, messageText, translatedString, originalMessage.getStyle());

                    if (senderName != null) {
                        MessageStore.linkMessages(senderName, messageText, translatedString);
                    }

                    chatComponent.addClientSystemMessage(newMessage);
                } else {
                    chatComponent.addClientSystemMessage(originalMessage);
                }
            }
        );
    }

    // Method for async translation and adding message to chat with metadata
    @Unique
    private void translateMessageAndAddWithMeta(ChatComponent chatComponent, Component originalMessage,
                                        MessageSignature signature, GuiMessageTag tag) {
        // Extract player name from sender profile or message text
        String senderName = extractSenderName(CURRENT_SENDER.get(), originalMessage.getString());
        String messageText = extractMessageText(originalMessage.getString(), senderName);

        // If message text couldn't be extracted, add original
        if (messageText == null) {
            chatComponent.addPlayerMessage(originalMessage, signature, tag);
            return;
        }

        Component textToTranslate = TextCompat.literal(messageText);

        debugLog("linguachat.log.debug.translating_message_meta", messageText);

        // Perform async translation
        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();

                // Only translate if result differs from original
                if (!translatedString.equals(messageText)) {
                    // Create composite message with hover effect on translated text only
                    String originalString = originalMessage.getString();
                    Component newMessage = createMessageWithHover(originalString, messageText, translatedString, originalMessage.getStyle());

                    // Link original and translated messages in store
                    if (senderName != null) {
                        MessageStore.linkMessages(senderName, messageText, translatedString);
                    }

                    // Add message to chat with metadata preserved
                    chatComponent.addPlayerMessage(newMessage, signature, tag);
                } else {
                    // If translation is identical to original, use original message
                    chatComponent.addPlayerMessage(originalMessage, signature, tag);
                }
            }
        );
    }

    // Method for async translation and displaying overlay message
    @Unique
    private void translateOverlayMessage(Hud hud, Component originalMessage, boolean tinted) {
        String originalText = originalMessage.getString();

        // Check for empty message
        if (originalText.isEmpty()) {
            hud.setOverlayMessage(originalMessage, tinted);
            return;
        }

        // Perform translation
        LinguaChatMod.getTranslationManager().translateAsync(
            originalMessage,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();

                // Translate only if got different text
                if (!translatedString.equals(originalText)) {
                    // Create message with hover effect
                    Component newMessage;

                    // Add hover effect with original text if needed
                    if (ModConfig.get().isShowOriginalOnHover()) {
                        HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                            TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalText))
                        );
                        newMessage = TextCompat.literal(translatedString).withStyle(style -> style.withHoverEvent(hoverEvent));
                    } else {
                        newMessage = TextCompat.literal(translatedString).setStyle(originalMessage.getStyle());
                    }

                    // Display translated message
                    hud.setOverlayMessage(newMessage, tinted);
                } else {
                    // If translation is identical to original, use original message
                    hud.setOverlayMessage(originalMessage, tinted);
                }
            }
        );
    }

    // Helper method for creating message with hover effect only on translated text
    @Unique
    private Component createMessageWithHover(String originalString, String originalMessage, String translatedMessage, Style baseStyle) {
        // Determine prefix (player name with formatting)
        String prefix = "";

        // Check standard chat format <player> message
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(originalString);
        if (chatMatcher.find()) {
            String playerName = chatMatcher.group(1);
            prefix = "<" + playerName + "> ";
        } else {
            // Check extended message format
            Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(originalString);
            if (extendedMatcher.find()) {
                String playerName = null;
                // Search for player name in groups 1-4
                for (int i = 1; i <= 4; i++) {
                    if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                        playerName = extendedMatcher.group(i);
                        break;
                    }
                }

                if (playerName != null) {
                    // Restore prefix format
                    if (originalString.startsWith("<")) {
                        prefix = "<" + playerName + "> ";
                    } else if (originalString.startsWith("[")) {
                        prefix = "[" + playerName + "] ";
                    } else if (originalString.startsWith("(")) {
                        prefix = "(" + playerName + ") ";
                    } else if (originalString.contains(": ")) {
                        prefix = playerName + ": ";
                    }
                }
            }
        }

        // Create composite message
        MutableComponent result;

        if (!prefix.isEmpty()) {
            // Create prefix without hover effect
            result = TextCompat.literal(prefix).setStyle(baseStyle);

            // Add translated text with hover effect
            if (ModConfig.get().isShowOriginalOnHover()) {
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalMessage))
                );
                result.append(TextCompat.literal(translatedMessage).withStyle(style -> baseStyle.withHoverEvent(hoverEvent)));
            } else {
                result.append(TextCompat.literal(translatedMessage).setStyle(baseStyle));
            }
        } else {
            // If prefix not found, apply hover to entire message
            if (ModConfig.get().isShowOriginalOnHover()) {
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalMessage))
                );
                result = TextCompat.literal(translatedMessage).withStyle(style -> baseStyle.withHoverEvent(hoverEvent));
            } else {
                result = TextCompat.literal(translatedMessage).setStyle(baseStyle);
            }
        }

        return result;
    }

    // Helper method for extracting sender name from message
    @Unique
    private String extractSenderName(GameProfile sender, String messageText) {
        // If sender profile exists, use it
        if (sender != null) {
            return sender.name();
        }

        // Try to extract name from message format
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(messageText);
        if (chatMatcher.find()) {
            return chatMatcher.group(1);
        }

        // Check extended message format
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(messageText);
        if (extendedMatcher.find()) {
            // Search for player name in groups 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                    return extendedMatcher.group(i);
                }
            }
        }

        // If special format not detected, couldn't determine sender name
        return null;
    }

    // Helper method for extracting message text, excluding sender name
    @Unique
    private String extractMessageText(String fullMessage, String senderName) {
        if (senderName == null) {
            // If sender name not determined, return entire message
            return fullMessage;
        }

        // Check standard chat format <player> message
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(fullMessage);
        if (chatMatcher.find() && chatMatcher.group(1).equals(senderName)) {
            return chatMatcher.group(2);
        }

        // Check extended message format
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(fullMessage);
        if (extendedMatcher.find()) {
            // Search for player name in groups 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null &&
                    !extendedMatcher.group(i).isEmpty() &&
                    extendedMatcher.group(i).equals(senderName)) {
                    // Group 5 contains message text
                    return extendedMatcher.group(5);
                }
            }
        }

        // Check format PlayerName: message
        String prefix = senderName + ": ";
        if (fullMessage.startsWith(prefix)) {
            return fullMessage.substring(prefix.length());
        }

        // If special format not detected, return entire message
        return fullMessage;
    }

    // Helper method for extracting player name from message
    @Unique
    private String extractPlayerName(String messageText, GameProfile sender) {
        // Use sender profile name if available
        if (sender != null) {
            return sender.name();
        }

        return extractPlayerNameFromMessage(messageText);
    }

    // Helper method for extracting player name from message text
    @Unique
    private String extractPlayerNameFromMessage(String messageText) {
        // Try to extract name from message format
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(messageText);
        if (chatMatcher.find()) {
            return chatMatcher.group(1);
        }

        // Check extended message format
        Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(messageText);
        if (extendedMatcher.find()) {
            // Search for player name in groups 1-4
            for (int i = 1; i <= 4; i++) {
                if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                    return extendedMatcher.group(i);
                }
            }
        }

        // If message contains colon, try to extract name from beginning
        int colonIndex = messageText.indexOf(": ");
        if (colonIndex > 0) {
            return messageText.substring(0, colonIndex);
        }

        // Couldn't determine sender name
        return null;
    }

    // Helper method for extracting sender name (profile first, then message text)
    @Unique
    private String extractSenderNameFromMessage(String messageText) {
        GameProfile sender = CURRENT_SENDER.get();
        if (sender != null) {
            return sender.name();
        }

        return extractPlayerNameFromMessage(messageText);
    }
}
