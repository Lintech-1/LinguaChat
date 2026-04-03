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
import com.linguachat.compat.TextCompat;
import com.linguachat.config.ModConfig;
import com.linguachat.compat.I18nCompat;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.HoverEvent;

//? if >=1.19.2 && <1.19.3 {
/*import net.minecraft.client.network.PlayerListEntry;
*///?} else if >=1.19 && <1.19.2 {
/*import net.minecraft.network.encryption.PlayerPublicKey;
*///?}

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
    
    // ThreadLocal stores sender info per-thread to avoid race conditions
    //? if >=1.19.3 {
    @Unique private static final ThreadLocal<GameProfile> CURRENT_SENDER = new ThreadLocal<>();
    //?} else if >=1.19.2 && <1.19.3 {
    /*@Unique private static final ThreadLocal<PlayerListEntry> CURRENT_SENDER_ENTRY = new ThreadLocal<>();
    *///?} else {
    /*@Unique private static final ThreadLocal<PlayerPublicKey> CURRENT_SENDER_KEY = new ThreadLocal<>();
    *///?}
    
    @Unique
    private boolean shouldTranslateMessage(GameProfile sender) {
        return shouldTranslateMessage(sender, null);
    }
    
    //? if >=1.19.2 && <1.19.3 {
    /*@Unique
    private boolean shouldTranslateMessage(PlayerListEntry senderEntry) {
        return shouldTranslateMessage(senderEntry, null);
    }
    *///?} else if >=1.19 && <1.19.2 {
    /*@Unique
    private boolean shouldTranslateMessage(PlayerPublicKey senderKey) {
        return shouldTranslateMessage(senderKey, null);
    }
    *///?}
    
    //? if >=1.19.2 && <1.19.3 {
    /*@Unique
    private boolean shouldTranslateMessage(PlayerListEntry senderEntry, Text message) {
        // If translation is disabled in settings, don't translate
        if (!ModConfig.get().isEnabled()) {
            return false;
        }

        // Determine if message is own
        boolean isOwnMessage = false;
        String senderName = null;
        String playerName = null;

        if (client != null && client.player != null && senderEntry != null) {
            GameProfile profile = senderEntry.getProfile();
            if (profile != null) {
                senderName = profile.getName();
                playerName = client.player.getName().getString();
                isOwnMessage = senderName.equalsIgnoreCase(playerName);
            }
        }

        // Check for internal system messages
        if (message != null) {
            String messageText = message.getString();

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
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.system_message_detected"));
                return false;
            }
        }

        // Check message type
        if (isOwnMessage) {
            return ModConfig.get().isTranslateOutgoing();
        } else {
            return ModConfig.get().isTranslateIncoming();
        }
    }
    *///?} else if >=1.19 && <1.19.2 {
    /*@Unique
    private boolean shouldTranslateMessage(PlayerPublicKey senderKey, Text message) {
        // If translation is disabled in settings, don't translate
        if (!ModConfig.get().isEnabled()) {
            return false;
        }

        // For 1.19.2 we cannot determine sender from PlayerPublicKey directly
        // So we rely on extracting name from message text
        boolean isOwnMessage = false;

        if (client != null && client.player != null && message != null) {
            String messageText = message.getString();
            String extractedPlayerName = extractPlayerNameFromMessage(messageText);
            String playerName = client.player.getName().getString();

            if (extractedPlayerName != null) {
                isOwnMessage = extractedPlayerName.equalsIgnoreCase(playerName);
            }
        }

        // Check for internal system messages
        if (message != null) {
            String messageText = message.getString();

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
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.system_message_detected"));
                return false;
            }
        }

        // Check message type
        if (isOwnMessage) {
            return ModConfig.get().isTranslateOutgoing();
        } else {
            return ModConfig.get().isTranslateIncoming();
        }
    }
    *///?}
    
    @Unique
    private boolean shouldTranslateMessage(GameProfile sender, Text message) {
        if (!ModConfig.get().isEnabled()) {
            return false;
        }
        
        boolean isOwnMessage = false;
        String senderName = null;
        String playerName = null;
        
        if (client != null && client.player != null && sender != null) {
            //? if >=1.21.11 {
            senderName = sender.name();
            //?} else {
            /*senderName = sender.getName();
            *///?}
            playerName = client.player.getName().getString();
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
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.system_message_detected"));
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
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.message_already_translated", 
                                                    extractedPlayerName, extractedMessageText));
                        return false;
                    }
                    
                    if (MessageStore.wasMessageRecentlyProcessed(extractedPlayerName, extractedMessageText)) {
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.message_recently_processed", 
                                                    extractedPlayerName, extractedMessageText));
                        return false;
                    }
                    
                    String originalKey = MessageStore.createMessageKey(extractedPlayerName, extractedMessageText);
                    if (MessageStore.getOriginalMessage(originalKey) != null) {
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.original_in_cache", originalKey));
                        return false;
                    }
                    
                    if (isOwnMessage) {
                        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.checking_own_message", 
                                                   extractedPlayerName));
                        
                        MessageStore.markMessageAsProcessed(extractedPlayerName, extractedMessageText);
                    }
                }
            }
        }
        
        if (isOwnMessage) {
            boolean shouldTranslate = ModConfig.get().isTranslateOutgoing();
            if (DEBUG) {
                if (shouldTranslate) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.should_translate_yes_own"));
                } else {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.should_translate_no_own"));
                }
            }
            return shouldTranslate;
        } else {
            boolean shouldTranslate = ModConfig.get().isTranslateIncoming();
            if (DEBUG) {
                if (shouldTranslate) {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.should_translate_yes_other"));
                } else {
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.should_translate_no_other"));
                }
            }
            return shouldTranslate;
        }
    }
    
    // Intercept processChatMessageInternal to save sender
    //? if >=1.19.3 {
    @Inject(method = "processChatMessageInternal", at = @At("HEAD"))
    private void onProcessChatMessageInternal(
            MessageType.Parameters typeParameters,
            SignedMessage signedMessage,
            Text content,
            GameProfile profile,
            boolean isSystem,
            Instant timestamp,
            CallbackInfoReturnable<Boolean> cir) {
        
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.separator"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.method_called_1_19_3"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.separator"));
        
        String messageContent = content.getString();
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.message_content", messageContent));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.is_system", isSystem));

        // Check for system messages
        if (messageContent.contains("получил достижение") ||  
            messageContent.contains("выполнил достижение") ||  
            messageContent.contains("разблокировал достижение") ||
            messageContent.contains("has made the advancement") ||
            messageContent.contains("earned the achievement") ||
            messageContent.contains("[System]") ||
            messageContent.contains("[CHAT]") ||
            isSystem) {
            
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.system_no_profile"));
            CURRENT_SENDER.remove();
            return;
        }
        
        if (profile != null) {
            CURRENT_SENDER.set(profile);
            //? if >=1.21.11 {
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_profile_saved", profile.name()));
            //?} else {
            /*LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_profile_saved", profile.getName()));
            *///?}
        } else {
            CURRENT_SENDER.remove();
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_profile_not_determined"));
        }
    }
    //?} else if >=1.19.2 && <1.19.3 {
    /*@Inject(method = "processChatMessageInternal", at = @At("HEAD"))
    private void onProcessChatMessageInternal(
            MessageType.Parameters typeParameters,
            SignedMessage signedMessage,
            Text content,
            PlayerListEntry senderEntry,
            boolean isSystem,
            Instant timestamp,
            CallbackInfoReturnable<Boolean> cir) {
        
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.separator"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.method_called_1_19_2"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.separator"));

        String messageContent = content.getString();
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.message_content", messageContent));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.is_system", isSystem));

        // Check for system messages
        if (messageContent.contains("получил достижение") ||
            messageContent.contains("выполнил достижение") ||
            messageContent.contains("разблокировал достижение") ||
            messageContent.contains("has made the advancement") ||
            messageContent.contains("earned the achievement") ||
            messageContent.contains("[System]") ||
            messageContent.contains("[CHAT]") ||
            isSystem) {

            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.system_no_entry"));
            CURRENT_SENDER_ENTRY.remove();
            return;
        }

        if (senderEntry != null) {
            CURRENT_SENDER_ENTRY.set(senderEntry);
            GameProfile profile = senderEntry.getProfile();
            if (profile != null) {
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_entry_saved", profile.getName()));
            } else {
                LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_entry_no_profile"));
            }
        } else {
            CURRENT_SENDER_ENTRY.remove();
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_entry_not_determined"));
        }
    }
    *///?} else {
    /*@Inject(method = "processChatMessageInternal", at = @At("HEAD"))
    private void onProcessChatMessageInternal(
            MessageType.Parameters typeParameters,
            SignedMessage signedMessage,
            Text content,
            PlayerPublicKey senderPublicKey,
            boolean isSystem,
            Instant timestamp,
            CallbackInfoReturnable<Boolean> cir) {

        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.separator"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.method_called_1_19_2"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.separator"));

        String messageContent = content.getString();
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.message_content", messageContent));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.is_system", isSystem));

        // Check for system messages 
        if (messageContent.contains("получил достижение") ||  
            messageContent.contains("выполнил достижение") ||   
            messageContent.contains("разблокировал достижение") || 
            messageContent.contains("has made the advancement") ||
            messageContent.contains("earned the achievement") ||
            messageContent.contains("[System]") ||
            messageContent.contains("[CHAT]") ||
            isSystem) {

            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.system_no_key"));
            CURRENT_SENDER_KEY.remove();
            return;
        }

        if (senderPublicKey != null) {
            CURRENT_SENDER_KEY.set(senderPublicKey);
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_key_saved"));
        } else {
            CURRENT_SENDER_KEY.remove();
            LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.sender_key_not_determined"));
        }
    }
    *///?}
    
    // Intercept addMessage(Text) for ChatHud
    @Redirect(method = "*", 
              at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V"))
    private void redirectAddMessage(ChatHud instance, Text message) {
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.intercept_add_message"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.original_text", message.getString()));
        
        // Get current sender (depends on version)
        //? if >=1.19.3 {
        GameProfile sender = CURRENT_SENDER.get();
        //?} else if >=1.19.2 && <1.19.3 {
        /*PlayerListEntry senderEntry = CURRENT_SENDER_ENTRY.get();
        GameProfile sender = (senderEntry != null) ? senderEntry.getProfile() : null;
        *///?} else {
        /*GameProfile sender = null; // In 1.19.0-1.19.1 there's no GameProfile, extract name from message
        *///?}
        
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
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.hover_added", messageText, original));
                    Text messageWithHover = createMessageWithHover(message.getString(), original, messageText, message.getStyle());
                    instance.addMessage(messageWithHover);
                    return;
                }
            }
        }
        
        //? if >=1.19.3 {
        if (shouldTranslateMessage(sender, message)) {
            // Perform async translation
            translateMessageAndAdd(instance, message, sender);
        } else {
            // Just add original message
            instance.addMessage(message);
        }
        //?} else if >=1.19.2 && <1.19.3 {
        /*if (shouldTranslateMessage(CURRENT_SENDER_ENTRY.get(), message)) {
            // Perform async translation
            translateMessageAndAdd(instance, message, sender);
        } else {
            // Just add original message
            instance.addMessage(message);
        }
        *///?} else {
        /*if (shouldTranslateMessage(CURRENT_SENDER_KEY.get(), message)) {
            // Perform async translation
            translateMessageAndAdd(instance, message, null);
        } else {
            // Just add original message
            instance.addMessage(message);
        }
        *///?}
    }
    
    // Intercept addMessage(Text, MessageSignatureData, MessageIndicator) for ChatHud
    @Redirect(method = "*", 
              at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"))
    private void redirectAddMessageWithMeta(ChatHud instance, Text message, MessageSignatureData messageSignatureData, MessageIndicator indicator) {
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.intercept_add_message_meta"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.original_text", message.getString()));
        
        // Get current sender (depends on version)
        //? if >=1.19.3 {
        GameProfile sender = CURRENT_SENDER.get();
        //?} else if >=1.19.2 && <1.19.3 {
        /*PlayerListEntry senderEntry = CURRENT_SENDER_ENTRY.get();
        GameProfile sender = (senderEntry != null) ? senderEntry.getProfile() : null;
        *///?} else {
        /*GameProfile sender = null; // In 1.19.0-1.19.1 there's no GameProfile
        *///?}
        
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
                    LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.hover_added", messageText, original));
                    Text messageWithHover = createMessageWithHover(message.getString(), original, messageText, message.getStyle());
                    instance.addMessage(messageWithHover, messageSignatureData, indicator);
                    return;
                }
            }
        }
        
        //? if >=1.19.3 {
        if (shouldTranslateMessage(sender, message)) {
            // Perform async translation with metadata preservation
            translateMessageAndAddWithMeta(instance, message, messageSignatureData, indicator);
        } else {
            // Just add original message
            instance.addMessage(message, messageSignatureData, indicator);
        }
        //?} else if >=1.19.2 && <1.19.3 {
        /*if (shouldTranslateMessage(CURRENT_SENDER_ENTRY.get(), message)) {
            // Perform async translation with metadata preservation
            translateMessageAndAddWithMeta(instance, message, messageSignatureData, indicator);
        } else {
            // Just add original message
            instance.addMessage(message, messageSignatureData, indicator);
        }
        *///?} else {
        /*if (shouldTranslateMessage(CURRENT_SENDER_KEY.get(), message)) {
            // Perform async translation with metadata preservation
            translateMessageAndAddWithMeta(instance, message, messageSignatureData, indicator);
        } else {
            // Just add original message
            instance.addMessage(message, messageSignatureData, indicator);
        }
        *///?}
    }
    
    // Intercept setOverlayMessage in InGameHud
    @Redirect(method = "*", 
              at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"))
    private void redirectSetOverlayMessage(InGameHud instance, Text message, boolean tinted) {
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.intercept_overlay"));
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.original_text", message.getString()));
        
        // If overlay message translation is enabled
        //? if >=1.19.3 {
        if (shouldTranslateMessage((GameProfile) null, message)) {
        //?} else if >=1.19.2 && <1.19.3 {
        /*if (shouldTranslateMessage((PlayerListEntry) null, message)) {
        *///?} else {
        /*if (shouldTranslateMessage((PlayerPublicKey) null, message)) {
        *///?}
            // Translate and display overlay message
            translateOverlayMessage(instance, message, tinted);
        } else {
            // Just display original message
            instance.setOverlayMessage(message, tinted);
        }
    }
    
    // Method for async translation and adding message to chat
    @Unique
    private void translateMessageAndAdd(ChatHud chatHud, Text originalMessage, GameProfile sender) {
        // Determine player name from sender profile
        String senderName = extractSenderName(sender, originalMessage.getString());
        String messageText = extractMessageText(originalMessage.getString(), senderName);
        
        // If couldn't extract message text, just add original
        if (messageText == null) {
            chatHud.addMessage(originalMessage);
            return;
        }
        
        Text textToTranslate = TextCompat.literal(messageText);
        
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.translating_message", messageText));
        
        LinguaChatMod.getTranslationManager().translateAsync(
            textToTranslate,
            TranslationDirection.SERVER_TO_CLIENT,
            translatedText -> {
                String translatedString = translatedText.getString();
                
                if (!translatedString.equals(messageText)) {
                    String originalString = originalMessage.getString();
                    Text newMessage = createMessageWithHover(originalString, messageText, translatedString, originalMessage.getStyle());
                    
                    if (senderName != null) {
                        MessageStore.linkMessages(senderName, messageText, translatedString);
                    }
                    
                    chatHud.addMessage(newMessage);
                } else {
                    chatHud.addMessage(originalMessage);
                }
            }
        );
    }
    
    // Method for async translation and adding message to chat with metadata
    @Unique
    private void translateMessageAndAddWithMeta(ChatHud chatHud, Text originalMessage,
                                        MessageSignatureData signature, MessageIndicator indicator) {
        // Extract player name from sender profile or message text
        //? if >=1.19.3 {
        String senderName = extractSenderName(CURRENT_SENDER.get(), originalMessage.getString());
        //?} else if >=1.19.2 && <1.19.3 {
        /*PlayerListEntry senderEntry = CURRENT_SENDER_ENTRY.get();
        GameProfile sender = (senderEntry != null) ? senderEntry.getProfile() : null;
        String senderName = extractSenderName(sender, originalMessage.getString());
        *///?} else {
        /*String senderName = extractSenderNameFromMessage(originalMessage.getString());
        *///?}
        String messageText = extractMessageText(originalMessage.getString(), senderName);

        // If message text couldn't be extracted, add original
        if (messageText == null) {
            chatHud.addMessage(originalMessage, signature, indicator);
            return;
        }
        
        Text textToTranslate = TextCompat.literal(messageText);

        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.debug.translating_message_meta", messageText));

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
                    Text newMessage = createMessageWithHover(originalString, messageText, translatedString, originalMessage.getStyle());

                    // Link original and translated messages in store
                    if (senderName != null) {
                        MessageStore.linkMessages(senderName, messageText, translatedString);
                    }

                    // Add message to chat with metadata preserved
                    chatHud.addMessage(newMessage, signature, indicator);
                } else {
                    // If translation is identical to original, use original message
                    chatHud.addMessage(originalMessage, signature, indicator);
                }
            }
        );
    }
    
    // Method for async translation and displaying overlay message
    @Unique
    private void translateOverlayMessage(InGameHud hud, Text originalMessage, boolean tinted) {
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
                    Text newMessage;
                    
                    // Add hover effect with original text if needed
                    if (ModConfig.get().isShowOriginalOnHover()) {
                        HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                            TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalText))
                        );
                        newMessage = TextCompat.literal(translatedString).styled(style -> style.withHoverEvent(hoverEvent));
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
    
    // Helper method for formatting translated message
    @Unique
    private String formatTranslatedMessage(String originalString, String originalMessage, String translatedMessage) {
        // If original message is part of string, replace it with translation
        if (originalString.contains(originalMessage)) {
            return originalString.replace(originalMessage, translatedMessage);
        }
        
        // Check standard chat format <player> message
        Matcher chatMatcher = PLAYER_MESSAGE_PATTERN.matcher(originalString);
        if (chatMatcher.find()) {
            String playerName = chatMatcher.group(1);
            return "<" + playerName + "> " + translatedMessage;
        }
        
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
                // Restore format
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
        
        // If special format not detected, just return translated message
        return translatedMessage;
    }
    
    // Helper method for creating message with hover effect only on translated text
    @Unique
    private Text createMessageWithHover(String originalString, String originalMessage, String translatedMessage, Style baseStyle) {
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
        MutableText result;
        
        if (!prefix.isEmpty()) {
            // Create prefix without hover effect
            result = TextCompat.literal(prefix).setStyle(baseStyle);
            
            // Add translated text with hover effect
            if (ModConfig.get().isShowOriginalOnHover()) {
                HoverEvent hoverEvent = TextCompat.createShowTextHoverEvent(
                    TextCompat.literal(I18nCompat.translate("linguachat.hover.original", originalMessage))
                );
                result.append(TextCompat.literal(translatedMessage).styled(style -> style.withHoverEvent(hoverEvent)));
            } else {
                result.append(TextCompat.literal(translatedMessage));
            }
        } else {
            // If prefix not found, apply hover to entire message
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
    
    // Helper method for extracting sender name from message
    @Unique
    private String extractSenderName(GameProfile sender, String messageText) {
        // If sender profile exists, use it
        if (sender != null) {
            //? if >=1.21.11 {
            return sender.name();
            //?} else {
            /*return sender.getName();
            *///?}
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
            //? if >=1.21.11 {
            return sender.name();
            //?} else {
            /*return sender.getName();
            *///?}
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
    
    // Helper method for extracting sender name (version-aware)
    @Unique
    private String extractSenderNameFromMessage(String messageText) {
        //? if >=1.19.3 {
        GameProfile sender = CURRENT_SENDER.get();
        if (sender != null) {
            //? if >=1.21.11 {
            return sender.name();
            //?} else {
            /*return sender.getName();
            *///?}
        }
        //?}
        
        return extractPlayerNameFromMessage(messageText);
    }
} 