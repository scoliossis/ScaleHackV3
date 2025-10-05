package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.Main;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.RenderTickEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
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
    @RegisterSubModule(name = "GameSense Colour", parent = "Square")
    public static Color senseColour = new Color(248, 97, 97);

    @RegisterSubModule(name = "Straight Bar", description = "replaces the rainbow bar with the colour you chose", parent = "Visuals")
    public static boolean straightBar = true;

    private static final int fontSize = 10;

    @SubscribeEvent
    public static void onRender2D(RenderTickEvent event) {
        String clientName = Main.MOD_NAME.split(" ")[0];
        String gamesenseTag = "Sense";
        String server = C.mc.isSingleplayer() ? "singleplayer" : C.mc.getCurrentServerData() != null ? C.mc.getCurrentServerData().serverIP : "unknown";
        String remainingHudText = " " + Main.MOD_VERSION + " | " + Minecraft.getDebugFPS() + " fps | " + server;

        float clientNameWidth = FontUtil.getStringWidth(clientName, fontSize);
        float gamesenseWidth = FontUtil.getStringWidth(gamesenseTag, fontSize);
        float remainingWidth = FontUtil.getStringWidth(remainingHudText, fontSize);

        float boxWidth = clientNameWidth + gamesenseWidth + remainingWidth + 4;
        float height = 12;

        float baseX = 5;
        float baseY = 5;

        RenderUtil.drawRect(baseX, baseY, boxWidth+8, height+8, new Color(60, 60, 60));
        RenderUtil.drawRect(baseX+1, baseY+1, boxWidth+6, height+6, new Color(40, 40, 40));
        RenderUtil.drawRect(baseX+2, baseY+2, boxWidth+4, height+4, new Color(60, 60, 60));
        RenderUtil.drawRect(baseX+3, baseY+3, boxWidth+2, height+2, new Color(22, 22, 22));

        float textBaseX = baseX + 5;
        float textBaseY = baseY + 4;

        FontUtil.drawString(clientName, textBaseX, textBaseY, fontSize, new Color(255,255,255), false);
        FontUtil.drawString(gamesenseTag, textBaseX + clientNameWidth, textBaseY, fontSize, senseColour, false);
        FontUtil.drawString(remainingHudText, textBaseX + clientNameWidth + gamesenseWidth, textBaseY, fontSize, new Color(255,255,255), false);

        Color[] colorsFade = straightBar ? new Color[] {senseColour, senseColour} : RenderUtil.getColorsFade(baseX, boxWidth, RenderUtil.ThemeColours.Gay.colours, 3f);
        RenderUtil.drawGradientLR(baseX + 3, baseY+3, boxWidth+2, 1, colorsFade[0], colorsFade[1]);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
