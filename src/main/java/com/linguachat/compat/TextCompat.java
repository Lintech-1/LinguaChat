package com.linguachat.compat;

//? if >=26.1 {
/*// Unobfuscated Minecraft: Mojang naming
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent;
*///?} else if >=1.19 {
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
 * In Minecraft 26.1+ the game is unobfuscated and uses Mojang naming (Component).
 */
public class TextCompat {

    //? if >=26.1 {
    //public static MutableComponent literal(String text) {
    //    return Component.literal(text);
    //}
    //
    //public static MutableComponent translatable(String key) {
    //    return Component.translatable(key);
    //}
    //
    //public static MutableComponent translatable(String key, Object... args) {
    //    return Component.translatable(key, args);
    //}
    //
    //public static Component literalWithStyle(String text, Style style) {
    //    return literal(text).setStyle(style);
    //}
    //
    //public static HoverEvent createShowTextHoverEvent(Component text) {
    //    return new HoverEvent.ShowText(text);
    //}
    //?} else {
    public static MutableText literal(String text) {
        //? if >=1.19 {
        return Text.literal(text);
        //?} else {
        /*return new LiteralText(text);
        *///?}
    }

    public static MutableText translatable(String key) {
        //? if >=1.19 {
        return Text.translatable(key);
        //?} else {
        /*return new TranslatableText(key);
        *///?}
    }

    public static MutableText translatable(String key, Object... args) {
        //? if >=1.19 {
        return Text.translatable(key, args);
        //?} else {
        /*return new TranslatableText(key, args);
        *///?}
    }

    public static Text literalWithStyle(String text, Style style) {
        return literal(text).setStyle(style);
    }

    public static HoverEvent createShowTextHoverEvent(Text text) {
        //? if >=1.21.5 {
        return new HoverEvent.ShowText(text);
        //?} else {
        /*return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        *///?}
    }
    //?}
}
