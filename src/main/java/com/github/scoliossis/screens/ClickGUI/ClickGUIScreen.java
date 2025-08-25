package com.github.scoliossis.screens.ClickGUI;

import com.github.scoliossis.Main;
import com.github.scoliossis.events.impl.KeyPressedEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.modules.impl.client.ClickGUIModule;
import com.github.scoliossis.screens.ClickGUI.SubModuleRenderers.*;
import com.github.scoliossis.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// todo: search bar
public class ClickGUIScreen extends GuiScreen {
    public static final int fontSize = 10;
    public static int GUI_TAB_WIDTH = 125;
    private static final int OUTLINE_GAP = 2;
    private static final int OUTLINE_WIDTH = 1;

    public static final int BASE_X = -GUI_TAB_WIDTH / 2;
    public static final int BASE_Y = 0;

    public static float fpsMultiplier = 1;
    public static SubModule currentSubModule;

    public static int mouseButton = -1;
    public static boolean leftMouseDown = false;

    protected static final CategoryRenderer categoryRenderer = new CategoryRenderer();
    protected static final ModuleRenderer moduleRenderer = new ModuleRenderer();

    protected static final BooleanSubModuleRenderer booleanSubModuleRenderer = new BooleanSubModuleRenderer();
    protected static final EnumSubModuleRenderer enumSubModuleRenderer = new EnumSubModuleRenderer();
    protected static final SliderSubModuleRenderer sliderSubModuleRenderer = new SliderSubModuleRenderer();
    protected static final ColourModuleRenderer colourSubModuleRenderer = new ColourModuleRenderer();
    protected static final SubCategoryRenderer subCategoryRenderer = new SubCategoryRenderer();

    public static Color secondaryColor = new Color(200, 200, 200);

    protected static Module moduleHovered;
    protected static SubModule subModuleHovered;
    protected static long hoverTime;

    private static final long minimumHoverTime = 500;
    private static final int hoverBoxXindent = 5;
    private static final int hoverBoxYindent = 2;
    private static final int hoverBoxTextSize = 7;

    private final GuiTextField searchBar = new GuiTextField(0, C.mc.fontRendererObj, -1, -1, 200, 0);
    private static final int searchBarXindent = 5;
    private static final int searchBarYindent = 2;
    private static final int searchBarTextSize = 10;

    @Override
    public void initGui() {
        searchBar.setMaxStringLength(50);
        searchBar.setCanLoseFocus(false);
        searchBar.setFocused(true);
        searchBar.setVisible(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!ModuleManager.isEnabled(ClickGUIModule.class)) {
            C.mc.displayGuiScreen(null);
            ClickGUIScreen.saveCategoryPositions();
            return;
        }

        fpsMultiplier = Math.max(Minecraft.getDebugFPS() * 0.1f, 2);

        int scrolledAmount = Mouse.getDWheel() / 5;

        GL11.glPushMatrix();

        List<Module> modules = searchBar.getText().isEmpty() ? ModuleManager.getModules() : FuzzySearchUtil.getSimilarModules(searchBar.getText(), 60);
        if (!modules.contains(moduleHovered))
            moduleHovered = null;

        for (Category category : Category.values()) {
            GL11.glPushMatrix();

            List<Module> modulesInCategory = ModuleManager.getModulesByCategory(category, modules);

            // cool dragging animation
            category.renderX += (category.posX - category.renderX) / fpsMultiplier;
            if (Math.abs(category.renderX - category.posX) < 0.01) category.renderX = category.posX;

            float draggingRotationX = MathHelper.clamp_float((category.posX-category.renderX)/3, -100, 100);

            category.renderY += (category.posY - category.renderY) / fpsMultiplier;
            if (Math.abs(category.renderY - category.posY) < 0.01) category.renderY = category.posY;

            double draggingRotationY =  MathHelper.clamp_float((category.posY-category.renderY)/3, -100, 100);

            if (ClickGUIModule.fancyDragging) {
                GL11.glTranslated(category.renderX - BASE_X, category.renderY - BASE_Y, 0);
                GL11.glRotated(draggingRotationX, 0, 0, 1);
                GL11.glRotated(draggingRotationY, 1, 0, 0);
            }
            else GL11.glTranslated(category.posX - BASE_X, category.posY - BASE_Y, 0);

            drawTop(category);
            categoryRenderer.handleMouse(category, mouseX, mouseY);
            categoryRenderer.render(category);
            drawBottom(category);

            RenderUtil.glScissor(BASE_X - OUTLINE_GAP - OUTLINE_WIDTH, 0, GUI_TAB_WIDTH + OUTLINE_GAP*2 + OUTLINE_WIDTH*2, C.res().getScaledHeight());

            category.renderScroll += (category.scroll - category.renderScroll) / fpsMultiplier;
            if (Math.abs(category.scroll - category.renderScroll) < 0.01) category.renderScroll = category.scroll;

            GL11.glTranslated(0, category.renderScroll, 0);

            if (category.shouldShow()) {
                double categoryAnimationProgress = EasingUtil.getAnimation(category.name());
                if (categoryAnimationProgress != -1) GL11.glScaled(1, categoryAnimationProgress, 1);

                for (Module module : modulesInCategory) {
                    moduleRenderer.handleMouse(module, mouseX, mouseY);
                    moduleRenderer.render(module, mouseX, mouseY);

                    double moduleAnimationProgress = EasingUtil.getAnimation(module.getUniqueKey(""));
                    if (moduleAnimationProgress != -1) GL11.glScaled(1, moduleAnimationProgress, 1);

                    if (!module.isOpen() && moduleAnimationProgress == -1) continue;

                    for (SubModule subModule : module.getChildren()) {
                        if (!subModule.shouldRender()) {
                            if (subModule == subModuleHovered) subModuleHovered = null;
                            continue;
                        }

                        double parentAnimationProgress = subModule.getAnimationProgress();
                        if (parentAnimationProgress != -1) GL11.glScaled(1, parentAnimationProgress, 1);

                        SubModuleRenderer.handle(mouseX, mouseY, subModule);

                        if (parentAnimationProgress != -1) GL11.glScaled(1,1/parentAnimationProgress,1);
                    }

                    if (moduleAnimationProgress != -1) GL11.glScaled(1,1/moduleAnimationProgress,1);
                }
            }

            if (category.shouldShow()) drawBottom(category);

            float[] translation = RenderUtil.getCurrentTranslation();
            float categoryTotalHeight = Math.max(translation[1] - category.renderY - categoryRenderer.CATEGORY_HEIGHT, categoryRenderer.CATEGORY_HEIGHT);
            if (ScreenUtil.isMouseOver(BASE_X, BASE_Y - categoryTotalHeight, GUI_TAB_WIDTH, categoryTotalHeight, mouseX, mouseY)) {
                category.scroll += scrolledAmount;
                scrolledAmount = 0;
            }

            if (translation[4] == 1 && category.open) {
                category.scroll =
                        MathHelper.clamp_float(
                                category.scroll,
                                -categoryTotalHeight + category.renderScroll + categoryRenderer.CATEGORY_HEIGHT,
                                0
                        );
            }

            GL11.glPopMatrix();
            RenderUtil.disableScissor();
        }

        GL11.glPopMatrix();

        mouseButton = -1;

        String hoverText = moduleHovered != null ? moduleHovered.getAnnotation().description() : subModuleHovered != null ? subModuleHovered.getAnnotation().description() : "";

        if (!hoverText.isEmpty() && System.currentTimeMillis() - ClickGUIScreen.hoverTime >= minimumHoverTime) {
            float hoverBoxHeight = FontUtil.getFontHeight(hoverBoxTextSize);
            int hoverBoxWidth = FontUtil.getStringWidth(hoverText, hoverBoxTextSize) + (hoverBoxXindent * 2);

            RenderUtil.drawRect(mouseX, mouseY - hoverBoxHeight, hoverBoxWidth, hoverBoxHeight + hoverBoxYindent*2, new Color(0, 0, 0, 100));
            FontUtil.drawString(hoverText, mouseX + hoverBoxXindent, mouseY - hoverBoxHeight + hoverBoxYindent, hoverBoxTextSize, Color.WHITE, true);
        }

        if (!searchBar.getText().isEmpty()) {
            int searchBarWidth = FontUtil.getStringWidth(searchBar.getText(), searchBarTextSize) + (searchBarXindent * 2);
            float searchBarX = C.res().getScaledWidth()/2f-searchBarWidth/2f;
            float searchBarY = C.res().getScaledHeight()/1.1f;

            RenderUtil.drawRect(searchBarX, searchBarY, searchBarWidth, FontUtil.getFontHeight(searchBarTextSize) + searchBarYindent * 2, new Color(0, 0, 0, 100));
            FontUtil.drawString(searchBar.getText(), searchBarX + searchBarXindent, searchBarY + searchBarYindent, searchBarTextSize, Color.WHITE, true);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void drawTop(Category category) {
        RenderUtil.drawRect(BASE_X - OUTLINE_GAP - OUTLINE_WIDTH, BASE_Y - OUTLINE_WIDTH, GUI_TAB_WIDTH + OUTLINE_GAP*2 + OUTLINE_WIDTH*2, OUTLINE_WIDTH, category.color);
    }

    public static void drawGuiBackground(int height, Category category) {
        RenderUtil.drawRect(
                ClickGUIScreen.BASE_X - OUTLINE_GAP,
                ClickGUIScreen.BASE_Y,
                ClickGUIScreen.GUI_TAB_WIDTH + OUTLINE_GAP*2,
                height + OUTLINE_GAP,
                new Color(26, 26, 26)
        );

        RenderUtil.drawRect(BASE_X - OUTLINE_GAP - OUTLINE_WIDTH, BASE_Y, OUTLINE_WIDTH, height + OUTLINE_GAP + OUTLINE_WIDTH, category.color);
        RenderUtil.drawRect(BASE_X + GUI_TAB_WIDTH + OUTLINE_GAP, BASE_Y, OUTLINE_WIDTH, height + OUTLINE_GAP + OUTLINE_WIDTH, category.color);
    }

    public static void drawBottom(Category category) {
        RenderUtil.drawRect(BASE_X - OUTLINE_GAP - OUTLINE_WIDTH, BASE_Y + OUTLINE_GAP, GUI_TAB_WIDTH + OUTLINE_GAP*2 + OUTLINE_WIDTH*2, OUTLINE_WIDTH, category.color);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (KeybindHandler.listeningModule != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                KeybindHandler.removeKeybind(KeybindHandler.listeningModule);
                KeybindHandler.listeningModule = null;
            }
            else KeybindHandler.onKeyPressed(new KeyPressedEvent(keyCode, true));
        }
        else if (keyCode == Keyboard.KEY_ESCAPE) ModuleManager.setEnabled(ClickGUIModule.class, false);
        else if (subModuleHovered != null && subModuleHovered.isSlider()) {
            double increment = subModuleHovered.getAnnotation().increment();
            if (subModuleHovered.getField().getType() == long.class || subModuleHovered.getField().getType() == int.class && increment < 1) increment = 1;

            double value = Double.parseDouble(subModuleHovered.get().toString());

            if (keyCode == Keyboard.KEY_RIGHT) subModuleHovered.set(value + increment);
            if (keyCode == Keyboard.KEY_LEFT) subModuleHovered.set(value - increment);
        }
        else {
            searchBar.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ClickGUIScreen.mouseButton = mouseButton;
        if (mouseButton == 0) leftMouseDown = true;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        ClickGUIScreen.mouseButton = -1;
        if (mouseButton == 0) {
            leftMouseDown = false;
            categoryRenderer.currentDraggingCategory = null;
            currentSubModule = null;
        }
    }

    private static final String categorySavingFile = Main.extraSavedFeaturesPath + "categoryPositions" + Main.configExtension;

    public static void saveCategoryPositions() {
        try {
            HashMap<String, float[]> posJSON = new HashMap<>();

            for (Category category : Category.values())
                posJSON.put(category.name(), new float[]{category.posX, category.posY});

            Files.createDirectories(Paths.get(Main.extraSavedFeaturesPath));
            Files.write(Paths.get(categorySavingFile), C.gson.toJson(posJSON).getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadCategoryPositions() {
        try {
            if (Files.exists(Paths.get(categorySavingFile))) {
                String configFileText = FileUtils.readFileToString(new File(categorySavingFile));

                HashMap<String, ArrayList<Double>> posJSON = C.gson.fromJson(configFileText, HashMap.class);

                for (Category category : Category.values()) {
                    if (posJSON.containsKey(category.name())) {
                        ArrayList<Double> xy = posJSON.get(category.name());
                        category.posX = category.renderX = xy.get(0).floatValue();
                        category.posY = category.renderY = xy.get(1).floatValue();
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
}