package com.linguachat.keybind;

import com.linguachat.compat.KeyBindingCompat;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
//? if >=1.21.9 {
import net.minecraft.util.Identifier;
//?}

public class ModKeybinds {
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
}
