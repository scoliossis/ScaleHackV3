package com.github.scoliossis.screens.ClickGUI.SubModuleRenderers;

import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.screens.ClickGUI.ClickGUIScreen;
import com.github.scoliossis.screens.ClickGUI.SubModuleRenderer;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import com.github.scoliossis.utils.ScreenUtil;
import org.lwjgl.opengl.GL11;
import java.awt.*;

public class BooleanSubModuleRenderer extends SubModuleRenderer {
    private static final int TOGGLED_BOX_SIZE = 10;

    @Override
    public void handleMouse(int mouseX, int mouseY, SubModule subModule) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0) {
                boolean currentState = (boolean) subModule.get();
                subModule.set(!currentState);
                EasingUtil.addAnimation(
                        subModule.getUniqueKey(),
                        !currentState ? ClickGUIModule.openAnimationLength : ClickGUIModule.closeAnimationLength,
                        !currentState,
                        !currentState ? ClickGUIModule.openAnimation : ClickGUIModule.closeAnimation
                );
            }

            ClickGUIScreen.mouseButton = -1;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, SubModule subModule) {
        super.render(mouseX, mouseY, subModule);

        RenderUtil.drawRect(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, this.SUBMODULE_BACKGROUND_COLOUR);
        RenderUtil.drawRect(
                ClickGUIScreen.BASE_X + ClickGUIScreen.GUI_TAB_WIDTH - TOGGLED_BOX_SIZE - this.LEFT_OFFSET,
                ClickGUIScreen.BASE_Y + SUBMODULE_HEIGHT / 2f - (TOGGLED_BOX_SIZE/2f),
                TOGGLED_BOX_SIZE,
                TOGGLED_BOX_SIZE,
                (boolean) subModule.get() ? subModule.getParentModule().getAnnotation().category().color : Color.WHITE
        );

        FontUtil.drawString(subModule.getAnnotation().name(), SUBMODULE_TEXT_X, SUBMODULE_TEXT_Y, ClickGUIScreen.fontSize, Color.WHITE, true);
        GL11.glTranslated(0, SUBMODULE_HEIGHT, 0);
    }
}
