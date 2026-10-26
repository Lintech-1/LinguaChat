package com.linguachat.compat;

//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
//?}

/**
 * Compatibility layer for KeyBinding registration across different Minecraft versions.
 *
 * KeyBindingHelper.registerKeyBinding() is available in all Fabric API versions we support,
 * so this is a simple wrapper for consistency with other compat layers.
 */
public class KeyBindingCompat {

    //? if >=26.1 {
    //public static KeyMapping register(KeyMapping keyMapping) {
    //    return KeyMappingHelper.registerKeyMapping(keyMapping);
    //}
    //?} else {
    public static KeyBinding register(KeyBinding keyBinding) {
        return KeyBindingHelper.registerKeyBinding(keyBinding);
    }
    //?}
}
