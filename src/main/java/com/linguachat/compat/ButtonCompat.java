package com.linguachat.compat;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Compatibility layer for ButtonWidget across different Minecraft versions.
 * 
 * In 1.19+, ButtonWidget uses a builder pattern.
 * In older versions, it uses constructors directly.
 */
public class ButtonCompat {
    
    /**
     * Creates a button widget with the appropriate API for the version.
     * 
     * @param x X position
     * @param y Y position
     * @param width Button width
     * @param height Button height
     * @param message Button text
     * @param onPress Action to perform when pressed
     * @return A ButtonWidget instance
     */
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
}
