package com.linguachat;

import com.linguachat.compat.I18nCompat;
import com.linguachat.keybind.KeybindHandler;
import com.linguachat.keybind.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;

public class LinguaChatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.client_init"));

        // Fabric fires this client-side only, safe to register keybinds here
        ModKeybinds.register();
        KeybindHandler.register();

        LinguaChatMod.LOGGER.info(I18nCompat.translate("linguachat.log.client_init_done"));
    }
}
