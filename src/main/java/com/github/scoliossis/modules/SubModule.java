package com.github.scoliossis.modules;

import com.github.scoliossis.Main;
import com.github.scoliossis.utils.ChatUtil;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

@Getter
// todo: add unique key
public class SubModule {
    private final Module parentModule;

    private final Field field;
    private final ArrayList<SubModule> children;

    @Setter
    private SubModule parent;

    public final RegisterSubModule annotation;

    public SubModule(Module parentModule, Field field) {
        this.parentModule = parentModule;
        this.field = field;
        this.children = new ArrayList<>();
        this.annotation = field.getAnnotation(RegisterSubModule.class);
    }

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
            // when saved to file, its saved as {value=colour, falpha=0.0}
            else if (field.getType() == Color.class && object.toString().contains("value=")) {
                int colorValue = (int) Double.parseDouble(object.toString().split("value=")[1].split(",")[0]);
                field.set(parentModule, new Color(colorValue, true));
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

    public boolean isSlider() {
        return this.getField().getType() == double.class
                || this.getField().getType() == float.class
                || this.getField().getType() == long.class
                || this.getField().getType() == int.class;
    }

    public boolean shouldRender() {
        return shouldRender(false);
    }

    public boolean shouldRender(boolean ignoreSubCategory) {
        if (!ignoreSubCategory && !this.parentModule.isOpen() && EasingUtil.getAnimation(this.parentModule.getUniqueKey("")) == -1)
            return false;

        if (this.parent == null) return true;
        if (!this.parent.shouldRender()) return false;
        if (EasingUtil.getAnimation(this.parent.getUniqueKey()) != -1) return true;

        if (this.parent.getField().getType() == boolean.class)
            return (boolean) this.parent.get();
        else if (this.parent.getField().getType() == SubCategory.class) {
            if (ignoreSubCategory) return true;
            return ((SubCategory) this.parent.get()).open;
        }
        else if (this.parent.getField().getType().isEnum()) {
            Enum<?> parentValue = (Enum<?>) this.parent.get();

            for (String string : this.getAnnotation().modeParentString()) {
                if (
                        parentValue.name().equalsIgnoreCase(string)
                        || EasingUtil.getAnimation((this.parent.getUniqueKey() + string).toLowerCase()) != -1
                )
                    return this.parent.shouldRender();
            }

            return false;
        }

        return this.parent.shouldRender();
    }

    // messy, but being smart is hard.
    public double getAnimationProgress() {
        double parentAnimationProgress = -1;
        SubModule parent = this.getParent();
        SubModule child = this;
        while (parent != null) {
            double parentAnimation = EasingUtil.getAnimation(parent.getUniqueKey());

            // handle enums differently todo: enums with multiple parents flicker a little, iykyk
            if (parent.getField().getType().isEnum()) {
                for (String mode : child.getAnnotation().modeParentString()) {
                    parentAnimation = Math.max(parentAnimation, EasingUtil.getAnimation((parent.getUniqueKey() + mode).toLowerCase()));
                }
            }

            if (parentAnimation != -1) {
                if (parentAnimationProgress == -1) parentAnimationProgress = parentAnimation;
                else parentAnimationProgress *= parentAnimation;
            }

            child = parent;
            parent = parent.getParent();
        }

        return parentAnimationProgress;
    }

    public String getUniqueKey() {
        return this.parentModule.getUniqueKey(this.getAnnotation().name() + this.getAnnotation().description());
    }

    // todo: this is dumb
    public ColorSettingValues colorSettingValues = new ColorSettingValues(false, 0, 0, 0);

    @AllArgsConstructor
    public static class ColorSettingValues {
        public boolean open;
        public int mX, mY;
        public float hueValue;
    }
}
