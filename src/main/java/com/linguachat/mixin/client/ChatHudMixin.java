package com.linguachat.mixin.client;

import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.linguachat.LinguaChatMod;
import com.linguachat.config.ModConfig;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
@SuppressWarnings({"ClassWithoutNoArgConstructor", "ClassHasNoToStringMethod", "MissingClassJavaDoc", "NonStaticInnerClassInSecureContext", "MixinClassInNonMixinPackage", "StaticMixinClass"})
public class ChatHudMixin {
    // Используем WeakHashMap для хранения недавно переведенных текстов, чтобы не допустить утечки памяти
    private static final WeakHashMap<Text, Boolean> recentlyTranslated = new WeakHashMap<>();
    
    static {
        // Добавляем явную запись при загрузке класса
        LinguaChatMod.LOGGER.error("!!! МИКСИН ChatHudMixin ЗАГРУЖЕН !!!");
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {
        // Добавляем более заметные логи уровня ERROR для отладки
        LinguaChatMod.LOGGER.error("!!! ВЫЗОВ ChatHudMixin.onAddMessage !!!");
        
        // ВАЖНО: Если сообщение уже было обработано в ClientPlayNetworkHandlerMixin, 
        // то мы не должны его обрабатывать здесь повторно
        if (message == null) {
            return;
        }
        
        String originalText = message.getString();
        LinguaChatMod.LOGGER.error("Оригинальный текст: '" + originalText + "'");
        
        // Проверка: было ли это сообщение недавно переведено
        if (recentlyTranslated.containsKey(message)) {
            LinguaChatMod.LOGGER.error("Сообщение уже было переведено, пропускаем повторную обработку");
            return;
        }

        // Проверяем, включен ли мод и перевод входящих сообщений
        if (!ModConfig.get().isEnabled()) {
            LinguaChatMod.LOGGER.error("Мод отключен");
            return;
        }

        if (!ModConfig.get().isTranslateIncoming()) {
            LinguaChatMod.LOGGER.error("Перевод входящих сообщений отключен");
            return;
        }
        
        // Этот миксин теперь будет служить резервным механизмом,
        // если ClientPlayNetworkHandlerMixin не обработал сообщение
        // В большинстве случаев ClientPlayNetworkHandlerMixin должен обрабатывать сообщения
        
        // Этот код можно оставить для дебага, чтобы увидеть, какие сообщения попадают сюда
        LinguaChatMod.LOGGER.error("ChatHudMixin: резервная обработка сообщения");
    }
} 