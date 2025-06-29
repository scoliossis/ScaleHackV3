package com.github.scoliossis.modules.SubModules;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.FontUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.github.scoliossis.modules.impl.client.ClickGUIModule.*;

public class SubCategory extends SubModule {
    public boolean open;

    public SubCategory(Module parentModule, Field field, ArrayList<SubModule> children, SubModule parent, RegisterSubModule annotation) {
        super(parentModule, field, children, parent, annotation);
    }

    public SubCategory(boolean open) {
        super(null, null, null, null, null);
        this.open = open;
    }

    public SubCategory() {
        this(false);
    }

    @Override
    public void render() {
        FontUtil.drawCenteredString(this.getAnnotation().name(),  ClickGUIScreen.X_OFFSET + ClickGUIScreen.WIDTH / 2f, TEXT_Y, FONT_SIZE, TEXT_COLOUR, true);
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY, Category category) {
        if (this.isHovered(mouseX, mouseY, category) && ClickGUIScreen.mouseButton != -1) {
            boolean enabled = !((SubCategory) this.get()).open;
            ((SubCategory) this.get()).open = enabled;
            EasingUtil.addAnimation(this.getUniqueKey(), enabled ? openAnimationLength : closeAnimationLength, enabled, enabled ? openAnimation : closeAnimation);

            ClickGUIScreen.mouseButton = -1;
        }
    }
}