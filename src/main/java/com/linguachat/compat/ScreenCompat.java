package com.linguachat.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

/**
 * Compatibility layer for Screen and rendering API across different Minecraft versions.
 */
public class ScreenCompat {
    
    /**
     * Opens a screen in the Minecraft client.
     * In 1.17+, uses setScreen(). In older versions, uses openScreen().
     * 
     * @param client The Minecraft client instance
     * @param screen The screen to open (can be null to close current screen)
     */
    public static void setScreen(MinecraftClient client, Screen screen) {
        //? if >=1.17 {
        client.setScreen(screen);
        //?} else {
        /*client.openScreen(screen);
        *///?}
    }
    
    /**
     * Draws text with shadow using the appropriate API for the version.
     * 
     * @param matrices The matrix stack (or DrawContext for 1.20+)
     * @param textRenderer The text renderer
     * @param text The text to draw
     * @param x X coordinate
     * @param y Y coordinate
     * @param color Text color
     */
    //? if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
    public static void drawTextWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }
    //?} else {
    /*public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, String text, int x, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, x, y, color);
    }
    *///?}
    
    /**
     * Draws centered text using the appropriate API for the version.
     * 
     * @param matrices The matrix stack (or DrawContext for 1.20+)
     * @param textRenderer The text renderer
     * @param text The text to draw
     * @param centerX Center X coordinate
     * @param y Y coordinate
     * @param color Text color
     */
    //? if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
    public static void drawCenteredText(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        context.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);
    }
    //?} else {
    /*public static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        textRenderer.drawWithShadow(matrices, text.getString(), centerX - textRenderer.getWidth(text.getString()) / 2, y, color);
    }
    *///?}
}
