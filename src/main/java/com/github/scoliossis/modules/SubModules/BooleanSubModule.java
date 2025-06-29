package com.github.scoliossis.modules.SubModules;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.github.scoliossis.modules.impl.client.ClickGUIModule.*;

public class BooleanSubModule extends SubModule {
    public BooleanSubModule(Module parentModule, Field field, ArrayList<SubModule> children, SubModule parent, RegisterSubModule annotation) {
        super(parentModule, field, children, parent, annotation);
    }

    private final float SQUARE_SIZE = HEIGHT/2;
    private final float SQUARE_X = ClickGUIScreen.WIDTH - SQUARE_SIZE *2;
    private final float SQUARE_Y = HEIGHT/2 - (SQUARE_SIZE /2);
    private final Color SQUARE_DISABLED_COLOUR = Color.BLACK;

    @Override
    public void render() {
        FontUtil.drawString(this.getAnnotation().name(), ClickGUIScreen.X_OFFSET+this.TEXT_INDENT_X, TEXT_Y, FONT_SIZE, (this.getAnnotation().dangerous() ? ClickGUIScreen.getDangerousColour() : TEXT_COLOUR), true);

        if ((boolean) this.get())
            RenderUtil.drawRect(ClickGUIScreen.X_OFFSET + SQUARE_X, SQUARE_Y, SQUARE_SIZE, SQUARE_SIZE, ClickGUIScreen.getEnabledColour());
        else
            RenderUtil.drawRect(ClickGUIScreen.X_OFFSET + SQUARE_X, SQUARE_Y, SQUARE_SIZE, SQUARE_SIZE, SQUARE_DISABLED_COLOUR);
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY, Category category) {
        if (this.isHovered(mouseX, mouseY, category) && ClickGUIScreen.mouseButton != -1) {
            boolean enabled = !(boolean) this.get();
            this.set(enabled);
            EasingUtil.addAnimation(this.getUniqueKey(), enabled ? openAnimationLength : closeAnimationLength, enabled, enabled ? openAnimation : closeAnimation);
            ClickGUIScreen.mouseButton = -1;
        }
    }
}
