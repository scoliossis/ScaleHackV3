package com.github.scoliossis.modules;

import com.github.scoliossis.Main;
import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.ModuleStateChangeEvent;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.*;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.scoliossis.modules.impl.client.ClickGUIModule.*;

@Getter
public abstract class Module {
    public static final float HEIGHT = 20;
    private final int MODULE_FONT_SIZE = 12;
    private final float TEXT_Y = HEIGHT/2F - (FontUtil.getFontHeight(MODULE_FONT_SIZE)/2F);
    private final Color TEXT_COLOUR = Color.WHITE;
    private final Color TEXT_COLOUR_OFF_TINT = new Color(200,200,200);
    private final Color BACKGROUND_COLOUR = new Color(22,22,22, 200);
    private final Color ENABLED_MODULE_BACKGROUND_COLOUR = BACKGROUND_COLOUR;
    private final int ARROW_X_OFFSET = ClickGUIScreen.WIDTH/10;
    private final float ARROW_WIDTH = 8;
    private final float ARROW_HEIGHT = 4;
    private final int ARROW_LINE_SIZE = 3;

    private boolean enabled;
    private final ArrayList<SubModule> children = new ArrayList<>();

    @Setter
    private RegisterModule annotation;

    private int keybind = -1;

    public void setKeybind(int keybind) {
        this.keybind = keybind;

        ModuleManager.saveConfig(Main.baseConfig);
    }

    public void setEnabled(boolean flag) {
        if (enabled != flag) {
            enabled = flag;

            // save config whenever anything changes!
            ModuleManager.saveConfig(Main.baseConfig);

            Bus.post(new ModuleStateChangeEvent(this, flag));

            if (enabled) onEnable();
            else onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public String getUniqueKey(String key) {
        return annotation.name() + annotation.category().name() + annotation.description() + key;
    }

    public String arrayListExtraInfo() {
        return "";
    }

    public void handle(int mouseX, int mouseY, Category category) {
        this.handleMouseInput(mouseX, mouseY, category);
        this.render();

        List<SubModule> subModules = this.getVisibleSubModules();
        double animationProgress = EasingUtil.getAnimation(this.getUniqueKey("clickgui"));
        boolean moduleOpened = (rightClickedModules.contains(this) || animationProgress != -1) && animationProgress != 0;

        if (moduleOpened) for (SubModule subModule : subModules) subModule.handle(mouseX, mouseY, category, this);
    }

    public void render() {
        String moduleName = annotation.name();
        if (this.getKeybind() != -1) moduleName += " &8[" + (this.getKeybind() == 1 ? "KEYBIND" : KeybindHandler.getCharacter(this.getKeybind())) + "]";

        Color moduleBackgroundColor = this.isEnabled() ? ENABLED_MODULE_BACKGROUND_COLOUR : BACKGROUND_COLOUR;

        // draw background of module
        RenderUtil.drawRect(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, moduleBackgroundColor);

        // drawing module name
        Color moduleTextColor = TEXT_COLOUR;

        if (this.getAnnotation().dangerous()) moduleTextColor = ClickGUIScreen.getDangerousColour();
        if (this.isEnabled()) moduleTextColor = ClickGUIScreen.getEnabledColour();
        if (ClickGUIScreen.moduleHovered == this) moduleTextColor = ClickGUIScreen.getOffTintColour(moduleTextColor);

        FontUtil.drawString(moduleName,  ClickGUIScreen.X_OFFSET+ClickGUIScreen.TEXT_INDENT_X, TEXT_Y, MODULE_FONT_SIZE, moduleTextColor, true);

        // draw arrow
        if (!this.getChildren().isEmpty()) {
            RenderUtil.drawArrow(
                    ClickGUIScreen.X_OFFSET+ClickGUIScreen.WIDTH - ARROW_X_OFFSET - ARROW_WIDTH / 2,
                    HEIGHT / 2 - ARROW_HEIGHT / 2,
                    ARROW_WIDTH,
                    ARROW_HEIGHT,
                    rightClickedModules.contains(this),
                    ARROW_LINE_SIZE,
                    Color.WHITE

            );
        }

        // offset, ready to draw next module / children
        GL11.glTranslated(0, HEIGHT, 0);
    }

    public void handleMouseInput(int mouseX, int mouseY, Category category) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, mouseX, mouseY) && mouseY / RenderUtil.getCurrentTranslation()[4] > category.renderPos[1] + category.HEIGHT) {
            // if module hovered is null, become the target of evil description rendering
            if (ClickGUIScreen.moduleHovered == null) {
                ClickGUIScreen.moduleHovered = this;
                ClickGUIScreen.moduleHoveredTime = System.currentTimeMillis();
            }

            if (ClickGUIScreen.mouseButton != -1) {
                if (ClickGUIScreen.mouseButton == 0) this.toggle();
                else if (ClickGUIScreen.mouseButton == 1) {
                    if (rightClickedModules.contains(this)) {
                        rightClickedModules.remove(this);
                        EasingUtil.addAnimation(this.getUniqueKey("clickgui"), closeAnimationLength, false, closeAnimation);
                    }
                    else {
                        rightClickedModules.add(this);
                        EasingUtil.addAnimation(this.getUniqueKey("clickgui"), openAnimationLength, true, openAnimation);
                    }
                } else if (ClickGUIScreen.mouseButton == 2) {
                    ClickGUIScreen.keybindModule = this;
                    ClickGUIScreen.keybindModule.setKeybind(1);
                }

                ClickGUIScreen.mouseButton = -1;
            }

        } else if (ClickGUIScreen.moduleHovered == this) ClickGUIScreen.moduleHovered = null;
    }


    private List<SubModule> getVisibleSubModules() {
        return this.getChildren().stream().filter(e -> e.shouldShow(true)).collect(Collectors.toList());
    }

    protected abstract void onEnable();
    protected abstract void onDisable();
}
