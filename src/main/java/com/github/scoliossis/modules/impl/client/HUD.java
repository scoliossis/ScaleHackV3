package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.Main;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import com.github.scoliossis.utils.render.draggable.Draggable;
import net.minecraft.client.Minecraft;

import java.awt.*;

@RegisterModule(
        name = "HUD",
        description = "Displays text on the screen with various degrees of helpfulness",
        category = Category.CLIENT
)
// todo: drag around
public class HUD extends Module {
    @RegisterSubModule(name = "Watermark", parent = "Square")
    public static Watermark_Mode watermarkMode = Watermark_Mode.Gamesense;
    public enum Watermark_Mode {
        Gamesense,
        Scale_Hack
    }

    @RegisterSubModule(name = "GameSense Colour", parent = "Watermark", modeParentString = "Gamesense")
    public static Color senseColour = new Color(248, 97, 97);
    @RegisterSubModule(name = "Straight Bar", description = "replaces the rainbow bar with the colour you chose", parent = "Watermark", modeParentString = "Gamesense")
    public static boolean straightBar = true;

    @RegisterSubModule(name = "Font Size", parent = "Watermark", modeParentString = "Scale_Hack", min = 10, max = 40)
    public static int fontSize = 30;
    @RegisterSubModule(name = "Fade Speed", parent = "Watermark", modeParentString = "Scale_Hack", min = 0.1, max = 10)
    public static double watermarkFadeSpeed = 5f;
    @RegisterSubModule(name = "Fade Spread", parent = "Watermark", modeParentString = "Scale_Hack", min = 0.1, max = 10)
    public static double watermarkFadeSpread = 5f;

    private static final int GAMESENSE_FONT_SIZE = 10;

    private static final float WATERMARK_X = 5;
    private static final float WATERMARK_Y = 5;

    private static final String CLIENT_NAME = Main.MOD_NAME.split(" ")[0];
    private static final String GAMESENSE_TAG = "Sense";

    private static final float GAMESENSE_BOX_HEIGHT = 12;

    public static Draggable gamesenseWatermark = new Draggable(
            "Gamesense",
            () -> {
                String server = C.mc.isSingleplayer() ? "singleplayer" : C.mc.getCurrentServerData() != null ? C.mc.getCurrentServerData().serverIP : "unknown";
                String remainingHudText = " " + Main.MOD_VERSION + " | " + Minecraft.getDebugFPS() + " fps | " + server;

                float clientNameWidth = FontUtil.getStringWidth(CLIENT_NAME, GAMESENSE_FONT_SIZE);
                float gamesenseWidth = FontUtil.getStringWidth(GAMESENSE_TAG, GAMESENSE_FONT_SIZE);
                float remainingWidth = FontUtil.getStringWidth(remainingHudText, GAMESENSE_FONT_SIZE);

                float boxWidth = clientNameWidth + gamesenseWidth + remainingWidth + 4;

                RenderUtil.drawRect(0, 0, boxWidth+8, GAMESENSE_BOX_HEIGHT+8, new Color(60, 60, 60));
                RenderUtil.drawRect(1, 1, boxWidth+6, GAMESENSE_BOX_HEIGHT+6, new Color(40, 40, 40));
                RenderUtil.drawRect(2, 2, boxWidth+4, GAMESENSE_BOX_HEIGHT+4, new Color(60, 60, 60));
                RenderUtil.drawRect(3, 3, boxWidth+2, GAMESENSE_BOX_HEIGHT+2, new Color(22, 22, 22));

                FontUtil.drawString(CLIENT_NAME, 5, 4, GAMESENSE_FONT_SIZE, new Color(255,255,255), false);
                FontUtil.drawString(GAMESENSE_TAG, 5 + clientNameWidth, 4, GAMESENSE_FONT_SIZE, senseColour, false);
                FontUtil.drawString(remainingHudText, 5 + clientNameWidth + gamesenseWidth, 4, GAMESENSE_FONT_SIZE, new Color(255,255,255), false);

                Color[] colorsFade = straightBar
                        ? new Color[] {senseColour, senseColour}
                        : RenderUtil.getColorsFade(0, boxWidth, RenderUtil.ThemeColours.Gay.getColours(), 3f);
                RenderUtil.drawGradientLR(3, 3, boxWidth+2, 1, colorsFade[0], colorsFade[1]);

                return new double[] {boxWidth+8, GAMESENSE_BOX_HEIGHT+8};
            },
            e -> ModuleManager.isEnabled(HUD.class) && watermarkMode == Watermark_Mode.Gamesense,
            e -> true
    );

    public static Draggable scalehakeWatermark = new Draggable(
            "ScaleHakeWatermark2025Punjabi",
            () -> {
                FontUtil.drawString(CLIENT_NAME, 0, 0, fontSize, ThemeModule.getThemeColours(), watermarkFadeSpeed, watermarkFadeSpread, true);

                return new double[] {FontUtil.getStringWidth(CLIENT_NAME, fontSize), FontUtil.getFontHeight(fontSize)};
            },
            e -> ModuleManager.isEnabled(HUD.class) && watermarkMode == Watermark_Mode.Scale_Hack,
            e -> true
    );

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
