package com.github.scoliossis.screens.ClickGUI;

import com.github.scoliossis.modules.SubCategory;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.utils.client.ScreenUtil;
import com.github.scoliossis.utils.render.FontUtil;

import java.awt.*;

public abstract class SubModuleRenderer {
    public final int SUBMODULE_HEIGHT = 15;
    public final Color SUBMODULE_BACKGROUND_COLOUR = new Color(25,25,25);
    public final float SUBMODULE_TEXT_X = ClickGUIScreen.BASE_X + 3;
    public final float SUBMODULE_TEXT_Y = ClickGUIScreen.BASE_Y + SUBMODULE_HEIGHT/2f - FontUtil.getFontHeight(ClickGUIScreen.fontSize)/2f;
    public final int LEFT_OFFSET = 5;

    public static void handle(int mouseX, int mouseY, SubModule subModule) {
        Class<?> fieldType = subModule.getField().getType();

        // java 8, sorry, cant switch by class
        if (fieldType == boolean.class) ClickGUIScreen.booleanSubModuleRenderer.render(mouseX, mouseY, subModule);
        else if (fieldType.isEnum()) ClickGUIScreen.enumSubModuleRenderer.render(mouseX, mouseY, subModule);
        else if (fieldType == Color.class) ClickGUIScreen.colourSubModuleRenderer.render(mouseX, mouseY, subModule);
        else if (fieldType == SubCategory.class) ClickGUIScreen.subCategoryRenderer.render(mouseX, mouseY, subModule);
        else ClickGUIScreen.sliderSubModuleRenderer.render(mouseX, mouseY, subModule);
    }

    public abstract void handleMouse(int mouseX, int mouseY, SubModule subModule);
    public void render(int mouseX, int mouseY, SubModule subModule) {
        this.handleMouse(mouseX, mouseY, subModule);

        boolean isHovered = ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, mouseX, mouseY);
        if (ClickGUIScreen.subModuleHovered == null && ClickGUIScreen.moduleHovered == null && isHovered) {
            ClickGUIScreen.subModuleHovered = subModule;
            ClickGUIScreen.hoverTime = System.currentTimeMillis();
        }
        else if (ClickGUIScreen.subModuleHovered == subModule && !isHovered)
            ClickGUIScreen.subModuleHovered = null;

        ClickGUIScreen.drawGuiBackground(SUBMODULE_HEIGHT, subModule.getParentModule().getAnnotation().category());
    }
}
