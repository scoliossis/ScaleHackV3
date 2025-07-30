package com.github.scoliossis.screens.ClickGUI.SubModuleRenderers;

import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.screens.ClickGUI.ClickGUIScreen;
import com.github.scoliossis.screens.ClickGUI.SubModuleRenderer;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import com.github.scoliossis.utils.ScreenUtil;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColourModuleRenderer extends SubModuleRenderer {
    private static final int DISPLAY_COLOR_SIZE = 10;

    private final int BOTTOM_GAP = 10;

    private static final int COLOUR_TRIANGLE_SIZE = 100;
    private final int COLOUR_SQUARE_X = ClickGUIScreen.BASE_X + (ClickGUIScreen.GUI_TAB_WIDTH/2) - (COLOUR_TRIANGLE_SIZE/2);
    private final int COLOUR_SQUARE_Y = ClickGUIScreen.BASE_Y + SUBMODULE_HEIGHT + BOTTOM_GAP;
    private final int SLIDERS_HEIGHT = 10;

    private final Color[] HSLcolours = new Color[] {
            new Color(255, 0, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 255, 255),
            new Color(0, 0, 255),
            new Color(255, 0, 255),
            new Color(255, 0, 0),
    };

    @Override
    public void handleMouse(int mouseX, int mouseY, SubModule subModule) {
        if (ScreenUtil.isMouseOver(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0) {
                boolean currentState = subModule.colorSettingValues.open;
                subModule.colorSettingValues.open = !currentState;
                if (!currentState) {
                    Color currentColour = (Color) subModule.get();
                    subModule.colorSettingValues.hueValue = Color.RGBtoHSB(currentColour.getRed(), currentColour.getGreen(), currentColour.getBlue(), null)[0];

                    float[] hsbValues = Color.RGBtoHSB(currentColour.getRed(), currentColour.getGreen(), currentColour.getBlue(), null);
                    int selectedBrightness = (int) (hsbValues[1] * COLOUR_TRIANGLE_SIZE);
                    int selectedSaturation = COLOUR_TRIANGLE_SIZE - (int) (hsbValues[2] * COLOUR_TRIANGLE_SIZE);
                    subModule.colorSettingValues.mX = selectedBrightness;
                    subModule.colorSettingValues.mY = selectedSaturation;
                    subModule.colorSettingValues.hueValue = hsbValues[0];
                }
                EasingUtil.addAnimation(
                        subModule.getUniqueKey(),
                        !currentState ? ClickGUIModule.openAnimationLength : ClickGUIModule.closeAnimationLength,
                        !currentState,
                        !currentState ? ClickGUIModule.openAnimation : ClickGUIModule.closeAnimation
                );

                ClickGUIScreen.mouseButton = -1;
                return;
            }
        }

        if (!subModule.colorSettingValues.open) return;
        Color currentColour = (Color) subModule.get();
        int[] mousePos = ScreenUtil.fixMousePos(mouseX, mouseY);

        if (ScreenUtil.isMouseOver(COLOUR_SQUARE_X, COLOUR_SQUARE_Y, COLOUR_TRIANGLE_SIZE, COLOUR_TRIANGLE_SIZE, mouseX, mouseY)) {
            if (ClickGUIScreen.mouseButton == 0) ClickGUIScreen.currentSubModule = subModule;
            ClickGUIScreen.mouseButton = -1;
        }

        if (ClickGUIScreen.currentSubModule == subModule) {
            float hue = subModule.colorSettingValues.hueValue;
            float saturation = MathHelper.clamp_float((float) (mousePos[0] - COLOUR_SQUARE_X) / COLOUR_TRIANGLE_SIZE, 0, 1);
            float brightness = MathHelper.clamp_float(1 - ((float) (mousePos[1] - COLOUR_SQUARE_Y) / COLOUR_TRIANGLE_SIZE), 0, 1);

            Color newColour = Color.getHSBColor(hue, saturation, brightness);
            newColour = new Color(newColour.getRed(), newColour.getGreen(), newColour.getBlue(), currentColour.getAlpha());
            subModule.set(newColour);

            subModule.colorSettingValues.mX = MathHelper.clamp_int(mousePos[0] - COLOUR_SQUARE_X, 0, COLOUR_TRIANGLE_SIZE);
            subModule.colorSettingValues.mY = MathHelper.clamp_int(mousePos[1] - ClickGUIScreen.BASE_Y - SUBMODULE_HEIGHT - BOTTOM_GAP, 0, COLOUR_TRIANGLE_SIZE);

            return;
        }

        if (!ClickGUIScreen.leftMouseDown) return;

        if (ScreenUtil.isMouseOver(COLOUR_SQUARE_X, COLOUR_SQUARE_Y + COLOUR_TRIANGLE_SIZE + BOTTOM_GAP, COLOUR_TRIANGLE_SIZE, SLIDERS_HEIGHT, mouseX, mouseY)) {
            float hue = MathHelper.clamp_float((float) (mousePos[0] - COLOUR_SQUARE_X) / COLOUR_TRIANGLE_SIZE, 0, 1);
            subModule.colorSettingValues.hueValue = hue;

            float[] hsbValues = Color.RGBtoHSB(currentColour.getRed(), currentColour.getGreen(), currentColour.getBlue(), null);
            Color colour = Color.getHSBColor(hue, hsbValues[1], hsbValues[2]);
            colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), currentColour.getAlpha());

            subModule.set(colour);
        }
        else if (ScreenUtil.isMouseOver(COLOUR_SQUARE_X, COLOUR_SQUARE_Y + COLOUR_TRIANGLE_SIZE + BOTTOM_GAP*2 + SLIDERS_HEIGHT, COLOUR_TRIANGLE_SIZE, SLIDERS_HEIGHT, mouseX, mouseY)) {
            int opacity = (int) (MathHelper.clamp_float((float) (mousePos[0] - COLOUR_SQUARE_X) / COLOUR_TRIANGLE_SIZE, 0, 1) * 255);

            Color colour = new Color(currentColour.getRed(), currentColour.getGreen(), currentColour.getBlue(), opacity);
            subModule.set(colour);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, SubModule subModule) {
        super.render(mouseX, mouseY, subModule);

        Color currentColour = (Color) subModule.get();

        RenderUtil.drawRect(ClickGUIScreen.BASE_X, ClickGUIScreen.BASE_Y, ClickGUIScreen.GUI_TAB_WIDTH, SUBMODULE_HEIGHT, this.SUBMODULE_BACKGROUND_COLOUR);
        RenderUtil.drawRect(
                ClickGUIScreen.BASE_X + ClickGUIScreen.GUI_TAB_WIDTH - DISPLAY_COLOR_SIZE - this.LEFT_OFFSET,
                ClickGUIScreen.BASE_Y + SUBMODULE_HEIGHT / 2f - (DISPLAY_COLOR_SIZE/2f),
                DISPLAY_COLOR_SIZE,
                DISPLAY_COLOR_SIZE,
                currentColour
        );

        FontUtil.drawString(subModule.getAnnotation().name(), SUBMODULE_TEXT_X, SUBMODULE_TEXT_Y, ClickGUIScreen.fontSize, Color.WHITE, true);

        GL11.glTranslated(0,SUBMODULE_HEIGHT,0);

        double animationLength = EasingUtil.getAnimation(subModule.getUniqueKey());
        boolean open = subModule.colorSettingValues.open || animationLength != -1;

        if (!open) return;

        if (animationLength != -1) GL11.glScaled(1, animationLength, 1);

        ClickGUIScreen.drawGuiBackground(COLOUR_TRIANGLE_SIZE + BOTTOM_GAP*4 + SLIDERS_HEIGHT*2, subModule.getParentModule().getAnnotation().category());

        Color colour = Color.getHSBColor(subModule.colorSettingValues.hueValue, 1, 1);

        RenderUtil.drawGradientLR(COLOUR_SQUARE_X, BOTTOM_GAP, COLOUR_TRIANGLE_SIZE, COLOUR_TRIANGLE_SIZE, Color.WHITE, colour);
        RenderUtil.drawGradientTB(COLOUR_SQUARE_X, BOTTOM_GAP, COLOUR_TRIANGLE_SIZE, COLOUR_TRIANGLE_SIZE, new Color(0,0,0,0), Color.BLACK);

        RenderUtil.drawRectOutline(COLOUR_SQUARE_X + subModule.colorSettingValues.mX - 3, ClickGUIScreen.BASE_Y + BOTTOM_GAP + subModule.colorSettingValues.mY - 3, 6, 6, 2, Color.WHITE);

        GL11.glTranslated(0,COLOUR_TRIANGLE_SIZE + BOTTOM_GAP*2,0);

        // draw HSL slider
        for (int i = 0; i < HSLcolours.length-1; i++) {
            float w = (float) COLOUR_TRIANGLE_SIZE / (HSLcolours.length-1);
            float x = COLOUR_SQUARE_X + w*i;
            RenderUtil.drawGradientLR(x, 0, w, SLIDERS_HEIGHT, HSLcolours[i], HSLcolours[i+1]);
        }
        RenderUtil.drawRectOutline(COLOUR_SQUARE_X, 0, COLOUR_TRIANGLE_SIZE, SLIDERS_HEIGHT, 2, Color.WHITE);

        int selectedBrightness = MathHelper.clamp_int((int) (subModule.colorSettingValues.hueValue * COLOUR_TRIANGLE_SIZE)-3, 0, COLOUR_TRIANGLE_SIZE-6);
        RenderUtil.drawRectOutline(COLOUR_SQUARE_X + selectedBrightness, 0, 6, SLIDERS_HEIGHT, 2, Color.WHITE);
        GL11.glTranslated(0,SLIDERS_HEIGHT + BOTTOM_GAP,0);

        // draw opacity slider
        RenderUtil.drawGradientLR(COLOUR_SQUARE_X, 0, COLOUR_TRIANGLE_SIZE, SLIDERS_HEIGHT, new Color(0,0,0,0), Color.WHITE);
        RenderUtil.drawRectOutline(COLOUR_SQUARE_X, 0, COLOUR_TRIANGLE_SIZE, SLIDERS_HEIGHT, 2, Color.WHITE);

        int selectedOpacity = MathHelper.clamp_int((int) (((currentColour.getAlpha()/255f) * COLOUR_TRIANGLE_SIZE)-3f), 0, COLOUR_TRIANGLE_SIZE-6);
        RenderUtil.drawRectOutline(COLOUR_SQUARE_X + selectedOpacity, 0, 6, SLIDERS_HEIGHT, 2, Color.WHITE);
        GL11.glTranslated(0,SLIDERS_HEIGHT + BOTTOM_GAP,0);

        if (animationLength != -1) GL11.glScaled(1, 1/animationLength, 1);
    }
}