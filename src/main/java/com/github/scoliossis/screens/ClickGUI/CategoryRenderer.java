package com.github.scoliossis.screens.ClickGUI;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.utils.client.ScreenUtil;
import com.github.scoliossis.utils.render.EasingUtil;
import com.github.scoliossis.utils.render.FontUtil;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class CategoryRenderer {
    public final int CATEGORY_HEIGHT = 20;
    protected Category currentDraggingCategory = null;
    private float categoryDragStartX = -1;
    private float categoryDragStartY = -1;

    public void render(Category category) {
        float textX = ClickGUIScreen.BASE_X + 2;
        float textY = ClickGUIScreen.BASE_Y + CATEGORY_HEIGHT / 2f - FontUtil.getFontHeight(ClickGUIScreen.fontSize) / 2f + 1f;

        ClickGUIScreen.drawGuiBackground(CATEGORY_HEIGHT, category);

        String categoryName = EnumChatFormatting.BOLD + category.name().replaceAll("_", "").toLowerCase();

        FontUtil.drawString(categoryName, textX, textY, ClickGUIScreen.fontSize, Color.WHITE, true);

        GL11.glTranslated(0, CATEGORY_HEIGHT, 0);
    }

    public void handleMouse(Category category, int mouseX, int mouseY) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, CATEGORY_HEIGHT, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0) {
                categoryDragStartX = mouseX - category.posX;
                categoryDragStartY = mouseY - category.posY;
                currentDraggingCategory = category;
            }
            if (ClickGUIScreen.mouseButton == 1) {
                category.open = !category.open;
                EasingUtil.addAnimation(
                        category.name(),
                        category.open ? ClickGUIModule.openAnimationLength : ClickGUIModule.closeAnimationLength,
                        category.open,
                        category.open ? ClickGUIModule.openAnimation : ClickGUIModule.closeAnimation
                );
            }

            ClickGUIScreen.mouseButton = -1;
        }

        if (currentDraggingCategory == category) {
            category.posX = mouseX - categoryDragStartX;
            category.posY = mouseY - categoryDragStartY;
        }
    }
}
