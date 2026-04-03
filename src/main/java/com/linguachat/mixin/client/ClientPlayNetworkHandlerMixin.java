package com.linguachat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.TextCompat;
import com.linguachat.compat.I18nCompat;
import com.linguachat.config.ModConfig;
import com.linguachat.translation.TranslationDirection;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
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
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.game_message"));
        }
        
        // Get message text
        //? if >=1.19 {
        final Text message = packet.content();
        //?} else {
        /*final Text message = packet.getMessage();
        *///?}
        final String originalText = message.getString();
        
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.network_message", originalText));
        }
        
        if (!ModConfig.get().isEnabled()) {
            return;
        }
        
        if (!ModConfig.get().isTranslateIncoming()) {
            return;
        }
        
        // Skip system messages (join/leave, achievements, etc)
        if (isSystemMessage(originalText)) {
            if (ModConfig.get().isDebugMode()) {
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.skip_system", originalText));
            }
            return;
        }
        
        // Try to parse player name and message from various formats
        String playerName = null;
        String messageText = null;
        
        Matcher standardMatcher = PLAYER_MESSAGE_PATTERN.matcher(originalText);
        if (standardMatcher.find()) {
            playerName = standardMatcher.group(1);
            messageText = standardMatcher.group(2);
            if (ModConfig.get().isDebugMode()) {
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.standard_format", playerName));
            }
        } else {
            Matcher extendedMatcher = EXTENDED_MESSAGE_PATTERN.matcher(originalText);
            if (extendedMatcher.find()) {
                // Try groups 1-4 for player name
                for (int i = 1; i <= 4; i++) {
                    if (extendedMatcher.group(i) != null && !extendedMatcher.group(i).isEmpty()) {
                        playerName = extendedMatcher.group(i);
                        break;
                    }
                }
                messageText = extendedMatcher.group(5);
                if (ModConfig.get().isDebugMode()) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.extended_format", playerName));
                }
            } else if (originalText.contains(": ")) {
                // Fallback: "PlayerName: message"
                int colonIndex = originalText.indexOf(": ");
                if (colonIndex > 0) {
                    playerName = originalText.substring(0, colonIndex);
                    messageText = originalText.substring(colonIndex + 2);
                    if (ModConfig.get().isDebugMode()) {
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.colon_format", playerName));
                    }
                }
            }
        }
        
        if (playerName == null || messageText == null || messageText.isEmpty()) {
            if (ModConfig.get().isDebugMode()) {
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.format_failed", originalText));
            }
            return;
        }
        
        // Skip own messages on multiplayer (already translated when sent)
        // In singleplayer, translate everything including own messages
        String currentPlayer = MinecraftClient.getInstance().getSession().getUsername();
        //? if >=1.18 {
        boolean isSingleplayer = MinecraftClient.getInstance().isIntegratedServerRunning();
        //?} else {
        /*boolean isSingleplayer = MinecraftClient.getInstance().isInSingleplayer();
        *///?}
        
        if (playerName.equals(currentPlayer)) {
            if (isSingleplayer) {
                if (ModConfig.get().isDebugMode()) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.own_singleplayer", currentPlayer));
                }
            } else {
                if (ModConfig.get().isDebugMode()) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.skip_own_multiplayer", currentPlayer));
                }
                return;
            }
        }
        
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.text_to_translate", messageText));
        }
        
        // Must be final for lambda
        final String finalPlayerName = playerName;
        final String finalMessageText = messageText;
        
        // Cancel vanilla message before async translation
        ci.cancel();
        
        com.linguachat.util.MessageBlocker.blockMessage(originalText);
        
        Text textToTranslate = TextCompat.literal(finalMessageText);
        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();
                if (ModConfig.get().isDebugMode()) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.translated_to", ModConfig.get().getDefaultTargetLang(), translatedString));
                }
                
                // Add hover effect only on translated text, not player name
                Text newMessage = createMessageWithHover(originalText, finalPlayerName, finalMessageText, translatedString, message.getStyle());
                
                com.linguachat.util.MessageBlocker.markAsTranslated(newMessage);
                
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(newMessage);
                if (ModConfig.get().isDebugMode()) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.message_added"));
                }
            }
        );
    }
    
    private Text createMessageWithHover(String originalString, String playerName, String originalMessage, String translatedMessage, Style baseStyle) {
        // Extract prefix (player name with brackets/formatting)
        String prefix = "";
        
        if (originalString.startsWith("<")) {
            prefix = "<" + playerName + "> ";
        } else if (originalString.startsWith("[")) {
            prefix = "[" + playerName + "] ";
        } else if (originalString.startsWith("(")) {
            prefix = "(" + playerName + ") ";
        } else if (originalString.contains(": ")) {
            prefix = playerName + ": ";
        } else {
            // Try to find prefix by locating message in original string
            int msgStart = originalString.indexOf(originalMessage);
            if (msgStart > 0) {
                prefix = originalString.substring(0, msgStart);
            }
        }
        
        net.minecraft.text.MutableText result;
        
        if (!prefix.isEmpty()) {
            result = TextCompat.literal(prefix).setStyle(baseStyle);
            
            if (ModConfig.get().isShowOriginalOnHover()) {
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalMessage))
                );
                result.append(TextCompat.literal(translatedMessage).styled(style -> style.withHoverEvent(hoverEvent)));
                if (ModConfig.get().isDebugMode()) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.composite_hover"));
                }
            } else {
                result.append(TextCompat.literal(translatedMessage));
            }
        } else {
            if (ModConfig.get().isShowOriginalOnHover()) {
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalMessage))
                );
                result = TextCompat.literal(translatedMessage).styled(style -> style.withHoverEvent(hoverEvent));
            } else {
                result = TextCompat.literal(translatedMessage).setStyle(baseStyle);
            }
        }
        
        return result;
    }
    
    private boolean isSystemMessage(String text) {
        return text.contains("joined the game") || 
               text.contains("left the game") ||
               text.contains("joined the game") ||
               text.contains("left the game") ||
               text.startsWith("[Server]") ||
               text.startsWith("[Server]") ||
               text.startsWith("/") ||
               text.startsWith("*") ||
               text.contains("[System]") ||
               text.contains("[CHAT]") ||
               text.contains("earned achievement") ||
               text.contains("completed achievement") ||
               text.contains("unlocked achievement") ||
               text.contains("has made the advancement") ||
               text.contains("earned the achievement");
    }
} 