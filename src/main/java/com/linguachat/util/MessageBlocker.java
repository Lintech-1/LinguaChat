package com.linguachat.util;

import java.util.WeakHashMap;
//? if >=26.1 {
/*import net.minecraft.network.chat.Component;
*///?} else {
import net.minecraft.text.Text;
//?}

/**
 * Utility for blocking messages from being added to chat
 */
public class MessageBlocker {
    private static final WeakHashMap<String, Boolean> blockedMessages = new WeakHashMap<>();
    //? if >=26.1 {
    //private static final WeakHashMap<Component, Boolean> translatedMessages = new WeakHashMap<>();
    //?} else {
    private static final WeakHashMap<Text, Boolean> translatedMessages = new WeakHashMap<>();
    //?}

    /**
     * Blocks message from being added to chat
     */
    public static void blockMessage(String messageText) {
        blockedMessages.put(messageText, Boolean.TRUE);
    }

    /**
     * Checks if message is blocked
     */
    public static boolean isBlocked(String messageText) {
        return blockedMessages.containsKey(messageText);
    }

    /**
     * Unblocks message
     */
    public static void unblockMessage(String messageText) {
        blockedMessages.remove(messageText);
    }

    /**
     * Marks message as translated (don't block)
     */
    //? if >=26.1 {
    //public static void markAsTranslated(Component message) {
    //    translatedMessages.put(message, Boolean.TRUE);
    //}
    //
    //public static boolean isTranslated(Component message) {
    //    return translatedMessages.containsKey(message);
    //}
    //?} else {
    public static void markAsTranslated(Text message) {
        translatedMessages.put(message, Boolean.TRUE);
    }

    public static boolean isTranslated(Text message) {
        return translatedMessages.containsKey(message);
    }
    //?}
}
