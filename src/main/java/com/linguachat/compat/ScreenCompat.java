package com.linguachat.compat;

//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
*///?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
//?}

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
    //? if >=26.1 {
    //// gui.setScreen = the plain setScreen of the older versions; setScreenAndShow forces an
    //// extra immediate render frame on open/close which flickers the screen for 1-2 frames
    //public static void setScreen(Minecraft client, Screen screen) {
    //    client.gui.setScreen(screen);
    //}
    //?} else {
    public static void setScreen(MinecraftClient client, Screen screen) {
        //? if >=1.17 {
        client.setScreen(screen);
        //?} else {
        /*client.openScreen(screen);
        *///?}
    }
    //?}

    /**
     * Draws text with shadow using the appropriate API for the version.
     */
    //? if >=26.1 {
    //public static void drawTextWithShadow(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
    //    graphics.text(font, text, x, y, color, true);
    //}
    //?} else if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
    public static void drawTextWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }
    //?} else {
    /*public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, String text, int x, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, x, y, color);
    }
    *///?}

    /**
     * Fills a rectangle; argument order (x1, y1, x2, y2, color) is the same across all versions.
     */
    //? if >=26.1 {
    //public static void fill(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
    //    graphics.fill(x1, y1, x2, y2, color);
    //}
    //?} else if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
    public static void fill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }
    //?} else {
    /*public static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
    }
    *///?}

    /**
     * Draws centered text using the appropriate API for the version.
     */
    //? if >=26.1 {
    //public static void drawCenteredText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int color) {
    //    graphics.centeredText(font, text, centerX, y, color);
    //}
    //?} else if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
    public static void drawCenteredText(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        context.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);
    }
    //?} else {
    /*public static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        textRenderer.drawWithShadow(matrices, text.getString(), centerX - textRenderer.getWidth(text.getString()) / 2, y, color);
    }
    *///?}
}
