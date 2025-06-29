package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;

import java.awt.*;

// the description of this module was almost https://www.youtube.com/watch?v=s4nzOAMMqW0
@RegisterModule(
        name = "Theme",
        description = "https://www.youtube.com/watch?v=8ptm5NuIthg",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class ThemeModule extends Module {
    @RegisterSubModule(name = "Theme Colour")
    public static RenderUtil.ThemeColours themeColour = RenderUtil.ThemeColours.Gay;

    @RegisterSubModule(name = "Replace Minecraft Font", description = "Replaces the minecraft font")
    public static boolean globalFont = false;

    @RegisterSubModule(name = "Minecraft Font Size", parent = "Replace Minecraft Font", min = 5, max = 20)
    public static int minecraftFontSize = 10;

    @RegisterSubModule(name = "Font")
    public static FontUtil.Fonts font = FontUtil.Fonts.Atkinson;

    private static FontUtil.Fonts previousFont = null;

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent event) {
        if (font != previousFont) {
            FontUtil.unloadFont();
            FontUtil.setFontName(font.name);
            FontUtil.loadFont(0);
            previousFont = font;
        }
    }

    public static Color[] getThemeColours() {
        return themeColour.colours;
    }

    // force reload font
    @Override
    protected void onEnable() {
        FontUtil.unloadFont();
        FontUtil.setFontName(font.name);
        FontUtil.loadFont(0);
        previousFont = font;
    }

    @Override
    protected void onDisable() {
        ModuleManager.getModule(ThemeModule.class).setEnabled(true);
    }
}
