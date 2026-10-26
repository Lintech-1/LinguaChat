package com.linguachat.compat;

//? if >=26.1 {
/*import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
*///?} else {
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
//?}

/**
 * Compatibility layer for ButtonWidget across different Minecraft versions.
 *
 * In 1.19+, ButtonWidget uses a builder pattern.
 * In older versions, it uses constructors directly.
 */
public class ButtonCompat {

    //? if >=26.1 {
    //public static Button create(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
    //    return Button.builder(message, onPress)
    //        .bounds(x, y, width, height)
    //        .build();
    //}
    //?} else {
    //? if >=1.20 {
    public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        return ButtonWidget.builder(message, onPress)
            .dimensions(x, y, width, height)
            .build();
    }
    //?} else {
    /*public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        return new ButtonWidget(x, y, width, height, message, onPress);
    }
    *///?}
    //?}
}
