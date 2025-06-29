package com.github.scoliossis.modules;

import com.github.scoliossis.Main;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.scoliossis.modules.impl.client.ClickGUIModule.*;

public enum Category {
    COMBAT,
    RENDER,
    MOVEMENT,
    PLAYER,
    SKYBLOCK,
    CLIENT;

    public int scrolledAmount = 0;
    public float renderedScroll = 0;
    public boolean shown = true;
    public float[] pos = defaultPos();
    public float[] renderPos = defaultPos();

    private float[] defaultPos() {
        return new float[] {0,0};
    }

    private static final String categorySavingFile = Main.extraSavedFeaturesPath + "categoryPositions" + Main.configExtension;

    public static void saveCategoryPositions() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
            HashMap<String, float[]> posJSON = new HashMap<>();

            for (Category category : Category.values())
                posJSON.put(category.name(), category.pos);

            Files.createDirectories(Paths.get(Main.extraSavedFeaturesPath));
            Files.write(Paths.get(categorySavingFile), gson.toJson(posJSON).getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadCategoryPositions() {
        try {
            if (Files.exists(Paths.get(categorySavingFile))) {
                String configFileText = FileUtils.readFileToString(new File(categorySavingFile));

                Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
                HashMap<String, ArrayList<Double>> posJSON = gson.fromJson(configFileText, HashMap.class);

                for (Category category : Category.values()) {
                    if (posJSON.containsKey(category.name())) {
                        ArrayList<Double> xy = posJSON.get(category.name());
                        category.pos = new float[]{xy.get(0).intValue(), xy.get(1).intValue()};
                        category.renderPos = category.pos;
                    }

                }
            }
            else {
                System.out.println("No category positions found, saving default positions.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public final float HEIGHT = 25;
    private final float TITLE_TEXT_Y = 3;
    private final int TITLE_FONT_SIZE = 15;
    private final Color TITLE_BACKGROUND_COLOUR = new Color(22,22,22, 200);
    private final Color TITLE_TEXT_COLOUR = Color.WHITE;

    public void handle(int mouseX, int mouseY, List<Module> modules) {
        // translate to where the start pos is
        GL11.glTranslated(this.renderPos[0] + ClickGUIScreen.WIDTH / 2d, this.renderPos[1], 0);

        if (ScreenUtil.isMouseOver(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, mouseX, mouseY) && ClickGUIScreen.mouseButton != -1) {
            this.mouseClicked(mouseX, mouseY, ClickGUIScreen.mouseButton);
            ClickGUIScreen.mouseButton = -1;
        }

        // handle dragging
        if (ClickGUIScreen.categoryBeingDragged == this) this.mouseDragged(mouseX, mouseY);

        // render category
        this.render();

        // move on to bigger and better things
        GL11.glTranslated(0,HEIGHT,0);

        List<Module> modulesInCategory = ModuleManager.getModulesByCategory(this, modules);
        double categoryAnimationProgress = EasingUtil.getAnimation(this.name());

        // render modules!
        if (!modulesInCategory.isEmpty() && (this.shown || categoryAnimationProgress != -1)) {
            if (categoryAnimationProgress != -1) GL11.glScaled(1, categoryAnimationProgress, 1);
            RenderUtil.glScissor(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, C.mc.displayHeight);

            handleScrolling(mouseX, mouseY, this, modulesInCategory);

            for (Module module : modulesInCategory) module.handle(mouseX, mouseY, this);

            GL11.glScaled(1, 1/categoryAnimationProgress, 1);
            RenderUtil.disableScissor();
        }
    }

    public void render() {
        // cool dragging animation
        this.renderPos[0] += (this.pos[0] - this.renderPos[0]) / ClickGUIScreen.fpsMulti;
        if (Math.abs(this.renderPos[0] - this.pos[0]) < 0.01) this.renderPos[0] = this.pos[0];

        double draggingRotation = (this.pos[0]-this.renderPos[0])/3;
        GL11.glRotated(draggingRotation, 0, 0, 1);

        this.renderPos[1] += (this.pos[1] - this.renderPos[1]) / ClickGUIScreen.fpsMulti;
        if (Math.abs(this.renderPos[1] - this.pos[1]) < 1) this.renderPos[1] = this.pos[1];

        GL11.glScaled(1, 1 - (this.pos[1] - this.renderPos[1])/1000, 1);

        // render category head
        String cuterCategoryName = this.name().charAt(0) + this.name().substring(1).toLowerCase();
        Color[] colorsFade = RenderUtil.getColorsFade(0, 100, RenderUtil.ThemeColours.Gay.colours, 3f);
        RenderUtil.drawRect(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, HEIGHT, TITLE_BACKGROUND_COLOUR);
        RenderUtil.drawRectFade(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, 1, colorsFade[0], colorsFade[1]);
        FontUtil.drawString("&l"+ cuterCategoryName, ClickGUIScreen.X_OFFSET+ClickGUIScreen.TEXT_INDENT_X, TITLE_TEXT_Y, TITLE_FONT_SIZE, TITLE_TEXT_COLOUR, false);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        switch (mouseButton) {
            // drag on left click
            case 0:
                int[] mousePos = ScreenUtil.fixMousePos(mouseX, mouseY);
                ClickGUIScreen.draggingOffset = new int[] {mousePos[0] - (int) ClickGUIScreen.X_OFFSET, mousePos[1]};
                ClickGUIScreen.categoryBeingDragged = this;
                break;

            // vanish on right click
            case 1:
                this.shown = !this.shown;
                EasingUtil.addAnimation(this.name(), this.shown ? openAnimationLength : closeAnimationLength, this.shown, this.shown ? openAnimation : closeAnimation);
                break;
        }
    }

    public void mouseDragged(int mouseX, int mouseY) {
        float[] translations = RenderUtil.getCurrentTranslation();

        double draggingX = mouseX/translations[3] - ClickGUIScreen.draggingOffset[0];
        this.pos = new float[] {(int) draggingX, (int) (mouseY/translations[4] - ClickGUIScreen.draggingOffset[1])};
    }

    private void handleScrolling(int mouseX, int mouseY, Category category, List<Module> modulesInCategory) {
        category.renderedScroll += (category.scrolledAmount - category.renderedScroll) / ClickGUIScreen.fpsMulti;

        // make sure scrolling doesn't overflow
        int shownSubmoduleCount = 0;
        for (Module module : modulesInCategory)
            shownSubmoduleCount += (int) module.getChildren().stream().filter(e -> e.shouldShow(true) && rightClickedModules.contains(module)).count();

        if (ScreenUtil.isMouseOver(ClickGUIScreen.X_OFFSET, 0, ClickGUIScreen.WIDTH, (Module.HEIGHT * modulesInCategory.size() + shownSubmoduleCount*SubModule.HEIGHT) - category.renderedScroll, mouseX, mouseY)) {
            if (ClickGUIScreen.scrollAmount != 0) {
                category.scrolledAmount += ClickGUIScreen.scrollAmount / 2;
                ClickGUIScreen.scrollAmount = 0;
            }
        }

        float maxHeight = (modulesInCategory.size() * Module.HEIGHT) + (shownSubmoduleCount * SubModule.HEIGHT) - Module.HEIGHT;
        category.scrolledAmount = (int) Math.min(Math.max(0, category.scrolledAmount), maxHeight);

        GL11.glTranslated(0, -category.renderedScroll, 0);
    }
}
