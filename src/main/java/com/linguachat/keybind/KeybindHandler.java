package com.linguachat.keybind;

import com.linguachat.compat.I18nCompat;
import com.linguachat.compat.ScreenCompat;
import com.linguachat.compat.TextCompat;
import com.linguachat.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class KeybindHandler {
    private static long lastToggleTime = 0;
    private static final long TOGGLE_COOLDOWN_MS = 500;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle outgoing translation toggle
            if (ModKeybinds.toggleOutboundTranslation.wasPressed()) {
                handleToggleOutbound(client);
            }

            // Handle config screen opening
            if (ModKeybinds.openConfigScreen.wasPressed()) {
                handleOpenConfig(client);
            }
        });
    }

    private static void handleToggleOutbound(MinecraftClient client) {
        long now = System.currentTimeMillis();
        if (now - lastToggleTime < TOGGLE_COOLDOWN_MS) {
            return; // Cooldown active
        }
        lastToggleTime = now;

        ModConfig config = ModConfig.get();
        boolean newState = !config.isTranslateOutgoing();
        config.setTranslateOutgoing(newState);

        // Send feedback message with i18n
        if (client.player != null) {
            String translationKey = newState
                ? "linguachat.feedback.outbound_enabled"
                : "linguachat.feedback.outbound_disabled";
            // bypass player.sendMessage - on 1.19.3+ it routes through MessageHandler,
            // where the mod's own mixin translates its feedback; direct add avoids the pipeline
            client.inGameHud.getChatHud().addMessage(I18nCompat.translatableText(translationKey));
        }
    }

    private static void handleOpenConfig(MinecraftClient client) {
        // Open config screen
        if (client.currentScreen == null) {
            client.execute(() -> {
                ScreenCompat.setScreen(client, new com.linguachat.gui.ConfigScreen(null));
            });
        }
    }
}
