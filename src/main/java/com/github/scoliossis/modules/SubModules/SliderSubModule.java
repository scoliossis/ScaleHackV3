package com.github.scoliossis.modules.SubModules;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import com.github.scoliossis.utils.ScreenUtil;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class SliderSubModule extends SubModule {
    public SliderSubModule(Module parentModule, Field field, ArrayList<SubModule> children, SubModule parent, RegisterSubModule annotation) {
        super(parentModule, field, children, parent, annotation);
    }

    private final float TEXT_Y = 0;
    private final float BAR_HEIGHT = 2;
    private final float BAR_X = 5;
    private final float BAR_Y = TEXT_Y + FontUtil.getFontHeight(FONT_SIZE) + BAR_HEIGHT - 1;
    private final float BAR_WIDTH = ClickGUIScreen.WIDTH-TEXT_INDENT_X*2;
    private final float CIRCLE_HEIGHT = 6;
    private final float CIRCLE_WIDTH = 2;
    private final float CIRCLE_Y = BAR_Y - (CIRCLE_HEIGHT / 2) + BAR_HEIGHT / 2;
    private final Color CIRCLE_COLOUR = Color.WHITE;

    @Override
    public void render() {
        double value = Double.parseDouble(this.get().toString());
        String subModuleName = this.getAnnotation().name() + ": &8" + value;

        FontUtil.drawString(subModuleName, ClickGUIScreen.X_OFFSET + TEXT_INDENT_X, TEXT_Y, FONT_SIZE, TEXT_COLOUR, true);

        float xRatio = (float) ((value - this.getAnnotation().min()) / (this.getAnnotation().max() - this.getAnnotation().min()));
        float circleX = BAR_X + (BAR_WIDTH * xRatio);

        RenderUtil.drawRoundedRect(ClickGUIScreen.X_OFFSET+BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, BAR_HEIGHT, ClickGUIScreen.getEnabledColour());
        RenderUtil.drawRoundedRect(ClickGUIScreen.X_OFFSET+circleX, BAR_Y, BAR_WIDTH - circleX + BAR_X, BAR_HEIGHT, BAR_HEIGHT, ClickGUIScreen.EXTRA_COLOUR);

        RenderUtil.drawRoundedRect((int) (ClickGUIScreen.X_OFFSET + circleX - CIRCLE_WIDTH / 2), CIRCLE_Y, CIRCLE_WIDTH, CIRCLE_HEIGHT, CIRCLE_WIDTH+CIRCLE_HEIGHT, CIRCLE_COLOUR);
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY, Category category) {
        if (ClickGUIScreen.categoryBeingDragged == null && !isMouseCulled(mouseX, mouseY, category) && ScreenUtil.isMouseOver(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, mouseX, mouseY) && ClickGUIScreen.mouseDown) {
            float translatedMouseX = ScreenUtil.fixMousePos(mouseX, mouseY)[0];

            double xRatio = Math.min(Math.max((translatedMouseX - (BAR_X + ClickGUIScreen.X_OFFSET)) / BAR_WIDTH, 0), 1);
            double setting = xRatio * (this.getAnnotation().max() - this.getAnnotation().min()) + this.getAnnotation().min();

            this.set(setting);
        }

    }
}
