package com.linguachat.compat;

//? if >=1.19 {
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import net.minecraft.text.HoverEvent;
//?} else {
/*import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import net.minecraft.text.HoverEvent;
*///?}

/**
 * Compatibility layer for Text API across different Minecraft versions.
 * 
 * In Minecraft 1.19+, Text uses static factory methods (Text.literal, Text.translatable).
 * In older versions (<1.19), you need to use constructors (new LiteralText, new TranslatableText).
 */
public class TextCompat {
    
    /**
     * Creates a literal text component from a string.
     * 
     * @param text The text content
     * @return A MutableText component containing the literal text
     */
    public static MutableText literal(String text) {
        //? if >=1.19 {
        return Text.literal(text);
        //?} else {
        /*return new LiteralText(text);
        *///?}
    }
    
    /**
     * Creates a translatable text component with a translation key.
     * 
     * @param key The translation key
     * @return A MutableText component with the translation
     */
    public static MutableText translatable(String key) {
        //? if >=1.19 {
        return Text.translatable(key);
        //?} else {
        /*return new TranslatableText(key);
        *///?}
    }
    
    /**
     * Creates a translatable text component with a translation key and arguments.
     * 
     * @param key The translation key
     * @param args Arguments for the translation
     * @return A MutableText component with the translation
     */
    public static MutableText translatable(String key, Object... args) {
        //? if >=1.19 {
        return Text.translatable(key, args);
        //?} else {
        /*return new TranslatableText(key, args);
        *///?}
    }
    
    /**
     * Creates a text component with a specific style.
     * This method handles the API differences between versions.
     * 
     * @param text The text content
     * @param style The style to apply
     * @return A Text component with the applied style
     */
    public static Text literalWithStyle(String text, Style style) {
        return literal(text).setStyle(style);
    }
    
    /**
     * Creates a HoverEvent for showing text on hover.
     * Handles API differences across Minecraft versions.
     * 
     * In Minecraft 1.21.5+, HoverEvent is an interface with record implementations.
     * In older versions, HoverEvent is a class with constructors.
     * 
     * @param text The text to show in the hover tooltip
     * @return A HoverEvent configured to show the provided text
     */
    public static HoverEvent createShowTextHoverEvent(Text text) {
        //? if >=1.21.5 {
        return new HoverEvent.ShowText(text);
        //?} else {
        /*return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        *///?}
    }
}
