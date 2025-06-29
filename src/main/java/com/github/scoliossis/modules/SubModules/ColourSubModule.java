package com.github.scoliossis.modules.SubModules;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import com.github.scoliossis.utils.ScreenUtil;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

// guihriuhgriushguihsug todo: this file is STILL a mess.
public class ColourSubModule extends SubModule {
    public int[] lastColourPickerMousePos = new int[] {0,0};
    public int lastHslMouseY = 0;
    public int lastOpacityMouseY = 0;
    public boolean showing = false;

    public Color colour;

    public ColourSubModule(int[] lastColourPickerMousePos, int lastHslMouseY, int lastOpacityMouseY, boolean showing, Color colour) {
        super(null, null, null, null, null);
        this.lastColourPickerMousePos = lastColourPickerMousePos;
        this.lastHslMouseY = lastHslMouseY;
        this.lastOpacityMouseY = lastOpacityMouseY;
        this.showing = showing;

        this.colour = colour;
    }

    public ColourSubModule(Color colour) {
        super(null, null, null, null, null);
        this.colour = colour;
    }


    public ColourSubModule(Module parentModule, Field field, ArrayList<SubModule> children, SubModule parent, RegisterSubModule annotation) {
        super(parentModule, field, children, parent, annotation);
    }

    private final float COLOUR_SQUARE_WIDTH = 100;
    private final int COLOUR_BAR_OFFSET_X = 5;
    private final float COLOUR_SQUARE_X = ClickGUIScreen.X_OFFSET + COLOUR_BAR_OFFSET_X;
    private final float COLOUR_SQUARE_Y = HEIGHT;
    private final int COLOUR_BAR_WIDTH = 10;
    private final int AMOUNT_OF_HSL_COLOURS = RenderUtil.HSLcolours.length-1;
    private final int HSLsliderX = (int) (COLOUR_SQUARE_X + COLOUR_BAR_OFFSET_X + COLOUR_SQUARE_WIDTH);
    private final int OpacitySliderX = (int) (COLOUR_SQUARE_X + COLOUR_BAR_OFFSET_X*2 + COLOUR_SQUARE_WIDTH + COLOUR_BAR_WIDTH);
    private final int COLOUR_BAR_SELECTED_HEIGHT = 4;
    private final float COLOUR_HOVER_CIRCLE_SIZE = 4;
    private final float individualHSLvalueWidth = COLOUR_SQUARE_WIDTH / AMOUNT_OF_HSL_COLOURS;
    private final float SELECTED_COLOUR_SQUARE_WIDTH = 10;
    private final float SELECTED_COLOUR_SQUARE_X = ClickGUIScreen.X_OFFSET + ClickGUIScreen.WIDTH - (SELECTED_COLOUR_SQUARE_WIDTH * 2);
    private final float SELECTED_COLOUR_SQUARE_Y = HEIGHT/2 - SELECTED_COLOUR_SQUARE_WIDTH/2;

    private Color HSLcolor;
    private Color HSLcolorOpposite;
    private float opacityPercent;
    private float HslPercent;

    @Override
    public void render() {
        ColourSubModule colourSubModule = (ColourSubModule) this.get();

        FontUtil.drawString(this.getAnnotation().name(), ClickGUIScreen.X_OFFSET+TEXT_INDENT_X, TEXT_Y, FONT_SIZE, TEXT_COLOUR, true);
        RenderUtil.drawRect(SELECTED_COLOUR_SQUARE_X, SELECTED_COLOUR_SQUARE_Y,SELECTED_COLOUR_SQUARE_WIDTH,SELECTED_COLOUR_SQUARE_WIDTH, colourSubModule.colour);

        if (colourSubModule.showing) {
            // draw background
            RenderUtil.drawRect(COLOUR_SQUARE_X - COLOUR_BAR_OFFSET_X, COLOUR_SQUARE_Y, COLOUR_SQUARE_WIDTH + COLOUR_BAR_OFFSET_X * 4 + COLOUR_BAR_WIDTH * 2, COLOUR_SQUARE_WIDTH + COLOUR_BAR_OFFSET_X, BACKGROUND_COLOUR);

            RenderUtil.drawRectFadeFourWay(COLOUR_SQUARE_X, COLOUR_SQUARE_Y, COLOUR_SQUARE_WIDTH, COLOUR_SQUARE_WIDTH, HSLcolorOpposite, Color.BLACK, HSLcolor, Color.WHITE);
            float colourPickerX = MathHelper.clamp_float(colourSubModule.lastColourPickerMousePos[0] - 2, COLOUR_SQUARE_X, COLOUR_SQUARE_X + COLOUR_SQUARE_WIDTH - COLOUR_BAR_SELECTED_HEIGHT);
            float colourPickerY = MathHelper.clamp_float(colourSubModule.lastColourPickerMousePos[1] - 2, COLOUR_SQUARE_Y, COLOUR_SQUARE_Y + COLOUR_SQUARE_WIDTH - COLOUR_BAR_SELECTED_HEIGHT);
            RenderUtil.drawRoundedRectOutline(colourPickerX, colourPickerY, COLOUR_HOVER_CIRCLE_SIZE, COLOUR_HOVER_CIRCLE_SIZE, COLOUR_HOVER_CIRCLE_SIZE, 2, Color.WHITE);

            // drawing hsl slider
            for (int c = 0; c < AMOUNT_OF_HSL_COLOURS; c++)
                RenderUtil.drawRectFadeDown(HSLsliderX, COLOUR_SQUARE_Y + c * individualHSLvalueWidth, COLOUR_BAR_WIDTH, individualHSLvalueWidth, RenderUtil.HSLcolours[c], RenderUtil.HSLcolours[c + 1]);

            int HslSliderSelectedY = (int) MathHelper.clamp_float((COLOUR_SQUARE_Y + (COLOUR_SQUARE_WIDTH * HslPercent) - (COLOUR_BAR_SELECTED_HEIGHT / 2f)), COLOUR_SQUARE_Y, COLOUR_SQUARE_Y + COLOUR_SQUARE_WIDTH - COLOUR_BAR_SELECTED_HEIGHT);
            RenderUtil.drawRoundedRectOutline(HSLsliderX, HslSliderSelectedY, COLOUR_BAR_WIDTH, COLOUR_BAR_SELECTED_HEIGHT, COLOUR_HOVER_CIRCLE_SIZE, 2, Color.WHITE);

            // drawing opacity slider
            RenderUtil.drawRect(OpacitySliderX, COLOUR_SQUARE_Y, COLOUR_BAR_WIDTH, COLOUR_SQUARE_WIDTH, Color.BLACK);
            RenderUtil.drawRectFadeDown(OpacitySliderX, COLOUR_SQUARE_Y, COLOUR_BAR_WIDTH, COLOUR_SQUARE_WIDTH, Color.WHITE, new Color(0, 0, 0, 0));
            int opacitySliderSelectedY = (int) MathHelper.clamp_float((COLOUR_SQUARE_Y + (COLOUR_SQUARE_WIDTH * opacityPercent) - (COLOUR_BAR_SELECTED_HEIGHT / 2f)), COLOUR_SQUARE_Y, COLOUR_SQUARE_Y + COLOUR_SQUARE_WIDTH - COLOUR_BAR_SELECTED_HEIGHT);
            RenderUtil.drawRoundedRectOutline(OpacitySliderX, opacitySliderSelectedY, COLOUR_BAR_WIDTH, COLOUR_BAR_SELECTED_HEIGHT, COLOUR_HOVER_CIRCLE_SIZE, 2, Color.WHITE);

            GL11.glTranslated(0, COLOUR_SQUARE_WIDTH + COLOUR_BAR_OFFSET_X, 0);
        }
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY, Category category) {
        ColourSubModule colourSubModule = (ColourSubModule) this.get();

        int[] fixedMousePos = ScreenUtil.fixMousePos(mouseX, mouseY);

        if (ScreenUtil.isMouseOver(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, mouseX, mouseY) && ClickGUIScreen.mouseButton != -1) {
            colourSubModule.showing = !colourSubModule.showing;
            ClickGUIScreen.mouseButton = -1;
        }

        if (colourSubModule.showing) {
            opacityPercent = (colourSubModule.lastOpacityMouseY - COLOUR_SQUARE_Y) / COLOUR_SQUARE_WIDTH;

            float opacity = opacityPercent * 255;
            float hslPortion = MathHelper.clamp_float((AMOUNT_OF_HSL_COLOURS * (colourSubModule.lastHslMouseY - COLOUR_SQUARE_Y)) / COLOUR_SQUARE_WIDTH, 0, AMOUNT_OF_HSL_COLOURS);

            int HslColor1 = (int) (hslPortion % AMOUNT_OF_HSL_COLOURS);
            int HslColor2 = (HslColor1 + 1 % AMOUNT_OF_HSL_COLOURS);

            int HslColor3 = (int) ((hslPortion + 3) % AMOUNT_OF_HSL_COLOURS);
            int HslColor4 = (HslColor3 + 1 % AMOUNT_OF_HSL_COLOURS);

            float HslPortionPercent = hslPortion % 1;
            HslPercent = hslPortion / AMOUNT_OF_HSL_COLOURS;

            HSLcolor = RenderUtil.interpolateColors(RenderUtil.HSLcolours[HslColor1], RenderUtil.HSLcolours[HslColor2], HslPortionPercent);
            HSLcolorOpposite = RenderUtil.interpolateColors(RenderUtil.HSLcolours[HslColor3], RenderUtil.HSLcolours[HslColor4], HslPortionPercent);

            if (!ClickGUIScreen.mouseDown) return;

            boolean anySliderHovered = true;
            if (ScreenUtil.isMouseOver(COLOUR_SQUARE_X, COLOUR_SQUARE_Y, COLOUR_SQUARE_WIDTH, COLOUR_SQUARE_WIDTH, mouseX, mouseY)) {
                colourSubModule.lastColourPickerMousePos[0] = fixedMousePos[0];
                colourSubModule.lastColourPickerMousePos[1] = fixedMousePos[1];
            } else if (ScreenUtil.isMouseOver(HSLsliderX, COLOUR_SQUARE_Y, COLOUR_BAR_WIDTH, COLOUR_SQUARE_WIDTH, mouseX, mouseY))
                colourSubModule.lastHslMouseY = fixedMousePos[1];
            else if (ScreenUtil.isMouseOver(OpacitySliderX, COLOUR_SQUARE_Y, COLOUR_BAR_WIDTH, COLOUR_SQUARE_WIDTH, mouseX, mouseY))
                colourSubModule.lastOpacityMouseY = fixedMousePos[1];
            else anySliderHovered = false;

            if (anySliderHovered) {
                ClickGUIScreen.mouseButton = -1;

                float xPercent = MathHelper.clamp_float((colourSubModule.lastColourPickerMousePos[0] - COLOUR_SQUARE_X) / COLOUR_SQUARE_WIDTH, 0, 1);
                float yPercent = MathHelper.clamp_float((colourSubModule.lastColourPickerMousePos[1] - COLOUR_SQUARE_Y) / COLOUR_SQUARE_WIDTH, 0, 1);

                colourSubModule.colour = new Color(
                        (int) (HSLcolor.getRed() + ((255 - HSLcolor.getRed()) * (1 - xPercent)) - (HSLcolor.getRed()) * yPercent),
                        (int) (HSLcolor.getGreen() + ((255 - HSLcolor.getGreen()) * (1 - xPercent)) - (HSLcolor.getGreen()) * yPercent),
                        (int) (HSLcolor.getBlue() + ((255 - HSLcolor.getBlue()) * (1 - xPercent)) - (HSLcolor.getBlue()) * yPercent),
                        (int) MathHelper.clamp_float(255 - opacity, 0, 255)
                );

                this.set(colourSubModule);
            }
        }
    }
}
