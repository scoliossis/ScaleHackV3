package com.github.scoliossis.screens.ClickGUI.SubModuleRenderers;

import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.screens.ClickGUI.ClickGUIScreen;
import com.github.scoliossis.screens.ClickGUI.SubModuleRenderer;
import com.github.scoliossis.utils.client.ScreenUtil;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class SliderSubModuleRenderer extends SubModuleRenderer {
    @Override
    public void handleMouse(int mouseX, int mouseY, SubModule subModule) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0) ClickGUIScreen.currentSubModule = subModule;
            ClickGUIScreen.mouseButton = -1;
        }

        if (ClickGUIScreen.currentSubModule == subModule) {
            int[] mousePos = ScreenUtil.fixMousePos(mouseX, mouseY);
            double value = mousePos[0] - ClickGUIScreen.BASE_X;
            value /= ClickGUIScreen.GUI_TAB_WIDTH;
            value *= (subModule.getAnnotation().max() - subModule.getAnnotation().min());
            value += subModule.getAnnotation().min();
            value = MathHelper.clamp_double(value, subModule.getAnnotation().min(), subModule.getAnnotation().max());

            subModule.set(value);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, SubModule subModule) {
        super.render(mouseX, mouseY, subModule);

        RenderUtil.drawRect(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, this.SUBMODULE_BACKGROUND_COLOUR);

        double width = ((Double.parseDouble(subModule.get().toString())) - subModule.getAnnotation().min()) / (subModule.getAnnotation().max() - subModule.getAnnotation().min()) * ClickGUIScreen.GUI_TAB_WIDTH;

        RenderUtil.drawRect(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, width, SUBMODULE_HEIGHT, subModule.getParentModule().getAnnotation().category().color);

        FontUtil.drawString(subModule.getAnnotation().name(), SUBMODULE_TEXT_X, getSubmoduleTextY(), ClickGUIScreen.fontSize, Color.WHITE, true);
        FontUtil.drawString(
                subModule.get().toString(),
                ClickGUIScreen.BASE_X + ClickGUIScreen.GUI_TAB_WIDTH - FontUtil.getStringWidth(subModule.get().toString(), ClickGUIScreen.fontSize) - this.LEFT_OFFSET,
                getSubmoduleTextY(),
                ClickGUIScreen.fontSize,
                ClickGUIScreen.secondaryColor,
                true
        );

        GL11.glTranslated(0, SUBMODULE_HEIGHT, 0);
    }
}
