package com.github.scoliossis.screens.ClickGUI.SubModuleRenderers;

import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.screens.ClickGUI.ClickGUIScreen;
import com.github.scoliossis.screens.ClickGUI.SubModuleRenderer;
import com.github.scoliossis.utils.client.ScreenUtil;
import com.github.scoliossis.utils.render.EasingUtil;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class EnumSubModuleRenderer extends SubModuleRenderer {
    @Override
    public void handleMouse(int mouseX, int mouseY, SubModule subModule) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0 || ClickGUIScreen.mouseButton == 1) {
                Enum<?> enumValue = (Enum<?>) subModule.get();
                int enumValues = subModule.getField().getType().getEnumConstants().length;
                int nextEnumValue = enumValue.ordinal() + (ClickGUIScreen.mouseButton == 0 ? 1 : -1);
                if (nextEnumValue < 0) nextEnumValue = enumValues - 1;
                subModule.set(subModule.getField().getType().getEnumConstants()[nextEnumValue % enumValues]);

                Enum<?> newEnumValue = (Enum<?>) subModule.get();

                EasingUtil.addAnimation(
                        (subModule.getUniqueKey()+enumValue.name()).toLowerCase(),
                        ClickGUIModule.closeAnimationLength,
                        false,
                        ClickGUIModule.closeAnimation
                );
                EasingUtil.addAnimation(
                        (subModule.getUniqueKey()+newEnumValue.name()).toLowerCase(),
                        ClickGUIModule.openAnimationLength,
                        true,
                        ClickGUIModule.openAnimation
                );
            }

            ClickGUIScreen.mouseButton = -1;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, SubModule subModule) {
        super.render(mouseX, mouseY, subModule);

        String enumValue = ((Enum<?>) subModule.get()).name().replace("_", " ");
        RenderUtil.drawRect(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, this.SUBMODULE_BACKGROUND_COLOUR);
        FontUtil.drawString(subModule.getAnnotation().name(), SUBMODULE_TEXT_X, getSubmoduleTextY(), ClickGUIScreen.fontSize, Color.WHITE, true);
        FontUtil.drawString(
                enumValue,
                ClickGUIScreen.BASE_X + ClickGUIScreen.GUI_TAB_WIDTH - FontUtil.getStringWidth(enumValue, ClickGUIScreen.fontSize) - this.LEFT_OFFSET,
                getSubmoduleTextY(),
                ClickGUIScreen.fontSize,
                ClickGUIScreen.secondaryColor,
                true
        );

        GL11.glTranslated(0, SUBMODULE_HEIGHT, 0);
    }
}