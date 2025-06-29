package com.github.scoliossis.screens;

import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

import static com.github.scoliossis.modules.impl.client.ClickGUIModule.clickGUIenabledColour;
import static com.github.scoliossis.modules.impl.client.ClickGUIModule.searchBar;

// todo: clean this up, everything about this is ugly.
public class ClickGUIScreen extends GuiScreen {
    public static Category categoryBeingDragged = null;
    public static int[] draggingOffset = null;

    public static Module keybindModule = null;

    public static Module moduleHovered = null;
    public static SubModule submoduleHovered = null;
    public static long moduleHoveredTime = 0;

    public static int mouseButton = -1;
    public static boolean mouseDown = false;

    public static final int WIDTH = 140;
    public static final float OUTSIDE_RADIUS = 0;
    public static final int TEXT_INDENT_X = 10;

    // colour of enums and number in slider
    public static final Color EXTRA_COLOUR = new Color(100,100,100);
    public static final float X_OFFSET = -WIDTH/2f;

    private final int TOOLTIP_HOVER_TIME_MS = 1000;
    private final Color TOOLTIP_BACKGROUND_COLOUR = new Color(22,22,22,100);
    private final int TOOLTIP_BACKGROUND_RADIUS = 0;
    private final int TOOLTIP_BACKGROUND_OFFSET_X = 8;
    private final int TOOLTIP_BACKGROUND_OFFSET_Y = -10;
    private final int TOOLTIP_BACKGROUND_INDENT_X = 3;
    private final int TOOLTIP_BACKGROUND_INDENT_Y = 3;
    private final Color TOOLTIP_TEXT_COLOUR = new Color(200,200,200);
    private final int TOOLTIP_FONT_SIZE = 7;

    private final float SEARCH_BAR_INDENT_X = 5;
    private final float SEARCH_BAR_INDENT_Y = 1;
    private final float SEARCH_BAR_Y_OFFSET_BOTTOM = 50;
    private final Color SEARCH_BAR_BACKGROUND_COLOUR = new Color(22,22,22,100);
    private final int SEARCH_BAR_BACKGROUND_RADIUS = 0;
    private final Color SEARCH_BAR_TEXT_COLOUR = new Color(200,200,200);
    private final int SEARCH_BAR_FONT_SIZE = 12;

    public static float fpsMulti = 1;

    public static int scrollAmount = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // if you fps is below 20 in this gui lwk log off.
        fpsMulti = Math.max(Minecraft.getDebugFPS() * 0.1f, 2);
        scrollAmount = -Mouse.getDWheel();

        // begin rendering
        GL11.glPushMatrix();

        // scale to be constant size, no matter the gui scale
        //GL11.glScaled((1d / C.res().getScaleFactor()), 1d / C.res().getScaleFactor(), 1);

        // push again after scaling so I can undo translation without undoing scaling for rendering tooltips last
        GL11.glPushMatrix();

        List<Module> modules = getVisibleModules();
        if (!modules.contains(moduleHovered)) moduleHovered = null;

        for (Category category : Category.values()) {
            GL11.glPushMatrix();

            // render category
            category.handle(mouseX, mouseY, modules);

            GL11.glPopMatrix();
        }

        // undo all translations
        GL11.glPopMatrix();

        if (System.currentTimeMillis() - moduleHoveredTime >= TOOLTIP_HOVER_TIME_MS)
            renderDescription(mouseX, mouseY);
        renderSearchbar();

        // reset mouseButton, we only want it set for 1 tick.
        mouseButton = -1;

        // undoes scaling
        GL11.glPopMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keybindModule != null) {
            if (keyCode == 1) KeybindHandler.removeKeybind(keybindModule);
            else KeybindHandler.addKeybind(keybindModule, keyCode);
            keybindModule = null;
        }
        else if (keyCode == 1) {
            Category.saveCategoryPositions();
            ModuleManager.getModule(ClickGUIModule.class).setEnabled(false);
        }
        else {
            if (submoduleHovered != null) {
                if (submoduleHovered.getField().getType() == double.class || submoduleHovered.getField().getType() == float.class || submoduleHovered.getField().getType() == long.class || submoduleHovered.getField().getType() == int.class) {
                    double increment = submoduleHovered.getAnnotation().increment();
                    if (submoduleHovered.getField().getType() == long.class || submoduleHovered.getField().getType() == int.class && increment < 1) increment = 1;

                    double value = Double.parseDouble(submoduleHovered.get().toString());

                    if (keyCode == Keyboard.KEY_RIGHT) submoduleHovered.set(value + increment);
                    if (keyCode == Keyboard.KEY_LEFT) submoduleHovered.set(value - increment);
                }
            }
            searchBar.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ClickGUIScreen.mouseButton = mouseButton;

        if (mouseButton == 0) mouseDown = true;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            mouseDown = false;
            categoryBeingDragged = null;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static List<Module> getVisibleModules() {
        List<Module> modules = ModuleManager.getModules();
        if (!searchBar.getText().isEmpty()) modules = FuzzySearchUtil.getSimilarModules(searchBar.getText());

        return modules;
    }

    public static Color getEnabledColour() {
        return clickGUIenabledColour.colour;
    }

    public static Color getDangerousColour() {
        return new Color(255, 211, 211);
    }

    public static Color getOffTintColour(Color colour) {
        return new Color((int) (colour.getRed() / 1.3), (int) (colour.getGreen() / 1.3), (int) (colour.getBlue() / 1.3), colour.getAlpha());
    }

    private void renderDescription(int mouseX, int mouseY) {
        if (submoduleHovered != null || moduleHovered != null) {
            String description = moduleHovered != null ? moduleHovered.getAnnotation().description() : submoduleHovered.getAnnotation().description();
            if ((moduleHovered != null && moduleHovered.getAnnotation().dangerous()) || (submoduleHovered != null && submoduleHovered.getAnnotation().dangerous()))
                description = "DANGEROUS! " + description;

            if (!description.isEmpty()) {
                GL11.glPushMatrix();

                float descriptionWidth = FontUtil.getStringWidth(description, TOOLTIP_FONT_SIZE);
                float fontHeight = FontUtil.getFontHeight(TOOLTIP_FONT_SIZE);

                int[] mousePos = ScreenUtil.fixMousePos(mouseX, mouseY);

                RenderUtil.drawRect(mousePos[0] - TOOLTIP_BACKGROUND_INDENT_X + TOOLTIP_BACKGROUND_OFFSET_X, mousePos[1] - TOOLTIP_BACKGROUND_INDENT_Y + TOOLTIP_BACKGROUND_OFFSET_Y, descriptionWidth + TOOLTIP_BACKGROUND_INDENT_X * 2, fontHeight + TOOLTIP_BACKGROUND_INDENT_Y * 2, TOOLTIP_BACKGROUND_COLOUR);
                FontUtil.drawString(description, mousePos[0] + TOOLTIP_BACKGROUND_OFFSET_X, mousePos[1] + TOOLTIP_BACKGROUND_OFFSET_Y, TOOLTIP_FONT_SIZE, TOOLTIP_TEXT_COLOUR, true);

                GL11.glPopMatrix();
            }
        }
    }

    private void renderSearchbar() {
        String searchBarText = searchBar.getText();
        if (!searchBarText.isEmpty()) {
            float searchBarTextWidth = FontUtil.getStringWidth(searchBarText, SEARCH_BAR_FONT_SIZE);
            float searchBarFontHeight = FontUtil.getFontHeight(SEARCH_BAR_FONT_SIZE);
            RenderUtil.drawRect(C.res().getScaledWidth() / 2f - searchBarTextWidth / 2 - SEARCH_BAR_INDENT_X, C.res().getScaledHeight() - SEARCH_BAR_Y_OFFSET_BOTTOM - SEARCH_BAR_INDENT_Y, searchBarTextWidth + SEARCH_BAR_INDENT_X * 2, searchBarFontHeight + SEARCH_BAR_INDENT_Y * 2, SEARCH_BAR_BACKGROUND_COLOUR);
            FontUtil.drawCenteredString(searchBarText, C.res().getScaledWidth() / 2f, C.res().getScaledHeight() - SEARCH_BAR_Y_OFFSET_BOTTOM + searchBarFontHeight / 2 - searchBarFontHeight / 2, SEARCH_BAR_FONT_SIZE, SEARCH_BAR_TEXT_COLOUR, true);
        }

    }
}