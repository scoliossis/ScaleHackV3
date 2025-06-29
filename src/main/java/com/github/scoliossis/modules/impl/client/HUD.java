package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.Main;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.RenderTickEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;

@RegisterModule(
        name = "HUD",
        description = "Displays text on the screen with various degrees of helpfulness",
        category = Category.CLIENT
)
public class HUD extends Module {
    private static final int fontSize = 10;

    @SubscribeEvent
    public static void onRender2D(RenderTickEvent event) {
        String clientGamesenseName = Main.MOD_NAME.split(" ")[0] + "Sense";
        String hudText = clientGamesenseName + " " + Main.MOD_VERSION + " | " + Minecraft.getDebugFPS() + " fps | " + (C.mc.getCurrentServerData() != null ? C.mc.getCurrentServerData().serverIP : "singleplayer");
        float width = FontUtil.getStringWidth(hudText, fontSize) + 4;
        float height = 12;

        float baseX = 5;
        float baseY = 5;

        RenderUtil.drawRect(baseX, baseY, width+8, height+8, new Color(60, 60, 60));
        RenderUtil.drawRect(baseX+1, baseY+1, width+6, height+6, new Color(40, 40, 40));
        RenderUtil.drawRect(baseX+2, baseY+2, width+4, height+4, new Color(60, 60, 60));
        RenderUtil.drawRect(baseX+3, baseY+3, width+2, height+2, new Color(22, 22, 22));
        FontUtil.drawString(hudText, baseX + 5, 9, fontSize, new Color(255,255,255), false);

        Color[] colorsFade = RenderUtil.getColorsFade(baseX, width, RenderUtil.ThemeColours.Gay.colours, 3f);
        RenderUtil.drawRectFade(baseX + 3, baseY+3, width+2, 1, colorsFade[0], colorsFade[1]);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
