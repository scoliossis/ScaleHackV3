package com.github.scoliossis.utils;

import com.github.scoliossis.Main;
import net.minecraft.util.ChatComponentText;

public class ChatUtil {

    public static void chat(Object message) {
        // i dont have a "§" on my keyboard, easier to type & if i want a color code.
        if (C.isInGame()) C.p().addChatMessage(new ChatComponentText((""+message).replaceAll("&", "§")));
    }

    public static void prefixMessage(Object message) {
        if (C.isInGame()) C.p().addChatMessage(new ChatComponentText(("&c[&f"+Main.MOD_NAME+"&c] " + message).replaceAll("&", "§")));
    }
}