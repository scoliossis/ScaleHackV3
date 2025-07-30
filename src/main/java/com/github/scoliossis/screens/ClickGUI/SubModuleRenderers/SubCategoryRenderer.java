package com.github.scoliossis.screens.ClickGUI.SubModuleRenderers;

import com.github.scoliossis.modules.SubCategory;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.screens.ClickGUI.ClickGUIScreen;
import com.github.scoliossis.screens.ClickGUI.SubModuleRenderer;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.ScreenUtil;
import org.lwjgl.opengl.GL11;
import java.awt.*;

public class SubCategoryRenderer extends SubModuleRenderer {
    public final int SUBCATEGORY_HEIGHT = 17;
    public final float SUBCATEGORY_TEXT_Y = ClickGUIScreen.BASE_Y + SUBCATEGORY_HEIGHT/2f - FontUtil.getFontHeight(ClickGUIScreen.fontSize)/2f;

    @Override
    public void handleMouse(int mouseX, int mouseY, SubModule subModule) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0) {
                SubCategory subCategory = (SubCategory) subModule.get();
                EasingUtil.addAnimation(
                        subModule.getUniqueKey(),
                        !subCategory.open ? ClickGUIModule.openAnimationLength : ClickGUIModule.closeAnimationLength,
                        !subCategory.open,
                        !subCategory.open ? ClickGUIModule.openAnimation : ClickGUIModule.closeAnimation
                );
                subCategory.open = !subCategory.open;
            }

            ClickGUIScreen.mouseButton = -1;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, SubModule subModule) {
        super.render(mouseX, mouseY, subModule);

        FontUtil.drawCenteredString(subModule.getAnnotation().name(), SUBMODULE_TEXT_X + ClickGUIScreen.GUI_TAB_WIDTH/2f, SUBCATEGORY_TEXT_Y, ClickGUIScreen.fontSize, Color.WHITE, true);
        GL11.glTranslated(0, SUBMODULE_HEIGHT, 0);
        ClickGUIScreen.drawGuiBackground(SUBCATEGORY_HEIGHT-SUBMODULE_HEIGHT, subModule.getParentModule().getAnnotation().category());
        GL11.glTranslated(0, SUBCATEGORY_HEIGHT-SUBMODULE_HEIGHT, 0);
    }
}
