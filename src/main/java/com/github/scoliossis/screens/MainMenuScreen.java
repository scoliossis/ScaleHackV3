package com.github.scoliossis.screens;

import com.github.scoliossis.Main;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.client.ScreenUtil;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import com.github.scoliossis.utils.render.panorama.PanoramaRenderer;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;

import java.awt.*;
import java.io.IOException;

public class MainMenuScreen extends GuiScreen {
    String[] strings = {"singleplayer", "multiplayer", "settings", "alts"};

    @Override
    public void drawScreen(int mX, int mY, float partialTicks) {
        drawBackground();
        drawButtons(mX, mY);

        leftMousePressed = false;

        super.drawScreen(mX, mY, partialTicks);
    }

    public void drawButtons(int mX, int mY) {
        int screenWidth = C.res().getScaledWidth();
        int screenHeight = C.res().getScaledHeight();

        float w = 300;
        float h = 220;
        float x = screenWidth/2f - w/2;
        float y = screenHeight/2f - h/2;


        String clientNameText = Main.MOD_NAME + " " + Main.MOD_VERSION;
        float textX = screenWidth/2f;
        float textY = y+10;

        RenderUtil.drawBlurRect(x, y, w, h, 3);
        RenderUtil.drawRoundedRect(x, y, w, h, 5, new Color(10,10,10, 100));
        RenderUtil.drawRoundedRectOutline(x, y, w, h, 5, 2, new Color(22,22,22, 50));
        FontUtil.drawCenteredString(clientNameText, textX, textY, 30, new Color(0xffffffff), true);

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];

            float newW = w / 1.3f;
            float newH = h/8;
            float newX = (x + w/2f) - newW/2f;
            float newY = y+newH*(i+1) + i * 10 + 30;

            float newTextX = (newX + newW/2f);
            float newTextY = newY+(newH/2) - FontUtil.getFontHeight(18)/2f;

            boolean hovered = ScreenUtil.isMouseOver(newX, newY, newW, newH, mX, mY);

            Color stringColor = hovered ? new Color(255,255,255,255) : new Color(255,255,255,190);

            RenderUtil.drawBlurRect(newX, newY, newW, newH, 5);
            RenderUtil.drawRoundedRect(newX, newY, newW, newH, 1, new Color(22, 22, 22, 100));

            FontUtil.drawCenteredString(string, newTextX, newTextY, 18, stringColor, true);

            if (hovered) {
                FontUtil.drawCenteredString(string, newTextX, newTextY, 18, new Color(0xffffffff), true);

                if (leftMousePressed) {
                    switch (i) {
                        case 0:
                            C.mc.displayGuiScreen(new GuiSelectWorld(this));
                            break;
                        case 1:
                            C.mc.displayGuiScreen(new GuiMultiplayer(this));
                            break;
                        case 2:
                            C.mc.displayGuiScreen(new GuiOptions(this, C.mc.gameSettings));
                            break;
                        case 3:
                            C.mc.displayGuiScreen(new AltManagerScreen());
                            break;
                    }
                }
            }
        }
    }

    public static void drawBackground() {
        PanoramaRenderer.getInstance().render(1f);
    }


    public static boolean leftMousePressed = false;

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);

        if (button == 0) leftMousePressed = true;
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);

        if (button == 0) leftMousePressed = false;
    }
}