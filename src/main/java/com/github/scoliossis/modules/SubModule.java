package com.github.scoliossis.modules;

import com.github.scoliossis.Main;
import com.github.scoliossis.modules.SubModules.SubCategory;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

@AllArgsConstructor
@Getter
public abstract class SubModule {
    public static final float HEIGHT = 20;
    protected final int FONT_SIZE = 10;
    protected final int TEXT_INDENT_X = 5;
    protected final float TEXT_Y = HEIGHT/2F - (FontUtil.getFontHeight(FONT_SIZE)/2F);
    protected final Color TEXT_COLOUR = Color.WHITE;
    protected final Color BACKGROUND_COLOUR = new Color(22,22,22, 200);

    private final Module parentModule;

    private final Field field;
    private final ArrayList<SubModule> children;

    @Setter
    private SubModule parent;

    private RegisterSubModule annotation;

    public void set(Object object) {
        try {
            // todo: clean this up, it's a mess.
            if (field.getType() == double.class || field.getType() == float.class || field.getType() == long.class || field.getType() == int.class) {
                double value = Double.parseDouble(object.toString());
                double roundedValue = MathUtil.toNearest(value, annotation.increment());
                // this is def stupid, my fault og
                if ((annotation.increment()+"").contains("."))
                    roundedValue = MathUtil.roundTo(roundedValue, (annotation.increment()+"").split("\\.")[1].length());

                double clampedValue = MathHelper.clamp_double(roundedValue, annotation.min(), annotation.max());

                if (field.getType() == double.class) field.set(parentModule, clampedValue);
                else if (field.getType() == float.class) field.set(parentModule, (float) clampedValue);
                else if (field.getType() == long.class) field.set(parentModule, (long) clampedValue);
                else field.set(parentModule, (int) clampedValue);
            }

            else field.set(parentModule, object);

            // save config whenever anything changes!
            ModuleManager.saveConfig(Main.baseConfig);
        } catch (Exception e) {
            e.printStackTrace();
            ChatUtil.prefixMessage("&cFailed to set field &f" + field.getName() + " &cof &f" + parentModule + " &cto &f" + object);
        }
    }

    public Object get() {
        try {
            return field.get(parentModule);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldShow(boolean includeSubcategories) {
        if (parent == null) return true;
        else if (parent.getField().getType().equals(boolean.class))
            return ((boolean) parent.get() || EasingUtil.getAnimation(parent.getUniqueKey()) != -1) && parent.shouldShow(includeSubcategories);
        else if (parent.getField().getType().isEnum()) {
            for (String parentEnum : this.annotation.modeParentString()) {
                if (parentEnum.equals(((Enum<?>) parent.get()).name()) || EasingUtil.getAnimation(parent.getUniqueKey()+parentEnum) != -1)
                    return parent.shouldShow(includeSubcategories);
            }
            return false;
        }
        else if (parent.getField().getType() == SubCategory.class && includeSubcategories)
            return (((SubCategory) parent.get()).open || EasingUtil.getAnimation(parent.getUniqueKey()) != -1) && parent.shouldShow(true);
        return parent.shouldShow(includeSubcategories);
    }

    public String getUniqueKey() {
        return annotation.name() + parentModule.getAnnotation().name() + annotation.parent() + annotation.description();
    }

    public void handle(int mouseX, int mouseY, Category category, Module module) {
        double subModuleAnimationProgress = this.getScale();

        // dont draw anything if its scale is 0 for obvious reasons.
        if (subModuleAnimationProgress == 0) return;

        if (subModuleAnimationProgress != -1) GL11.glScaled(1, subModuleAnimationProgress, 1);

        RenderUtil.drawRect(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, BACKGROUND_COLOUR);

        // if submodule is hovered it will need its description rendered.
        if (isHovered(mouseX, mouseY, category)) {
            if (ClickGUIScreen.submoduleHovered == null) {
                ClickGUIScreen.submoduleHovered = this;
                ClickGUIScreen.moduleHoveredTime = System.currentTimeMillis();
            }
        } else if (ClickGUIScreen.submoduleHovered == this) ClickGUIScreen.submoduleHovered = null;

        this.handleMouseInput(mouseX, mouseY, category);
        this.render();

        GL11.glTranslated(0, HEIGHT, 0);
        if (subModuleAnimationProgress != -1) GL11.glScaled(1, 1/subModuleAnimationProgress, 1);
    }

    private double getScale() {
        double subModuleAnimationProgress = EasingUtil.getAnimation(this.getParentModule().getUniqueKey("clickgui"));
        if (EasingUtil.getAnimation(this.getParentModule().getUniqueKey("clickgui")) == -1) {
            if (!this.shouldShow(true)) return 0;
            subModuleAnimationProgress = 1;
        }


        SubModule parent = this.getParent();
        SubModule prev = this;

        while (parent != null) {
            double animationProgressParent = EasingUtil.getAnimation(parent.getUniqueKey());
            if (parent.getField().getType().isEnum()) {
                for (String parentEnum : prev.getAnnotation().modeParentString()) {
                    animationProgressParent = EasingUtil.getAnimation(parent.getUniqueKey()+parentEnum);
                    break;
                }
            }
            if (animationProgressParent != -1) subModuleAnimationProgress *= animationProgressParent;
            prev = parent;
            parent = parent.getParent();
        }

        return subModuleAnimationProgress;
    }

    protected boolean isMouseCulled(int mouseX, int mouseY, Category category) {
        return mouseY / RenderUtil.getCurrentTranslation()[4] <= category.renderPos[1] + category.HEIGHT;
    }

    protected boolean isHovered(int mouseX, int mouseY, Category category) {
        return ScreenUtil.isMouseOver(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, mouseX, mouseY) && !isMouseCulled(mouseX, mouseY, category);
    }

    public abstract void render();
    public abstract void handleMouseInput(int mouseX, int mouseY, Category category);
}
