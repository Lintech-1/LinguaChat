package com.linguachat.keybind;

import com.linguachat.compat.KeyBindingCompat;
//? if >=26.1 {
/*import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
//? if >=1.21.9 {
import net.minecraft.util.Identifier;
//?}
//?}

public class ModKeybinds {
    //? if >=26.1 {
    //public static KeyMapping toggleOutboundTranslation;
    //public static KeyMapping openConfigScreen;
    //
    //private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("linguachat", "main"));
    //
    //public static void register() {
    //    toggleOutboundTranslation = KeyBindingCompat.register(new KeyMapping(
    //        "key.linguachat.toggle_outbound",
    //        InputConstants.Type.KEYBOARD,
    //        InputConstants.KEY_N,
    //        CATEGORY
    //    ));
    //
    //    openConfigScreen = KeyBindingCompat.register(new KeyMapping(
    //        "key.linguachat.open_config",
    //        InputConstants.Type.KEYBOARD,
    //        InputConstants.KEY_K,
    //        CATEGORY
    //    ));
    //}
    //?} else {
    public static KeyBinding toggleOutboundTranslation;
    public static KeyBinding openConfigScreen;

    //? if >=1.21.9 {
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("linguachat", "main"));
    //?}

    public static void register() {
        toggleOutboundTranslation = KeyBindingCompat.register(new KeyBinding(
            "key.linguachat.toggle_outbound",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            //? if >=1.21.9 {
            CATEGORY
            //?} else {
            /*"category.linguachat"
            *///?}
        ));

        openConfigScreen = KeyBindingCompat.register(new KeyBinding(
            "key.linguachat.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            //? if >=1.21.9 {
            CATEGORY
            //?} else {
            /*"category.linguachat"
            *///?}
        ));
    }
    //?}
}
