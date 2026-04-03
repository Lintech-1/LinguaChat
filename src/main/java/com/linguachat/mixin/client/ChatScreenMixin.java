package com.linguachat.mixin.client;

import com.linguachat.LinguaChatMod;
import com.linguachat.compat.ScreenCompat;
import com.linguachat.compat.TextCompat;
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
//? if <1.21 {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow
    protected TextFieldWidget chatField;

    // Prevents recursion when we send translated messages back through chat
    @Unique
    private static volatile boolean IS_PROCESSING = false;
    
    @Unique
    private ChatScreen thisChatScreen = (ChatScreen)(Object)this;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        LinguaChatMod.LOGGER.info("ChatScreen initialized");
    }

    //? if >=1.19 {
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    //? if >=1.21 {
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
    //?} else {
    /*private void onSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
    *///?}
    //?} else {
    /*@Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
    *///?}
        // Skip if already translating, mod disabled, or it's a command
        if (IS_PROCESSING || !ModConfig.get().isEnabled() || !ModConfig.get().isTranslateOutgoing() || chatText.startsWith("/")) {
            return;
        }
        
        IS_PROCESSING = true;
        
        try {
            // Cancel vanilla send so we can translate first
            //? if >=1.21 {
            ci.cancel();
            //?} else {
            /*cir.cancel();
            *///?}
            
            String playerName = MinecraftClient.getInstance().getSession().getUsername();
            String originalMessage = chatText;
            
            LinguaChatMod.LOGGER.info("Original message: " + originalMessage);
            
            // Avoid retranslating if we just sent this
            if (MessageStore.wasMessageRecentlyProcessed(playerName, originalMessage)) {
                LinguaChatMod.LOGGER.info("Message already processed, sending without retranslation");
                sendChatMessageDirectly(originalMessage);
                MinecraftClient.getInstance().execute(() -> {
                    closeScreenSafely(thisChatScreen);
                });
                return;
            }
            
            MessageStore.markMessageAsProcessed(playerName, originalMessage);
            
            String key = MessageStore.createMessageKey(playerName, originalMessage);
            MessageStore.storeOriginalMessage(key, originalMessage);
            
            // Close chat before translation starts (feels more responsive)
            MinecraftClient.getInstance().execute(() -> {
                closeScreenSafely(thisChatScreen);
            });
            
            LinguaChatMod.getTranslationManager().translateAsync(
                TextCompat.literal(originalMessage),
                TranslationDirection.CLIENT_TO_SERVER,
                translatedText -> {
                    try {
                        String translatedString = translatedText.getString();
                        
                        if (translatedText != null && !translatedString.equals(originalMessage)) {
                            MessageStore.markMessageAsProcessed(playerName, translatedString);
                            MessageStore.linkMessages(playerName, originalMessage, translatedString);
                            
                            LinguaChatMod.LOGGER.info("Translated outgoing message: " + translatedString);
                            
                            MinecraftClient.getInstance().execute(() -> {
                                if (ModConfig.get().isShowOriginalOnHover()) {
                                    LinguaChatMod.LOGGER.info("Original text '" + originalMessage + "' saved for hover effect");
                                }
                                sendChatMessageDirectly(translatedString);
                            });
                        } else {
                            LinguaChatMod.LOGGER.info("Translation didn't change message, sending original: " + originalMessage);
                            MinecraftClient.getInstance().execute(() -> {
                                sendChatMessageDirectly(originalMessage);
                            });
                        }
                    } catch (Exception e) {
                        LinguaChatMod.LOGGER.error("Error sending translated message", e);
                        MinecraftClient.getInstance().execute(() -> {
                            sendChatMessageDirectly(originalMessage);
                        });
                    }
                }
            );
        } finally {
            IS_PROCESSING = false;
        }
    }
    
    @Unique
    private static void sendChatMessageDirectly(String message) {
        try {
            //? if >=1.20 {
            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(message);
                LinguaChatMod.LOGGER.info("Message sent directly: " + message);
            } else {
                LinguaChatMod.LOGGER.error("Failed to send message, network handler unavailable");
            }
            //?} else if >=1.19 {
            /*if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendChatMessage(message, TextCompat.literal(message));
                LinguaChatMod.LOGGER.info("Message sent directly: " + message);
            } else {
                LinguaChatMod.LOGGER.error("Failed to send message, player unavailable");
            }
            *///?} else {
            /*if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendChatMessage(message);
                LinguaChatMod.LOGGER.info("Message sent directly: " + message);
            } else {
                LinguaChatMod.LOGGER.error("Failed to send message, player unavailable");
            }
            *///?}
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error("Error during direct message send", e);
        }
    }

    @Unique
    private static void closeScreenSafely(ChatScreen targetScreen) {
        // Only close if user hasn't switched screens already
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen == targetScreen) {
                ScreenCompat.setScreen(client, null);
                LinguaChatMod.LOGGER.info("Chat screen closed");
            } else {
                LinguaChatMod.LOGGER.info("Chat screen already changed, not closing");
            }
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error("Error closing chat screen", e);
        }
    }

    @Unique
    private static void closeScreen() {
        try {
            ScreenCompat.setScreen(MinecraftClient.getInstance(), null);
            LinguaChatMod.LOGGER.info("Chat screen closed");
        } catch (Exception e) {
            LinguaChatMod.LOGGER.error("Error closing chat screen", e);
        }
    }

    @Unique
    private static String getOriginalMessage(String translatedMessage, String playerName) {
        String key = MessageStore.createMessageKey(playerName, translatedMessage);
        return MessageStore.getOriginalMessage(key);
    }
} 