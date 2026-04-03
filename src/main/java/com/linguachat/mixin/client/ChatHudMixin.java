package com.linguachat.mixin.client;

import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.linguachat.LinguaChatMod;
import com.linguachat.config.ModConfig;
import com.linguachat.util.MessageBlocker;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
@SuppressWarnings({"ClassWithoutNoArgConstructor", "ClassHasNoToStringMethod", "MissingClassJavaDoc", "NonStaticInnerClassInSecureContext", "MixinClassInNonMixinPackage", "StaticMixinClass"})
public class ChatHudMixin {
    // Weak refs so GC can clean up old messages
    private static final WeakHashMap<Text, Boolean> recentlyTranslated = new WeakHashMap<>();
    
    static {
        LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.debug.chathud_mixin_loaded"));
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {
        if (message == null) {
            return;
        }
        
        String originalText = message.getString();
        
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.debug.chathud_add_message", originalText));
        }
        
        if (MessageBlocker.isTranslated(message)) {
            if (ModConfig.get().isDebugMode()) {
                LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.debug.translated_skip"));
            }
            return;
        }
        
        if (MessageBlocker.isBlocked(originalText)) {
            if (ModConfig.get().isDebugMode()) {
                LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.debug.message_blocked"));
            }
            MessageBlocker.unblockMessage(originalText);
            ci.cancel();
            return;
        }
        
        if (recentlyTranslated.containsKey(message)) {
            if (ModConfig.get().isDebugMode()) {
                LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.debug.already_translated"));
            }
            return;
        }

        if (!ModConfig.get().isEnabled()) {
            return;
        }

        if (!ModConfig.get().isTranslateIncoming()) {
            return;
        }
        
        // Fallback if ClientPlayNetworkHandlerMixin didn't catch it
        if (ModConfig.get().isDebugMode()) {
            LinguaChatMod.LOGGER.info(com.linguachat.compat.I18nCompat.translate("linguachat.log.debug.fallback_processing"));
        }
    }
} 