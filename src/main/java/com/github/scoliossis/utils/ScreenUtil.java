package com.github.scoliossis.utils;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;

public class ScreenUtil {
    @Setter
    private static GuiScreen guiToDisplay = null;

    @SubscribeEvent
    public static void onPlayerTick(PlayerUpdateEvent event) {
        if (guiToDisplay != null) {
            C.mc.displayGuiScreen(guiToDisplay);
            guiToDisplay = null;
        }
    }

    public static int[] fixMousePos(float mouseX, float mouseY) {
        float[] translations = RenderUtil.getCurrentTranslation();

        mouseX -= translations[0];
        mouseY -= translations[1];

        mouseX /= translations[3];
        mouseY /= translations[4];

        return new int[] {(int) mouseX, (int) mouseY};
    }

    public static boolean isMouseOver(float x, float y, float width, float height, int mouseX, int mouseY) {
        int[] mousePos = fixMousePos(mouseX, mouseY);

        return mousePos[0] >= x && mousePos[1] >= y && mousePos[0] <= x + width && mousePos[1] <= y + height;
    }
}
