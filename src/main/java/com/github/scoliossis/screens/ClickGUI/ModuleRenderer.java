package com.github.scoliossis.screens.ClickGUI;

import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.utils.client.KeybindHandler;
import com.github.scoliossis.utils.client.ScreenUtil;
import com.github.scoliossis.utils.render.EasingUtil;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ModuleRenderer extends Module {
    private final int MODULE_HEIGHT = 17;
    private final float MODULE_TEXT_INDENT_X = 5;
    private final Color MODULE_COLOUR = new Color(37, 37, 37);

    public static String moduleName(Module module) {
        return module.getAnnotation().name().replaceAll(" ", "").toLowerCase();
    }

    public void render(Module module, int mouseX, int mouseY) {
        ClickGUIScreen.drawGuiBackground(MODULE_HEIGHT, module.getAnnotation().category());

        String moduleName = moduleName(module);

        Color backgroundColor = module.isEnabled() ? module.getAnnotation().category().color : MODULE_COLOUR;
        boolean isMouseOver = ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y+1, ClickGUIScreen.GUI_TAB_WIDTH, MODULE_HEIGHT-1, mouseX, mouseY);
        if (isMouseOver && ClickGUIScreen.categoryRenderer.currentDraggingCategory == null) {
            if (ClickGUIScreen.moduleHovered == null) {
                ClickGUIScreen.hoverTime = System.currentTimeMillis();
                ClickGUIScreen.moduleHovered = module;
            }
            backgroundColor = backgroundColor.darker();
        }
        else if (ClickGUIScreen.moduleHovered == module) {
            ClickGUIScreen.moduleHovered = null;
        }

        RenderUtil.drawRect(
                ClickGUIScreen.BASE_X,
                ClickGUIScreen.BASE_Y,
                ClickGUIScreen.GUI_TAB_WIDTH,
                MODULE_HEIGHT,
                backgroundColor
        );

        float moduleNameTextX = ClickGUIScreen.BASE_X + ClickGUIScreen.GUI_TAB_WIDTH - MODULE_TEXT_INDENT_X - FontUtil.getStringWidth(moduleName, ClickGUIScreen.fontSize);
        float moduleNameTextY = ClickGUIScreen.BASE_Y + MODULE_HEIGHT / 2f - FontUtil.getFontHeight(ClickGUIScreen.fontSize) / 2f + 1f;
        FontUtil.drawString(moduleName, moduleNameTextX, moduleNameTextY, ClickGUIScreen.fontSize, Color.WHITE, true);

        String keybindName = KeybindHandler.listeningModule == module ? "[LISTENING]" : module.getKeybind() != -1 ? "[" + Keyboard.getKeyName(module.getKeybind()) + "]" : "";
        FontUtil.drawString(keybindName, ClickGUIScreen.BASE_X + MODULE_TEXT_INDENT_X, moduleNameTextY, ClickGUIScreen.fontSize, ClickGUIScreen.secondaryColor, true);

        GL11.glTranslated(0, MODULE_HEIGHT, 0);
    }

    public void handleMouse(Module module, int mouseX, int mouseY) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, MODULE_HEIGHT, mouseX, mouseY)) {
            switch (ClickGUIScreen.mouseButton) {
                case 0:
                    module.toggle();
                    break;
                case 1:
                    module.setOpen(!module.isOpen());
                    EasingUtil.addAnimation(
                            module.getUniqueKey(""),
                            module.isOpen() ? ClickGUIModule.openAnimationLength : ClickGUIModule.closeAnimationLength,
                            module.isOpen(),
                            module.isOpen() ? ClickGUIModule.openAnimation : ClickGUIModule.closeAnimation
                    );
                    break;
                case 2:
                    KeybindHandler.listeningModule = module;
                    break;
            }
            ClickGUIScreen.mouseButton = -1;
        }
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }
}
