package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;

import java.awt.*;

// the description of this module was almost https://www.youtube.com/watch?v=s4nzOAMMqW0
@RegisterModule(
        name = "Theme",
        description = "https://www.youtube.com/watch?v=8ptm5NuIthg",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class ThemeModule extends Module {
    @RegisterSubModule(name = "Client")
    public static SubCategory client = new SubCategory();
    @RegisterSubModule(name = "Font", parent = "Client")
    public static FontUtil.Fonts font = FontUtil.Fonts.Atkinson;

    @RegisterSubModule(name = "Theme Colour", parent = "Client")
    public static RenderUtil.ThemeColours themeColour = RenderUtil.ThemeColours.Gay;
    @RegisterSubModule(name = "Custom Colour 1", parent = "Theme Colour", modeParentString = "Custom")
    public static Color customColour1 = new Color(255,255,255);
    @RegisterSubModule(name = "Custom Colour 2", parent = "Theme Colour", modeParentString = "Custom")
    public static Color customColour2 = new Color(0, 0, 0);

    @RegisterSubModule(name = "Minecraft")
    public static SubCategory minecraft = new SubCategory();
    @RegisterSubModule(name = "Replace Minecraft Font", description = "Replaces the minecraft font", parent = "Minecraft")
    public static boolean globalFont = false;
    @RegisterSubModule(name = "Minecraft Font Size", parent = "Replace Minecraft Font", min = 5, max = 20)
    public static int minecraftFontSize = 10;

    private static FontUtil.Fonts previousFont = null;

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent event) {
        if (font != previousFont) {
            FontUtil.setCurrentFont(font);
            previousFont = font;
        }
    }

    public static Color[] getThemeColours() {
        return themeColour.getColours();
    }

    // force reload font
    @Override
    protected void onEnable() {
        FontUtil.setCurrentFont(font);
        previousFont = font;
    }

    @Override
    protected void onDisable() {
        ModuleManager.getModule(ThemeModule.class).setEnabled(true);
    }
}
