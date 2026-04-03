package com.linguachat.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

/**
 * Compatibility layer for KeyBinding registration across different Minecraft versions.
 * 
 * KeyBindingHelper.registerKeyBinding() is available in all Fabric API versions we support,
 * so this is a simple wrapper for consistency with other compat layers.
 */
public class KeyBindingCompat {
    
    /**
     * Registers a keybinding with Fabric API.
     * 
     * @param keyBinding The KeyBinding to register
     * @return The registered KeyBinding
     */
    public static KeyBinding register(KeyBinding keyBinding) {
        return KeyBindingHelper.registerKeyBinding(keyBinding);
    }
}
