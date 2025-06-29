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

// todo: maybe make easier to use, but lazy rn
public class EnumSubModule extends SubModule {
    public EnumSubModule(Module parentModule, Field field, ArrayList<SubModule> children, SubModule parent, RegisterSubModule annotation) {
        super(parentModule, field, children, parent, annotation);
    }

    @Override
    public void render() {
        String enumName = ((Enum<?>) this.get()).name().replaceAll("_", " ");
        String subModuleName = this.getAnnotation().name() + ": &8" + enumName;
        FontUtil.drawString(subModuleName, ClickGUIScreen.X_OFFSET+TEXT_INDENT_X, TEXT_Y, FONT_SIZE, TEXT_COLOUR, true);
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY, Category category) {
        if (isHovered(mouseX, mouseY, category) && ClickGUIScreen.mouseButton != -1) {
            Enum<?> setting = (Enum<?>) this.get();

            EasingUtil.addAnimation(this.getUniqueKey() + setting.name(), closeAnimationLength, false, closeAnimation);

            int settingValue = setting.ordinal() + (ClickGUIScreen.mouseButton == 0 ? 1 : -1);

            if (settingValue >= this.getField().getType().getEnumConstants().length)
                this.set(this.getField().getType().getEnumConstants()[0]);
            else if (settingValue < 0)
                this.set(this.getField().getType().getEnumConstants()[this.getField().getType().getEnumConstants().length - 1]);
            else this.set(this.getField().getType().getEnumConstants()[settingValue]);

            EasingUtil.addAnimation(this.getUniqueKey() + ((Enum<?>) this.get()).name(), openAnimationLength, true, openAnimation);

            ClickGUIScreen.mouseButton = -1;
        }
    }
}
